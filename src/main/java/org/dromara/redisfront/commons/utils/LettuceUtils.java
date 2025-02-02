package org.dromara.redisfront.commons.utils;

import cn.hutool.core.util.RandomUtil;
import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import io.netty.util.internal.StringUtil;
import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * LettuceUtil
 *
 * @author Jin
 */
public class LettuceUtils {

    private static final Logger log = LoggerFactory.getLogger(LettuceUtils.class);
    private static final ConcurrentSkipListSet<Integer> PORT_SET = new ConcurrentSkipListSet<>();
    private static final int MIN_PORT = 32768;
    private static final int MAX_PORT = 65535;
    private static final Duration TIMEOUT = Duration.ofMillis(3000);

    private LettuceUtils() {
    }

    public synchronized static RedisClusterClient getRedisClusterClient(RedisURI redisURI, RedisConnectContext redisConnectContext) {
        var clusterClient = RedisClusterClient.create(redisURI);
        if (redisConnectContext.getEnableSsl()) {
            configureSsl(clusterClient, redisConnectContext);
        }
        if (Fn.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
            Map<Integer, Integer> clusterTempPort = new HashMap<>();
            for (RedisClusterNode partition : clusterClient.getPartitions()) {
                var remotePort = partition.getUri().getPort();
                int port = getTempLocalPort();
                clusterTempPort.put(remotePort, port);
            }
            redisConnectContext.setClusterLocalPort(clusterTempPort);
        }
        return clusterClient;
    }

