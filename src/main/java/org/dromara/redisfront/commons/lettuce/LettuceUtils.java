package org.dromara.redisfront.commons.lettuce;

import cn.hutool.core.collection.CollUtil;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.Partitions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import io.netty.util.internal.StringUtil;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * LettuceUtil
 *
 * @author Jin
 */
public class LettuceUtils {

    private static final Logger log = LoggerFactory.getLogger(LettuceUtils.class);


    private LettuceUtils() {
    }

    public static RedisURI createRedisURI(RedisConnectContext redisConnectContext) {
        String host = "";
        String password = "";
        if (!StringUtil.isNullOrEmpty(redisConnectContext.getHost())) {
            host = redisConnectContext.getHost().replace("\uFEFF", "");
        }
        if (!StringUtil.isNullOrEmpty(redisConnectContext.getPassword())) {
            password = redisConnectContext.getPassword().replace("\uFEFF", "");
        }

        var redisURI = RedisURI.builder()
                .withHost(RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH) ? redisConnectContext.getLocalHost() : host)
                .withPort(RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH) ? redisConnectContext.getLocalPort() : redisConnectContext.getPort())
                .withSsl(redisConnectContext.getEnableSsl())
                .withDatabase(redisConnectContext.getDatabase())
                .withTimeout(Duration.ofMillis(redisConnectContext.getSetting().getRedisTimeout()))
                .build();

        if (RedisFrontUtils.isNotEmpty(redisConnectContext.getUsername()) && RedisFrontUtils.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(redisConnectContext.getUsername(), password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        } else if (RedisFrontUtils.isNotEmpty(password)) {
            var staticCredentialsProvider = new StaticCredentialsProvider(null, password.toCharArray());
            redisURI.setCredentialsProvider(staticCredentialsProvider);
        }
        return redisURI;
    }

    private static void configureOptions(RedisClusterClient redisClient, RedisConnectContext redisConnectContext) {
        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofMinutes(5))
                .enableAllAdaptiveRefreshTriggers()
                .build();
        var clusterClientOptions = ClusterClientOptions.builder()
                .autoReconnect(true)
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .socketOptions(SocketOptions.builder()
                        .keepAlive(true)
                        .tcpNoDelay(true)
                        .build())
                .timeoutOptions(TimeoutOptions.builder()
                        .fixedTimeout(Duration.ofSeconds(30))
                        .build())
                .build();
        if (redisConnectContext.getEnableSsl()) {
            if (redisConnectContext.getSslInfo() != null && RedisFrontUtils.isNotEmpty(redisConnectContext.getSslInfo().getPassword()) || RedisFrontUtils.isNotEmpty(redisConnectContext.getSslInfo().getPublicKeyFilePath())) {
                clusterClientOptions = clusterClientOptions
                        .mutate()
                        .sslOptions(SslOptions.builder()
                                .jdkSslProvider()
                                .truststore(new File(redisConnectContext.getSslInfo().getPublicKeyFilePath()), redisConnectContext.getSslInfo().getPassword())
                                .build())
                        .build();
            }
        }
        redisClient.setOptions(clusterClientOptions);
    }

    private static void configureOptions(RedisClient redisClient, RedisConnectContext redisConnectContext) {
        if (redisConnectContext.getEnableSsl()) {
            if (RedisFrontUtils.isNotEmpty(redisConnectContext.getSslInfo().getPassword()) || RedisFrontUtils.isNotEmpty(redisConnectContext.getSslInfo().getPublicKeyFilePath())) {
                var sslOptions = SslOptions.builder()
                        .jdkSslProvider()
                        .truststore(new File(redisConnectContext.getSslInfo().getPublicKeyFilePath()), redisConnectContext.getSslInfo().getPassword())
                        .build();
                redisClient.setOptions(ClientOptions.builder().sslOptions(sslOptions).build());
            }
        }
    }


    public static Set<Integer> getRedisClusterPartitionPorts(RedisConnectContext redisConnectContext) {
        Set<Integer> ports = new HashSet<>();
        var redisURI = createRedisURI(redisConnectContext);
        try (var clusterClient = RedisClusterClient.create(redisURI)) {
            clusterClient.getPartitions().forEach(redisClusterNode -> ports.add(redisClusterNode.getUri().getPort()));
        }
        return ports;
    }

    public static RedisClusterClient getRedisClusterClient(RedisURI redisURI, RedisConnectContext redisConnectContext) {
        AddressMappingResolver mappingResolver = new AddressMappingResolver(redisConnectContext);
        var clusterClient = RedisClusterClient.create(ClientResources.builder().socketAddressResolver(mappingResolver).build(), redisURI);
        configureOptions(clusterClient, redisConnectContext);
        return clusterClient;
    }

    public static RedisClient getRedisClient(RedisConnectContext redisConnectContext) {
        var redisURI = createRedisURI(redisConnectContext);
        var redisClient = RedisClient.create(redisURI);
        configureOptions(redisClient, redisConnectContext);
        return redisClient;
    }

    public static void clusterRun(RedisConnectContext redisConnectContext, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        try {
            StatefulRedisClusterConnection<String, String> connection = RedisConnectionPoolManager.getClusterConnection(redisConnectContext);
            consumer.accept(connection.sync());
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public static <T> T clusterExec(RedisConnectContext redisConnectContext, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        try {
            StatefulRedisClusterConnection<String, String> connection = RedisConnectionPoolManager.getClusterConnection(redisConnectContext);
            T apply = function.apply(connection.sync());
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
            return apply;
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }


    public static void sentinelRun(RedisConnectContext redisConnectContext, Consumer<RedisSentinelCommands<String, String>> consumer) {
        try {
            StatefulRedisSentinelConnection<String, String> connection = RedisConnectionPoolManager.getSentinelConnection(redisConnectContext);
            consumer.accept(connection.sync());
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public static <T> T sentinelExec(RedisConnectContext redisConnectContext, Function<RedisSentinelCommands<String, String>, T> function) {
        try {
            StatefulRedisSentinelConnection<String, String> connection = RedisConnectionPoolManager.getSentinelConnection(redisConnectContext);
            T apply = function.apply(connection.sync());
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
            return apply;
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public static void run(RedisConnectContext redisConnectContext, Consumer<RedisCommands<String, String>> consumer) {
        try {
            StatefulRedisConnection<String, String> connection = RedisConnectionPoolManager.getConnection(redisConnectContext);
            consumer.accept(connection.sync());
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public static <T> T exec(RedisConnectContext redisConnectContext, Function<RedisCommands<String, String>, T> function) {
        try {
            StatefulRedisConnection<String, String> connection = RedisConnectionPoolManager.getConnection(redisConnectContext);
            T apply = function.apply(connection.sync());
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
            return apply;
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

}
