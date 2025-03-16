
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
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
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

    private static final int MAX_TOTAL = 20;
    private static final int MAX_IDLE = 10;
    private static final int MIN_IDLE = 2;
    private static final long MAX_WAIT_MILLIS = 5000;

    private static final Map<String, GenericObjectPool<StatefulRedisClusterConnection<String, String>>> CLUSTER_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisClusterPubSubConnection<String, String>>> CLUSTER_PUB_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisSentinelConnection<String, String>>> SENTINEL_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisConnection<String, String>>> NORMAL_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisPubSubConnection<String, String>>> NORMAL_PUB_POOLS = new ConcurrentHashMap<>();

    public static StatefulRedisClusterPubSubConnection<String, String> getClusterConnectPubSub(RedisConnectContext context) {
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

    public static StatefulRedisClusterConnection<String, String> getClusterConnection(RedisConnectContext context) {
        return getConnection(CLUSTER_POOLS, context, () -> {
            RedisURI uri = LettuceUtils.createRedisURI(context);
            RedisClusterClient client = LettuceUtils.getRedisClusterClient(uri, context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connect();
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

    public static StatefulRedisConnection<String, String> getConnection(RedisConnectContext context) {
        return getConnection(NORMAL_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            client.addListener(new CommandListener() {
                @Override
                public void commandStarted(CommandStartedEvent event) {
                    publishCommandEvent(event, context);
                }
            });
            return client.connect();
        });
    }

    private static void publishCommandEvent(CommandStartedEvent event, RedisConnectContext context) {
        if (LogStatusHolder.getIgnoredLog() == null) {
            String type = event.getCommand().getType().toString();
            String commandString = event.getCommand().getArgs().toCommandString();
            LogInfo logInfo = new LogInfo();
            logInfo.setIp(context.getHost());
            logInfo.setDate(LocalDateTime.now());
            logInfo.setInfo(type + " " + commandString);
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
            GenericObjectPool<T> pool = poolMap.computeIfAbsent(poolKey, _ -> {
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

    public static void closeConnection(RedisConnectContext context, StatefulRedisClusterConnection<String, String> connection) {
        String poolKey = context.key();
        returnConnection(CLUSTER_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisSentinelConnection<String, String> connection) {
        String poolKey = context.key();
        returnConnection(SENTINEL_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisConnection<String, String> connection) {
        String poolKey = context.key();
        returnConnection(NORMAL_POOLS.get(poolKey), connection);
    }

    private static <T> void returnConnection(GenericObjectPool<T> pool, T connection) {
        if (pool != null && connection != null) {
            pool.returnObject(connection);
        }
    }

    public static void cleanupContextPool(RedisConnectContext context) {
        String poolKey = context.key();
        cleanupPools(poolKey);
    }

    private static void cleanupPools(String specificKey) {
        cleanPoolMap(CLUSTER_POOLS, specificKey);
        cleanPoolMap(SENTINEL_POOLS, specificKey);
        cleanPoolMap(NORMAL_POOLS, specificKey);
    }

    private static <T> void cleanPoolMap(
            Map<String, GenericObjectPool<T>> poolMap,
            String specificKey) {

        poolMap.forEach((key, pool) -> {
            if (key.equals(specificKey)) {
                closeAndRemove(poolMap, key, pool);
            }
        });
    }

    private static <T> void closeAndRemove(
            Map<String, GenericObjectPool<T>> poolMap,
            String key,
            GenericObjectPool<T> pool) {

        try {
            if (!pool.isClosed()) {
                pool.close();
                poolMap.remove(key);
                log.info("Closed connection pool: {}", key);
            }
        } catch (Exception e) {
            log.error("Close pool failed: {}", key, e);
        }
    }
}