    public synchronized static void clusterRun(RedisConnectContext redisConnectContext, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(redisConnectContext);
        var clusterClient = getRedisClusterClient(redisURI, redisConnectContext);
        try {
            JschUtils.openSession(redisConnectContext, clusterClient);
            try (var connection = clusterClient.connect()) {
                consumer.accept(connection.sync());
            } finally {
                clusterClient.shutdown();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            clusterClient.shutdown();
            JschUtils.closeSession(redisConnectContext);
            removeTmpLocalPort(redisConnectContext);
            throw exception;
        }
    }

    public static void removeTmpLocalPort(RedisConnectContext redisConnectContext) {
        if (redisConnectContext.getRedisMode().equals(RedisMode.CLUSTER)) {
            redisConnectContext.getClusterLocalPort().forEach((_, v) -> PORT_SET.remove(v));
        } else {
            PORT_SET.remove(redisConnectContext.getLocalPort());
        }
    }

    public synchronized static <T> T clusterExec(RedisConnectContext redisConnectContext, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(redisConnectContext);
        var clusterClient = getRedisClusterClient(redisURI, redisConnectContext);
        try {
            JschUtils.openSession(redisConnectContext, clusterClient);
            try (var connection = clusterClient.connect()) {
                return function.apply(connection.sync());
            } finally {
                clusterClient.shutdown();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            clusterClient.shutdown();
            JschUtils.closeSession(redisConnectContext);
            removeTmpLocalPort(redisConnectContext);
            throw exception;
        }
    }


    public synchronized static void sentinelRun(RedisConnectContext redisConnectContext, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(redisConnectContext);
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
        try {
            JschUtils.openSession(redisConnectContext);
            try (var connection = redisClient.connectSentinel()) {
                consumer.accept(connection.sync());
            } finally {
                redisClient.shutdown();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            redisClient.shutdown();
            JschUtils.closeSession(redisConnectContext);
            removeTmpLocalPort(redisConnectContext);
            throw exception;
        }
    }

    public synchronized static <T> T sentinelExec(RedisConnectContext redisConnectContext, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(redisConnectContext);
        var redisClient = io.lettuce.core.RedisClient.create(redisURI);
        try {
            JschUtils.openSession(redisConnectContext);
            try (var connection = redisClient.connectSentinel()) {
                return function.apply(connection.sync());
            } finally {
                redisClient.shutdown();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            redisClient.shutdown();
            JschUtils.closeSession(redisConnectContext);
            removeTmpLocalPort(redisConnectContext);
            throw exception;
        }
    }

    public synchronized static void run(RedisConnectContext redisConnectContext, Consumer<RedisCommands<String, String>> consumer) {
        var redisClient = getRedisClient(redisConnectContext);
        try {
            JschUtils.openSession(redisConnectContext);
            try (var connection = redisClient.connect()) {
                consumer.accept(connection.sync());
            } finally {
                redisClient.shutdown();
            }
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            redisClient.shutdown();
            JschUtils.closeSession(redisConnectContext);
            removeTmpLocalPort(redisConnectContext);
            throw exception;
        }
    }

    public synchronized static RedisClient getRedisClient(RedisConnectContext redisConnectContext) {
        var redisURI = getRedisURI(redisConnectContext);
        var redisClient = RedisClient.create(redisURI);
        if (redisConnectContext.getEnableSsl()) {
            configureSsl(redisClient, redisConnectContext);
        }
        return redisClient;
    }

    public synchronized static <T> T exec(RedisConnectContext redisConnectContext, Function<RedisCommands<String, String>, T> function) {
        var redisClient = getRedisClient(redisConnectContext);
        try {
            JschUtils.openSession(redisConnectContext);
            try (var connection = redisClient.connect()) {
                return function.apply(connection.sync());
            } finally {
                redisClient.shutdown();
            }
        } catch (Exception exception) {
            redisClient.shutdown();
            JschUtils.closeSession(redisConnectContext);
            removeTmpLocalPort(redisConnectContext);
            throw exception;
        }
    }

    public synchronized static RedisURI getRedisURI(RedisConnectContext redisConnectContext) {
        if (Fn.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
            redisConnectContext.setLocalHost("127.0.0.1");
            int port = getTempLocalPort();
            redisConnectContext.setLocalPort(port);
        }
        String host = "";
        String password = "";
        if (!StringUtil.isNullOrEmpty(redisConnectContext.getHost())) {
            host = redisConnectContext.getHost().replace("\uFEFF", "");
        }
        if (!StringUtil.isNullOrEmpty(redisConnectContext.getPassword())) {
            password = redisConnectContext.getPassword().replace("\uFEFF", "");
        }

        var redisURI = RedisURI.builder()
                .withHost(Fn.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH) ? redisConnectContext.getLocalHost() : host)
                .withPort(Fn.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH) ? redisConnectContext.getLocalPort() : redisConnectContext.getPort())
                .withSsl(redisConnectContext.getEnableSsl())
                .withDatabase(redisConnectContext.getDatabase())
                .withTimeout(TIMEOUT)
                .build();

        if (Fn.isNotEmpty(redisConnectContext.getUsername()) && Fn.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(redisConnectContext.getUsername(), password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        } else if (Fn.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(null, password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        }
        return redisURI;
    }

    private static int getTempLocalPort() {
        int port;
        do {
            port = RandomUtil.randomInt(MIN_PORT, MAX_PORT);
        } while (!PORT_SET.add(port));
        return port;
    }

    private static void configureSsl(RedisClusterClient redisClient, RedisConnectContext redisConnectContext) {
        var clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                .enablePeriodicRefresh(Duration.ofMinutes(30))
                .build();
        var clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();
        if (Fn.isNotEmpty(redisConnectContext.getSslInfo().getPassword()) || Fn.isNotEmpty(redisConnectContext.getSslInfo().getPublicKeyFilePath())) {
            clientOptions = clientOptions
                    .mutate()
                    .sslOptions(SslOptions.builder()
                            .jdkSslProvider()
                            .truststore(new File(redisConnectContext.getSslInfo().getPublicKeyFilePath()), redisConnectContext.getSslInfo().getPassword())
                            .build())
                    .build();
        }
        redisClient.setOptions(clientOptions);
    }

    private static void configureSsl(RedisClient redisClient, RedisConnectContext redisConnectContext) {
        if (Fn.isNotEmpty(redisConnectContext.getSslInfo().getPassword()) || Fn.isNotEmpty(redisConnectContext.getSslInfo().getPublicKeyFilePath())) {
            var sslOptions = SslOptions.builder()
                    .jdkSslProvider()
                    .truststore(new File(redisConnectContext.getSslInfo().getPublicKeyFilePath()), redisConnectContext.getSslInfo().getPassword())
                    .build();
            redisClient.setOptions(ClientOptions.builder().sslOptions(sslOptions).build());
        }
    }
}
