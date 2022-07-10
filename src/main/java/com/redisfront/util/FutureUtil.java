package com.redisfront.util;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FutureUtil {

    private FutureUtil() {

    }

    public static <T> CompletableFuture<Void> propConv(Supplier<T> supplier, Consumer<T> consumer, Executor executor) {
        return CompletableFuture.supplyAsync(supplier, executor).thenAccept(t -> {
            if (Objects.nonNull(t)) {
                consumer.accept(t);
            }
        });
    }
}
