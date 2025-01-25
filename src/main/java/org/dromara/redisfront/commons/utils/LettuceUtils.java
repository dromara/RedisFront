package org.dromara.redisfront.commons.utils;

import cn.hutool.core.util.RandomUtil;
import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.model.context.ConnectContext;
import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import io.netty.util.internal.StringUtil;
import org.dromara.redisfront.commons.func.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * LettuceUtil
 *
 * @author Jin
 */
public class LettuceUtils {

    private static final Logger log = LoggerFactory.getLogger(LettuceUtils.class);

    private static final HashSet<Integer> portHashSet = new HashSet<>();

    private LettuceUtils() {
    }

    public synchronized static RedisClusterClient getRedisClusterClient(RedisURI redisURI, ConnectContext connectContext) {
        var clusterClient = RedisClusterClient.create(redisURI);
        var clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                .enablePeriodicRefresh(Duration.ofMinutes(30))
                .build();
        var clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();

        if (connectContext.getEnableSsl()) {
            if (Fn.isNotEmpty(connectContext.getSslInfo().getPassword()) || Fn.isNotEmpty(connectContext.getSslInfo().getPublicKeyFilePath())) {
                clientOptions = clientOptions
                        .mutate()
                        .sslOptions(SslOptions.builder()
                                .jdkSslProvider()
                                .truststore(new File(connectContext.getSslInfo().getPublicKeyFilePath()), connectContext.getSslInfo().getPassword())
                                .build())
                        .build();
            }
        }

        clusterClient.setOptions(clientOptions);
        if (Fn.equal(connectContext.getConnectTypeMode(), Enums.ConnectType.SSH)) {
            Map<Integer, Integer> clusterTempPort = new HashMap<>();
            for (RedisClusterNode partition : clusterClient.getPartitions()) {
                var remotePort = partition.getUri().getPort();
                int port = getTempLocalPort();
                clusterTempPort.put(remotePort, port);
            }
            connectContext.setClusterLocalPort(clusterTempPort);
        }
        return clusterClient;
    }

    public synchronized static void clusterRun(ConnectContext connectContext, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectContext);
        var clusterClient = getRedisClusterClient(redisURI, connectContext);
        try {
            JschUtils.openSession(connectContext, clusterClient);
            try (var connection = clusterClient.connect()) {
                consumer.accept(connection.sync());
            } finally {
                clusterClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            clusterClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static <T> T clusterExec(ConnectContext connectContext, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectContext);
        var clusterClient = getRedisClusterClient(redisURI, connectContext);
        try {
            JschUtils.openSession(connectContext, clusterClient);
            try (var connection = clusterClient.connect()) {
                return function.apply(connection.sync());
            } finally {
                clusterClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            clusterClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }


    public synchronized static void sentinelRun(ConnectContext connectContext, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(connectContext);
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
        try {
            JschUtils.openSession(connectContext);
            try (var connection = redisClient.connectSentinel()) {
                consumer.accept(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static <T> T sentinelExec(ConnectContext connectContext, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(connectContext);
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
        try {
            JschUtils.openSession(connectContext);
            try (var connection = redisClient.connectSentinel()) {
                return function.apply(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static void run(ConnectContext connectContext, Consumer<RedisCommands<String, String>> consumer) {
        var redisClient = getRedisClient(connectContext);
        try {
            JschUtils.openSession(connectContext);
            try (var connection = redisClient.connect()) {
                consumer.accept(connection.sync());
            } finally {
                redisClient.shutdown();
                JschUtils.closeSession();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            redisClient.shutdown();
            JschUtils.closeSession();
            throw exception;
        }
    }

    public synchronized static RedisClient getRedisClient(ConnectContext connectContext) {
        var redisURI = getRedisURI(connectContext);
        var redisClient = RedisClient.create(redisURI);
        if (connectContext.getEnableSsl()) {
            if (Fn.isNotEmpty(connectContext.getSslInfo().getPassword()) || Fn.isNotEmpty(connectContext.getSslInfo().getPublicKeyFilePath())) {
                var sslOptions = SslOptions.builder()
                        .jdkSslProvider()
                        .truststore(new File(connectContext.getSslInfo().getPublicKeyFilePath()), connectContext.getSslInfo().getPassword())
                        .build();
                redisClient.setOptions(ClientOptions.builder().sslOptions(sslOptions).build());
            }
        }
        return redisClient;
    }

    public synchronized static <T> T exec(ConnectContext connectContext, Function<RedisCommands<String, String>, T> function) {
        var redisClient = getRedisClient(connectContext);
        try {
            JschUtils.openSession(connectContext);
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

    public synchronized static RedisURI getRedisURI(ConnectContext connectContext) {
        if (Fn.equal(connectContext.getConnectTypeMode(), Enums.ConnectType.SSH)) {
            connectContext.setLocalHost("127.0.0.1");
            int port = getTempLocalPort();
            connectContext.setLocalPort(port);
        }
        String host = "";
        String password = "";
        if (!StringUtil.isNullOrEmpty(connectContext.getHost())) {
            host = connectContext.getHost().replace("\uFEFF", "");
        }
        if (!StringUtil.isNullOrEmpty(connectContext.getPassword())) {
            password = connectContext.getPassword().replace("\uFEFF", "");
        }

        var redisURI = RedisURI.builder()
                .withHost(Fn.equal(connectContext.getConnectTypeMode(), Enums.ConnectType.SSH) ? connectContext.getLocalHost() : host)
                .withPort(Fn.equal(connectContext.getConnectTypeMode(), Enums.ConnectType.SSH) ? connectContext.getLocalPort() : connectContext.getPort())
                .withSsl(connectContext.getEnableSsl())
                .withDatabase(connectContext.getDatabase())
                .withTimeout(Duration.ofMillis(1000))
                .build();

        if (Fn.isNotEmpty(connectContext.getUsername()) && Fn.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(connectContext.getUsername(), password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        } else if (Fn.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(null, password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        }
        return redisURI;
    }

    private static int getTempLocalPort() {
        int port = RandomUtil.randomInt(32768, 65535);
        if (!portHashSet.contains(port)) {
            portHashSet.add(port);
        } else {
            port = getTempLocalPort();
        }
        return port;
    }

}
