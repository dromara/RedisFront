package com.redisfront.util;

import com.redisfront.exception.RedisFrontException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ExectorUtil
 *
 * @author Jin
 */
public class ExecutorUtil {
    private static final Logger log = LoggerFactory.getLogger(ExecutorUtil.class);
    private static ExecutorService executorService;

    public static void init() {
        executorService = Executors.newFixedThreadPool(10);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log.error("exception:", e);
            LoadingUtil.closeDialog();
            if (e instanceof RedisFrontException redisFrontException) {
                if (redisFrontException.showMessage()) {
                    MsgUtil.showErrorDialog("Error", redisFrontException);
                }
            } else {
                MsgUtil.showErrorDialog("Error", new Exception(e.getMessage()));
            }
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
