package org.dromara.redisfront.commons.utils;

import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.models.partitions.Partitions;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import io.netty.util.internal.StringUtil;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
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

    public synchronized static Partitions getRedisClusterPartitions(RedisConnectContext redisConnectContext) {
        var redisURI = getRedisURI(redisConnectContext);
        try (var clusterClient = RedisClusterClient.create(redisURI)) {
            return clusterClient.getPartitions();
        }
    }

    public synchronized static RedisClusterClient getRedisClusterClient(RedisURI redisURI, RedisConnectContext redisConnectContext) {
        var clusterClient = RedisClusterClient.create(redisURI);
        if (!RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
            clusterClient.getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(redisConnectContext.getHost()));
        }
        configureOptions(clusterClient, redisConnectContext);
        return clusterClient;
    }

    public synchronized static void clusterRun(RedisConnectContext redisConnectContext, Consumer<RedisAdvancedClusterCommands<String, String>> consumer) {
        var redisURI = getRedisURI(redisConnectContext);
        try (var clusterClient = getRedisClusterClient(redisURI, redisConnectContext);
             var connection = clusterClient.connect()) {
            consumer.accept(connection.sync());
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public synchronized static <T> T clusterExec(RedisConnectContext redisConnectContext, Function<RedisAdvancedClusterCommands<String, String>, T> function) {
        var redisURI = getRedisURI(redisConnectContext);
        ;
        try (var clusterClient = getRedisClusterClient(redisURI, redisConnectContext);
             var connection = clusterClient.connect()) {
            return function.apply(connection.sync());
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }


    public synchronized static void sentinelRun(RedisConnectContext redisConnectContext, Consumer<RedisSentinelCommands<String, String>> consumer) {
        var redisURI = getRedisURI(redisConnectContext);
        try (var redisClient = io.lettuce.core.RedisClient.create(redisURI);
             var connection = redisClient.connectSentinel()) {
            consumer.accept(connection.sync());
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public synchronized static <T> T sentinelExec(RedisConnectContext redisConnectContext, Function<RedisSentinelCommands<String, String>, T> function) {
        var redisURI = getRedisURI(redisConnectContext);
        try (var redisClient = io.lettuce.core.RedisClient.create(redisURI);
             var connection = redisClient.connectSentinel()) {
            return function.apply(connection.sync());
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public synchronized static void run(RedisConnectContext redisConnectContext, Consumer<RedisCommands<String, String>> consumer) {
        ;
        try (var redisClient = getRedisClient(redisConnectContext);
             var connection = redisClient.connect()) {
            consumer.accept(connection.sync());
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public synchronized static RedisClient getRedisClient(RedisConnectContext redisConnectContext) {
        var redisURI = getRedisURI(redisConnectContext);
        var redisClient = RedisClient.create(redisURI);
        configureOptions(redisClient, redisConnectContext);
        return redisClient;
    }

    public synchronized static <T> T exec(RedisConnectContext redisConnectContext, Function<RedisCommands<String, String>, T> function) {
        try (var redisClient = getRedisClient(redisConnectContext);
             var connection = redisClient.connect()) {
            return function.apply(connection.sync());
        } catch (Exception exception) {
            log.error("redis连接失败！", exception);
            throw exception;
        }
    }

    public synchronized static RedisURI getRedisURI(RedisConnectContext redisConnectContext) {
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
                .enableAllAdaptiveRefreshTriggers()
                .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(10))
                .build();
        var clusterClientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
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
}
