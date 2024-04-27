package org.dromara.redisfront.commons.exception;

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
        if (e instanceof RedisFrontException redisFrontException) {
            if (redisFrontException.showMessage()) {
            }
        } else if (e instanceof RedisException redisFrontException) {
            Throwable throwable = redisFrontException.getCause() == null ? redisFrontException : redisFrontException.getCause();
        } else {
        }
    };

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler(redisFrontExceptionHandler);
    }

}
