package com.redisfront.commons.util;

import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * LettuceUtil
 *
 * @author Jin
 */
public class LettuceUtils {

    private LettuceUtils() {
    }

    private synchronized static RedisClusterClient getRedisClusterClient(RedisURI redisURI) {
        var clusterClient = RedisClusterClient.create(redisURI);
        clusterClient.setDefaultTimeout(Duration.ofMinutes(1));
        var clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                .enablePeriodicRefresh(Duration.ofMinutes(30))
                .build();
        var clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();
        clusterClient.setOptions(clusterClientOptions);
        return clusterClient;
    }

    public synchronized static void clusterRun(ConnectInfo connectInfo, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = getRedisClusterClient(redisURI);
        clusterClient.setDefaultTimeout(Duration.ofMinutes(1));
        try {
            JschUtils.openSession(connectInfo, clusterClient);
            try (var connection = clusterClient.connect()) {
                consumer.accept(connection.sync());
            } finally {
                clusterClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            clusterClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static <T> T clusterExec(ConnectInfo connectInfo, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = getRedisClusterClient(redisURI);
        clusterClient.setDefaultTimeout(Duration.ofMinutes(1));
        try {
            JschUtils.openSession(connectInfo, clusterClient);
            try (var connection = clusterClient.connect()) {
                return function.apply(connection.sync());
            } finally {
                clusterClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            clusterClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static void sentinelRun(ConnectInfo connectInfo, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        redisClient.setDefaultTimeout(Duration.ofMinutes(1));
        try {
            JschUtils.openSession(connectInfo);
            try (var connection = redisClient.connectSentinel()) {
                consumer.accept(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static <T> T sentinelExec(ConnectInfo connectInfo, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        redisClient.setDefaultTimeout(Duration.ofMinutes(1));
        try {
            JschUtils.openSession(connectInfo);
            try (var connection = redisClient.connectSentinel()) {
                return function.apply(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static void run(ConnectInfo connectInfo, Consumer<RedisCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        redisClient.setDefaultTimeout(Duration.ofMinutes(1));
        try {
            JschUtils.openSession(connectInfo);
            try (var connection = redisClient.connect()) {
                consumer.accept(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }


    public synchronized static <T> T exec(ConnectInfo connectInfo, Function<RedisCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        redisClient.setDefaultTimeout(Duration.ofMinutes(1));
        try {
            JschUtils.openSession(connectInfo);
            try (var connection = redisClient.connect()) {
                return function.apply(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }


    public synchronized static RedisURI getRedisURI(ConnectInfo connectInfo) {
        var redisURI = RedisURI.builder()
                .withHost(connectInfo.host())
                .withPort(connectInfo.port())
                .withSsl(connectInfo.ssl())
                .withDatabase(connectInfo.database())
                .build();
        if (Fn.isNotEmpty(connectInfo.user())) {
            redisURI.setUsername(connectInfo.user());
        }
        if (Fn.isNotEmpty(connectInfo.password())) {
            redisURI.setPassword(connectInfo.password().toCharArray());
        }
        return redisURI;
    }

}
