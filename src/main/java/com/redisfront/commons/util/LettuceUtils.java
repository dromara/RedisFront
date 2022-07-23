package com.redisfront.commons.util;

import cn.hutool.core.util.RandomUtil;
import com.jcraft.jsch.Session;
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

    private static RedisClusterClient getRedisClusterClient(RedisURI redisURI) {
        var clusterClient = RedisClusterClient.create(redisURI);
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

    public static void clusterRun(ConnectInfo connectInfo, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = getRedisClusterClient(redisURI);
        Session session = JschUtils.openSession(connectInfo, clusterClient);
        try (var connection = clusterClient.connect()) {
            consumer.accept(connection.sync());
        } finally {
            clusterClient.shutdown();
            JschUtils.closeSession(session);
        }
    }

    public static <T> T clusterExec(ConnectInfo connectInfo, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = getRedisClusterClient(redisURI);
        Session session = JschUtils.openSession(connectInfo, clusterClient);
        try (var connection = clusterClient.connect()) {
            return function.apply(connection.sync());
        } finally {
            clusterClient.shutdown();
            JschUtils.closeSession(session);
        }
    }

    public static void sentinelRun(ConnectInfo connectInfo, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        Session session = JschUtils.openSession(connectInfo);
        try (var connection = redisClient.connectSentinel()) {
            consumer.accept(connection.sync());
        } finally {
            redisClient.shutdown();
            JschUtils.closeSession(session);
        }
    }

    public static <T> T sentinelExec(ConnectInfo connectInfo, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        Session session = JschUtils.openSession(connectInfo);
        try (var connection = redisClient.connectSentinel()) {
            return function.apply(connection.sync());
        } finally {
            redisClient.shutdown();
            JschUtils.closeSession(session);
        }
    }

    public static void run(ConnectInfo connectInfo, Consumer<RedisCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        Session session = JschUtils.openSession(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        try (var connection = redisClient.connect()) {
            consumer.accept(connection.sync());
        } finally {
            redisClient.shutdown();
            JschUtils.closeSession(session);
        }
    }


    public static <T> T exec(ConnectInfo connectInfo, Function<RedisCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        Session session = JschUtils.openSession(connectInfo);
        try (var connection = redisClient.connect()) {
            return function.apply(connection.sync());
        } finally {
            redisClient.shutdown();
            JschUtils.closeSession(session);
        }
    }


    public static RedisURI getRedisURI(ConnectInfo connectInfo) {
        Integer port = connectInfo.port();
        if (Fn.isNotNull(connectInfo.sshConfig())) {
            port = RandomUtil.randomInt(32768, 61000);
        }
        connectInfo.setLocalPort(port);
        var redisURI = RedisURI.builder()
                .withHost(connectInfo.host())
                .withPort(port)
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
