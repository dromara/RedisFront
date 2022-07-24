package com.redisfront.commons.util;

import com.redisfront.commons.handler.ProcessHandler;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * ExectorUtil
 *
 * @author Jin
 */
public class ExecutorUtils {
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

    public static void runAsync(Runnable command) {
        executorService.execute(command);
    }

    public static void runAsync(Runnable command, ProcessHandler<Throwable> throwableProcessHandler) {
        try {
            Future<?> feature = executorService.submit(command);
            feature.get();
        } catch (Exception e) {
            throwableProcessHandler.processHandler(e);
        }
    }

}
