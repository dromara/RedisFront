package com.redisfront.commons.util;

import cn.hutool.core.util.RandomUtil;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.StaticCredentialsProvider;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import io.netty.util.internal.StringUtil;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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

    private synchronized static RedisClusterClient getRedisClusterClient(RedisURI redisURI, ConnectInfo connectInfo) {
        var clusterClient = RedisClusterClient.create(redisURI);
        var clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                .enablePeriodicRefresh(Duration.ofMinutes(30))
                .build();
        var clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();
        clusterClient.setOptions(clusterClientOptions);
        var isSSH = connectInfo.connectMode().equals(Enum.Connect.SSH);
        if (isSSH) {
            Map<Integer, Integer> clusterTempPort = new HashMap<>();
            for (RedisClusterNode partition : clusterClient.getPartitions()) {
                var remotePort = partition.getUri().getPort();
                clusterTempPort.put(remotePort, RandomUtil.randomInt(32768, 65535));
            }
            connectInfo.setClusterLocalPort(clusterTempPort);
        }
        return clusterClient;
    }

    public synchronized static void clusterRun(ConnectInfo connectInfo, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectInfo);
        var clusterClient = getRedisClusterClient(redisURI, connectInfo);
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
        var clusterClient = getRedisClusterClient(redisURI, connectInfo);
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
        var isSSH = connectInfo.connectMode().equals(Enum.Connect.SSH);
        if (isSSH) {
            connectInfo.setLocalHost("127.0.0.1");
            connectInfo.setLocalPort(RandomUtil.randomInt(32768, 65535));
        }
        String host = "";
        String password = "";
        if (!StringUtil.isNullOrEmpty(connectInfo.host())) {
            host = connectInfo.host().replace("\uFEFF", "");
        }
        if (!StringUtil.isNullOrEmpty(connectInfo.password())) {
            password = connectInfo.password().replace("\uFEFF", "");
        }

        var redisURI = RedisURI.builder()
                .withHost(isSSH ? connectInfo.getLocalHost() : host)
                .withPort(isSSH ? connectInfo.getLocalPort() : connectInfo.port())
                .withSsl(connectInfo.ssl())
                .withDatabase(connectInfo.database())
                .withTimeout(Duration.ofMinutes(1))
                .build();

        if (Fn.isNotEmpty(connectInfo.user()) && Fn.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(connectInfo.user(), password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        } else if (Fn.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(null, password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        }
        return redisURI;
    }

}
