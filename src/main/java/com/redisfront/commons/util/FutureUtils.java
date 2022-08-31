package com.redisfront.commons.util;

import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ProcessHandler;

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

    private static ExecutorService executorService;

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static void init() {
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

    public static CompletableFuture<Void> runAsync(Runnable runnable, ExecutorService executorService) {
        return CompletableFuture.runAsync(runnable);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, Runnable beforeHandler, Runnable afterHandler) {
        return CompletableFuture
                .runAsync(beforeHandler, executorService)
                .thenRunAsync(runnable, executorService)
                .thenRunAsync(afterHandler, executorService);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, ProcessHandler<Throwable> throwableProcessHandler) {
        return CompletableFuture.runAsync(runnable, executorService).exceptionallyAsync(throwable -> {
            throwableProcessHandler.processHandler(throwable);
            return null;
        });
    }

    public static <T> CompletableFuture<Void> supplyAsync(Supplier<T> supplier, Consumer<T> consumer) {
        return CompletableFuture.supplyAsync(supplier, executorService).thenAccept(t -> {
            if (Fn.isNotNull(t)) {
                consumer.accept(t);
            }
        });
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executorService);
    }

}
