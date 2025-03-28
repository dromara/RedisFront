package org.dromara.redisfront.commons.utils;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * ExectorUtil
 *
 * @author Jin
 */
public class FutureUtils {
    private static final int MAX_WORKER_THREADS = 10;

    private static final ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(MAX_WORKER_THREADS, runnable -> {
            final var threadFactory = Executors.defaultThreadFactory();
            final var newThread = threadFactory.newThread(runnable);
            newThread.setName("RedisFrontWorker-" + newThread.getName());
            newThread.setDaemon(true);
            return newThread;
        });
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable);
    }

    public static void runAsync(Runnable runnable, Consumer<Throwable> consumer) {
        CompletableFuture.runAsync(runnable, executorService).exceptionallyAsync(throwable -> {
            consumer.accept(throwable);
            return null;
        });
    }

    public static <T> CompletableFuture<Void> supplyAsync(Supplier<T> supplier, Consumer<T> consumer) {
        return CompletableFuture.supplyAsync(supplier, executorService).thenAccept(t -> {
            if (RedisFrontUtils.isNotNull(t)) {
                consumer.accept(t);
            }
        });
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executorService);
    }

}
