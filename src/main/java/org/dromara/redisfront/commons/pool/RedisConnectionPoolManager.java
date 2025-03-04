
package org.dromara.redisfront.commons.pool;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.pubsub.StatefulRedisClusterPubSubConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RedisConnectionPoolManager {
    // 连接池配置参数
    private static final int MAX_TOTAL = 20;
    private static final int MAX_IDLE = 10;
    private static final int MIN_IDLE = 2;
    private static final long MAX_WAIT_MILLIS = 5000;

    // 连接池存储结构
    private static final Map<String, GenericObjectPool<StatefulRedisClusterConnection<String, String>>> CLUSTER_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisClusterPubSubConnection<String, String>>> CLUSTER_PUB_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisSentinelConnection<String, String>>> SENTINEL_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisConnection<String, String>>> NORMAL_POOLS = new ConcurrentHashMap<>();
    private static final Map<String, GenericObjectPool<StatefulRedisPubSubConnection<String, String>>> NORMAL_PUB_POOLS = new ConcurrentHashMap<>();

    public static StatefulRedisClusterPubSubConnection<String, String> getClusterConnectPubSub(RedisConnectContext context) {
        return getConnection(CLUSTER_PUB_POOLS, context, () -> {
            RedisURI uri = LettuceUtils.createRedisURI(context);
            RedisClusterClient client = LettuceUtils.getRedisClusterClient(uri, context);
            return client.connectPubSub();
        });
    }

    public static StatefulRedisClusterConnection<String, String> getClusterConnection(RedisConnectContext context) {
        return getConnection(CLUSTER_POOLS, context, () -> {
            RedisURI uri = LettuceUtils.createRedisURI(context);
            RedisClusterClient client = LettuceUtils.getRedisClusterClient(uri, context);
            return client.connect();
        });
    }

    public static StatefulRedisSentinelConnection<String, String> getSentinelConnection(RedisConnectContext context) {
        return getConnection(SENTINEL_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            return client.connectSentinel();
        });
    }

    public static StatefulRedisConnection<String, String> getConnection(RedisConnectContext context) {
        return getConnection(NORMAL_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            return client.connect();
        });
    }
    public static StatefulRedisPubSubConnection<String, String> getConnectPubSub(RedisConnectContext context) {
        return getConnection(NORMAL_PUB_POOLS, context, () -> {
            RedisClient client = LettuceUtils.getRedisClient(context);
            return client.connectPubSub();
        });
    }

    private static <T> T getConnection(Map<String, GenericObjectPool<T>> poolMap,
                                       RedisConnectContext context,
                                       ConnectionSupplier<T> supplier) {
        String poolKey = context.getId() + "_" + context.getRedisMode();

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
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException("Get connection failed", e);
        }
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisClusterConnection<String, String> connection) {
        String poolKey = context.getId() + "_" + context.getRedisMode();
        returnConnection(CLUSTER_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisSentinelConnection<String, String> connection) {
        String poolKey = context.getId() + "_" + context.getRedisMode();
        returnConnection(SENTINEL_POOLS.get(poolKey), connection);
    }

    public static void closeConnection(RedisConnectContext context, StatefulRedisConnection<String, String> connection) {
        String poolKey = context.getId() + "_" + context.getRedisMode();
        returnConnection(NORMAL_POOLS.get(poolKey), connection);
    }

    private static <T> void returnConnection(GenericObjectPool<T> pool, T connection) {
        if (pool != null && connection != null) {
            pool.returnObject(connection);
        }
    }

    public static void cleanupContextPool(RedisConnectContext context) {
        String poolKey = context.getId() + "_" + context.getRedisMode();
        cleanupPools(poolKey);
    }

    private static void cleanupPools(String specificKey) {
        cleanPoolMap(CLUSTER_POOLS,  specificKey);
        cleanPoolMap(SENTINEL_POOLS, specificKey);
        cleanPoolMap(NORMAL_POOLS,  specificKey);
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
