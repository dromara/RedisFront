package com.redisfront.commons.util;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FutureUtil {

    private FutureUtil() {

    }

    public static <T> CompletableFuture<Void> completableFuture(Supplier<T> supplier, Consumer<T> consumer) {
        return CompletableFuture.supplyAsync(supplier, ExecutorUtil.getExecutorService()).thenAccept(t -> {
            if (Objects.nonNull(t)) {
                consumer.accept(t);
            }
        });
    }
}
