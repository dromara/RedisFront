package com.redisfront.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExectorUtil
 *
 * @author Jin
 */
public class ExecutorUtil {
    private static final int MAX_WORKER_THREADS = 10;

    private static ExecutorService executorService;

    public static ExecutorService getExecutorService(){
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
        try {
            executorService.execute(command);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

}
