package com.redisfront.commons.util;

import cn.hutool.core.util.RandomUtil;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.cluster.pubsub.api.async.RedisClusterPubSubAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import io.netty.util.internal.StringUtil;

import java.io.File;
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

    public synchronized static RedisClusterClient getRedisClusterClient(RedisURI redisURI, ConnectInfo connectInfo) {
        var clusterClient = RedisClusterClient.create(redisURI);
        var clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                .enablePeriodicRefresh(Duration.ofMinutes(30))
                .build();
        var clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();

        if (connectInfo.ssl()) {
            if (Fn.isNotEmpty(connectInfo.sslConfig().getPassword()) || Fn.isNotEmpty(connectInfo.sslConfig().getPublicKeyFilePath())) {
                clientOptions = clientOptions
                        .mutate()
                        .sslOptions(SslOptions.builder()
                                .jdkSslProvider()
                                .truststore(new File(connectInfo.sslConfig().getPublicKeyFilePath()), connectInfo.sslConfig().getPassword())
                                .build())
                        .build();
            }
        }

        clusterClient.setOptions(clientOptions);
        if (Fn.equal(connectInfo.connectMode(), Enum.Connect.SSH)) {
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
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
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
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
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
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
        if (connectInfo.ssl()) {
            if (Fn.isNotEmpty(connectInfo.sslConfig().getPassword()) || Fn.isNotEmpty(connectInfo.sslConfig().getPublicKeyFilePath())) {
                var sslOptions = SslOptions.builder()
                        .jdkSslProvider()
                        .truststore(new File(connectInfo.sslConfig().getPublicKeyFilePath()), connectInfo.sslConfig().getPassword())
                        .build();
                redisClient.setOptions(ClientOptions.builder().sslOptions(sslOptions).build());
            }
        }
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

    public synchronized static RedisClient getRedisClient(ConnectInfo connectInfo) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        if (connectInfo.ssl()) {
            if (Fn.isNotEmpty(connectInfo.sslConfig().getPassword()) || Fn.isNotEmpty(connectInfo.sslConfig().getPublicKeyFilePath())) {
                var sslOptions = SslOptions.builder()
                        .jdkSslProvider()
                        .truststore(new File(connectInfo.sslConfig().getPublicKeyFilePath()), connectInfo.sslConfig().getPassword())
                        .build();
                redisClient.setOptions(ClientOptions.builder().sslOptions(sslOptions).build());
            }
        }
        return redisClient;
    }

    public synchronized static <T> T exec(ConnectInfo connectInfo, Function<RedisCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectInfo);
        var redisClient = RedisClient.create(redisURI);
        if (connectInfo.ssl()) {
            if (Fn.isNotEmpty(connectInfo.sslConfig().getPassword()) || Fn.isNotEmpty(connectInfo.sslConfig().getPublicKeyFilePath())) {
                var sslOptions = SslOptions.builder()
                        .jdkSslProvider()
                        .truststore(new File(connectInfo.sslConfig().getPublicKeyFilePath()), connectInfo.sslConfig().getPassword())
                        .build();
                redisClient.setOptions(ClientOptions.builder().sslOptions(sslOptions).build());
            }
        }
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
        if (Fn.equal(connectInfo.connectMode(), Enum.Connect.SSH)) {
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
                .withHost(Fn.equal(connectInfo.connectMode(), Enum.Connect.SSH) ? connectInfo.getLocalHost() : host)
                .withPort(Fn.equal(connectInfo.connectMode(), Enum.Connect.SSH) ? connectInfo.getLocalPort() : connectInfo.port())
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
