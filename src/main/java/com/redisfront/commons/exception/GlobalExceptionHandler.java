package com.redisfront.commons.exception;

import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.LoadingUtils;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExceptionHandler
 *
 * @author Jin
 */
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public static final Thread.UncaughtExceptionHandler redisFrontExceptionHandler = (t, e) -> {
        log.error("Thread[{}] 应用异常", t.getName(), e);
        LoadingUtils.closeDialog();
        if (e instanceof RedisFrontException redisFrontException) {
            if (redisFrontException.showMessage()) {
                AlertUtils.showErrorDialog("Error", redisFrontException);
            }
        } else if (e instanceof RedisException redisFrontException) {
            Throwable throwable = redisFrontException.getCause() == null ? redisFrontException : redisFrontException.getCause();
            AlertUtils.showErrorDialog("Error", throwable);
        } else {
            AlertUtils.showErrorDialog("Error", e);
        }
    };

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(redisFrontExceptionHandler);
    }

}
