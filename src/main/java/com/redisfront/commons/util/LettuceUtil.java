package com.redisfront.commons.util;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
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


    private static Session getSession(ConnectInfo connectInfo) {
        if (Fn.isNotEmpty(connectInfo.sshConfig().privateKeyPath()) && Fn.isNotEmpty(connectInfo.sshConfig().password())) {
            return JschUtil.createSession(connectInfo.host(), connectInfo.port(), connectInfo.user(), connectInfo.sshConfig().privateKeyPath(), connectInfo.sshConfig().password().getBytes());
        } else if (Fn.isNotEmpty(connectInfo.sshConfig().privateKeyPath())) {
            return JschUtil.createSession(connectInfo.host(), connectInfo.port(), connectInfo.user(), connectInfo.sshConfig().privateKeyPath(), null);
        } else {
            return JschUtil.createSession(connectInfo.host(), connectInfo.port(), connectInfo.user(), connectInfo.password());
        }
    }


    private static Session getJschSession(ConnectInfo connectInfo, RedisClusterClient clusterClient) {
        if (Fn.isNotNull(connectInfo.sshConfig())) {
            Session session = getSession(connectInfo);
            try {
                session.connect();
                for (RedisClusterNode partition : clusterClient.getPartitions()) {
                    JschUtil.bindPort(session, connectInfo.host(), partition.getUri().getPort(), partition.getUri().getPort());
                }
                return session;
            } catch (JSchException e) {
                throw new RedisFrontException(e, true);
            }
        } else {
            clusterClient.getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(connectInfo.host()));
            return null;
        }
    }

    private static Session getJschSession(ConnectInfo connectInfo) {
        if (Fn.isNotNull(connectInfo.sshConfig())) {
            Session session = getSession(connectInfo);
            try {
                session.connect();
                return session;
            } catch (JSchException e) {
                throw new RedisFrontException(e, true);
            }
        } else {
            return null;
        }
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
        Session session = getJschSession(connectInfo, clusterClient);
        try (var connection = clusterClient.connect()) {
            consumer.accept(connection.sync());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            clusterClient.shutdown();
        }
    }

    public static <T> T clusterExec(ConnectInfo connectInfo, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = getRedisClusterClient(redisURI);
        Session session = getJschSession(connectInfo, clusterClient);
        try (var connection = clusterClient.connect()) {
            return function.apply(connection.sync());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            clusterClient.shutdown();
        }
    }

    public static void sentinelRun(ConnectInfo connectInfo, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        Session session = getJschSession(connectInfo);
        try (var connection = redisClient.connectSentinel()) {
            consumer.accept(connection.sync());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            redisClient.shutdown();
        }
    }

    public static <T> T sentinelExec(ConnectInfo connectInfo, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        Session session = getJschSession(connectInfo);
        try (var connection = redisClient.connectSentinel()) {
            return function.apply(connection.sync());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            redisClient.shutdown();
        }
    }

    public static void run(ConnectInfo connectInfo, Consumer<RedisCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        Session session = getJschSession(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        try (var connection = redisClient.connect()) {
            consumer.accept(connection.sync());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            redisClient.shutdown();
        }
    }


    public static <T> T exec(ConnectInfo connectInfo, Function<RedisCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        Session session = getJschSession(connectInfo);
        try (var connection = redisClient.connect()) {
            return function.apply(connection.sync());
        } finally {
            if (session != null) {
                session.disconnect();
            }
            redisClient.shutdown();
        }
    }


    public static RedisURI getRedisURI(ConnectInfo connectInfo) {
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
