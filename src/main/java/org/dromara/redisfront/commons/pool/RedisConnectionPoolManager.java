
package org.dromara.redisfront.commons.pool;

import cn.hutool.core.exceptions.ExceptionUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.event.command.CommandListener;
import io.lettuce.core.event.command.CommandStartedEvent;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.codec.Utf8KeyByteArrayValueCodec;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.LogInfo;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.info.LogStatusHolder;
import org.dromara.redisfront.ui.event.CommandExecuteEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@SuppressWarnings("all")
public class RedisConnectionPoolManager {

    private static final int MAX_TOTAL = 5;
    private static final int MAX_IDLE = 5;
    private static final int MIN_IDLE = 2;
    private static final long MAX_WAIT_MILLIS = 5000;

    private static final Utf8KeyByteArrayValueCodec UTF8_KEY_BYTE_ARRAY_VALUE_CODEC = new Utf8KeyByteArrayValueCodec();

    private static final Map<String, GenericObjectPool<StatefulRedisClusterConnection<String, byte[]>>> CLUSTER_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisClusterPubSubConnection<String, String>>> CLUSTER_PUB_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisSentinelConnection<String, String>>> SENTINEL_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisConnection<String, byte[]>>> NORMAL_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisPubSubConnection<String, String>>> NORMAL_PUB_POOLS = new ConcurrentHashMap<>();

    public static StatefulRedisClusterPubSubConnection<String, String> getClusterConnectPubSub(
            RedisConnectContext context) {
        return getConnection(CLUSTER_PUB_POOLS, context, () -> {
            RedisURI uri = LettuceUtils.createRedisURI(context);
            RedisClusterClient client = LettuceUtils.getRedisClusterClient(uri, context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connectPubSub();
        });
    }

    public static StatefulRedisClusterConnection<String, byte[]> getClusterConnection(RedisConnectContext context) {
        return getConnection(CLUSTER_POOLS, context, () -> {
            RedisURI uri = LettuceUtils.createRedisURI(context);
            RedisClusterClient client = LettuceUtils.getRedisClusterClient(uri, context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connect(UTF8_KEY_BYTE_ARRAY_VALUE_CODEC);
        });
    }

    public static StatefulRedisSentinelConnection<String, String> getSentinelConnection(RedisConnectContext context) {
        return getConnection(SENTINEL_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connectSentinel();
        });
    }

    public static StatefulRedisConnection<String, byte[]> getConnection(RedisConnectContext context) {
        return getConnection(NORMAL_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connect(UTF8_KEY_BYTE_ARRAY_VALUE_CODEC);
        });
    }

    static String commandLogInfo(String type, String commandString) {
        return type;
    }

    private static void publishCommandEvent(CommandStartedEvent event, RedisConnectContext context) {
        if (LogStatusHolder.getIgnoredLog() == null) {
            String type = event.getCommand().getType().toString();
            CommandArgs<Object, Object> args = event.getCommand().getArgs();
            String commandString = RedisFrontUtils.isNotNull(args) ? args.toCommandString() : "";
            LogInfo logInfo = new LogInfo();
            logInfo.setIp(context.getHost());
            logInfo.setDate(LocalDateTime.now());
            logInfo.setInfo(commandLogInfo(type, commandString));
            RedisFrontContext.publishEvent(new CommandExecuteEvent(logInfo, context.getId()));
        }
    }

    public static StatefulRedisPubSubConnection<String, String> getConnectPubSub(RedisConnectContext context) {
        return getConnection(NORMAL_PUB_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connectPubSub();
        });
    }

    private static <T> T getConnection(Map<String, GenericObjectPool<T>> poolMap,
            RedisConnectContext context,
            ConnectionSupplier<T> supplier) {
        String poolKey = context.key();
        try {
            GenericObjectPool<T> pool = poolMap.computeIfAbsent(poolKey, ignore -> {
                GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
                config.setMaxTotal(MAX_TOTAL);
                config.setMaxIdle(MAX_IDLE);
                config.setMinIdle(MIN_IDLE);
                config.setMaxWait(Duration.ofMillis(MAX_WAIT_MILLIS));
                config.setTestOnBorrow(true);
                config.setTestWhileIdle(true);
                return new GenericObjectPool<>(new RedisConnectionFactory<>(supplier), config);
            });
            return pool.borrowObject();

        } catch (Exception e) {
            cleanupContextPool(context);
            if (ExceptionUtil.isCausedBy(e, RedisCommandExecutionException.class)) {
                Throwable causedBy = ExceptionUtil.getCausedBy(e, RedisCommandExecutionException.class);
                throw new RedisFrontException(causedBy.getMessage());
            } else {
                throw new RedisFrontException("Get connection failed", e, false);
            }
        }
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisClusterConnection<String, byte[]> connection) {
        String poolKey = context.key();
        returnConnection(CLUSTER_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisClusterPubSubConnection<String, String> connection) {
        String poolKey = context.key();
        returnConnection(CLUSTER_PUB_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisSentinelConnection<String, String> connection) {
        String poolKey = context.key();
        returnConnection(SENTINEL_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisConnection<String, byte[]> connection) {
        String poolKey = context.key();
        returnConnection(NORMAL_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisPubSubConnection<String, String> connection) {
        String poolKey = context.key();
        returnConnection(NORMAL_PUB_POOLS.get(poolKey), connection);
    }

    private static <T> void returnConnection(GenericObjectPool<T> pool, T connection) {
        if (pool != null && connection != null) {
            pool.returnObject(connection);
        }
    }

    public static void cleanupContextPool(RedisConnectContext context) {
        String poolKey = context.key();
        cleanupPools(poolKey);
        LettuceUtils.close(context);
    }

    private static void cleanupPools(String specificKey) {
        cleanPoolMap(CLUSTER_POOLS, specificKey);
        cleanPoolMap(CLUSTER_PUB_POOLS, specificKey);
        cleanPoolMap(SENTINEL_POOLS, specificKey);
        cleanPoolMap(NORMAL_POOLS, specificKey);
        cleanPoolMap(NORMAL_PUB_POOLS, specificKey);
    }

    private static <T> void cleanPoolMap(
            Map<String, GenericObjectPool<T>> poolMap,
            String specificKey) {

        GenericObjectPool<T> pool = poolMap.get(specificKey);
        if (pool != null) {
            closeAndRemove(poolMap, specificKey, pool);
        }
    }

    private static <T> void closeAndRemove(
            Map<String, GenericObjectPool<T>> poolMap,
            String key,
            GenericObjectPool<T> pool) {

        try {
            if (!pool.isClosed()) {
                pool.close();
                pool.clear();
                poolMap.remove(key);
                log.info("Closed connection pool: {}", key);
            }
        } catch (Exception e) {
            log.error("Close pool failed: {}", key, e);
        }
    }
}
