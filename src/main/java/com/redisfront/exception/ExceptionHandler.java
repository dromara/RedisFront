package com.redisfront.exception;

import com.redisfront.util.LoadingUtil;
import com.redisfront.util.AlertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ExceptionHandler
 *
 * @author Jin
 */
public class ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    public static void init() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            log.error("Thread[{}] 应用异常", t.getName(), e);
            //close LoadingDialog
            LoadingUtil.closeDialog();
            //alert message
            if (e instanceof RedisFrontException redisFrontException) {
                if (redisFrontException.showMessage()) {
                    AlertUtil.showErrorDialog("Error", redisFrontException);
                }
            } else {
                AlertUtil.showErrorDialog("Error", new Exception(e.getMessage()));
            }
        });
    }

}
