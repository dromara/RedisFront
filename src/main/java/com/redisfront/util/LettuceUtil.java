package com.redisfront.util;

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
public class LettuceUtil {

    private LettuceUtil() {
    }

    public static void clusterRun(ConnectInfo connectInfo, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = RedisClusterClient.create(redisURI);
        clusterClient.getPartitions().getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(connectInfo.host()));
        try (var connection = clusterClient.connect()) {
            clusterClient
                    .setOptions(ClusterClientOptions.builder()
                            .topologyRefreshOptions(ClusterTopologyRefreshOptions.builder()
                                    .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                                    .enablePeriodicRefresh(Duration.ofMinutes(30))
                                    .build())
                            .build());
            consumer.accept(connection.sync());
        } finally {
            clusterClient.shutdown();
        }
    }

    public static <T> T clusterExec(ConnectInfo connectInfo, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = RedisClusterClient.create(redisURI);
        clusterClient.getPartitions().getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(connectInfo.host()));
        try (var connection = clusterClient.connect()) {
            return function.apply(connection.sync());
        } finally {
            clusterClient.shutdown();
        }
    }

    public static void sentinelRun(ConnectInfo connectInfo, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        try (var connection = redisClient.connectSentinel()) {
            consumer.accept(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }

    public static <T> T sentinelExec(ConnectInfo connectInfo, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        try (var connection = redisClient.connectSentinel()) {
            return function.apply(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }

    public static void run(ConnectInfo connectInfo, Consumer<RedisCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        try (var connection = redisClient.connect()) {
            consumer.accept(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }


    public static <T> T exec(ConnectInfo connectInfo, Function<RedisCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        try (var connection = redisClient.connect()) {
            return function.apply(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }


    public static RedisURI getRedisURI(ConnectInfo connectInfo) {
        var redisURI = RedisURI.builder()
                .withHost(connectInfo.host())
                .withPort(connectInfo.port())
                .withDatabase(connectInfo.database())
                .withSsl(connectInfo.ssl())
                .build();
        if (FunUtil.isNotEmpty(connectInfo.user())) {
            redisURI.setUsername(connectInfo.user());
        }
        if (FunUtil.isNotEmpty(connectInfo.password())) {
            redisURI.setPassword(connectInfo.password().toCharArray());
        }
        return redisURI;
    }


}
