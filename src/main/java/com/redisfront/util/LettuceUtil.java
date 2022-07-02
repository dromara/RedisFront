package com.redisfront.util;

import com.redisfront.model.ConnectInfo;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;

import java.util.function.Consumer;
import java.util.function.Function;


/**
 * LettuceUtil
 *
 * @author Jin
 */
public class LettuceUtil {

    public static void run(ConnectInfo connectInfo, Consumer<RedisCommands<String, String>> consumer) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect()) {
            consumer.accept(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }

    public static <K, V> void run(ConnectInfo connectInfo, RedisCodec<K, V> codec, Consumer<RedisCommands<K, V>> consumer) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect(codec)) {
            consumer.accept(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }

    public static void runAsync(ConnectInfo connectInfo, Consumer<RedisAsyncCommands<String, String>> consumer) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect()) {
            consumer.accept(connection.async());
        } finally {
            redisClient.shutdown();
        }
    }

    public static <K, V> void runAsync(ConnectInfo connectInfo, RedisCodec<K, V> codec, Consumer<RedisAsyncCommands<K, V>> consumer) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect(codec)) {
            consumer.accept(connection.async());
        } finally {
            redisClient.shutdown();
        }
    }

    public static <T> T exec(ConnectInfo connectInfo, Function<RedisCommands<String, String>, T> function) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect()) {
            return function.apply(connection.sync());
        }finally {
            redisClient.shutdown();
        }
    }

    public static <T> T execAsync(ConnectInfo connectInfo, Function<RedisAsyncCommands<String, String>, T> function) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect()) {
            return function.apply(connection.async());
        } finally {
            redisClient.shutdown();
        }
    }


    public static <K, V, T> T exec(ConnectInfo connectInfo, RedisCodec<K, V> codec, Function<RedisCommands<K, V>, T> function) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect(codec)) {
            return function.apply(connection.sync());
        } finally {
            redisClient.shutdown();
        }
    }

    public static <K, V, T> T execAsync(ConnectInfo connectInfo, RedisCodec<K, V> codec, Function<RedisAsyncCommands<K, V>, T> function) {
        var redisClient = getRedisClient(connectInfo);
        try (var connection = redisClient.connect(codec)) {
            return function.apply(connection.async());
        } finally {
            redisClient.shutdown();
        }
    }

    public static RedisClient getRedisClient(ConnectInfo connectInfo) {
        RedisURI redisURI = RedisURI.builder()
                .withHost(connectInfo.host())
                .withPort(connectInfo.port())
                .withDatabase(connectInfo.database())
                .withSsl(connectInfo.ssl())
                .build();
        if (Fn.isNotEmpty(connectInfo.user())) {
            redisURI.setUsername(connectInfo.user());
        }
        if (Fn.isNotEmpty(connectInfo.password())) {
            redisURI.setPassword(connectInfo.password().toCharArray());
        }
        return RedisClient.create(redisURI);
    }
}
