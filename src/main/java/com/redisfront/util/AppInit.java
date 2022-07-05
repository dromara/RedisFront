package com.redisfront.util;

import com.redisfront.constant.Const;
import com.redisfront.exception.RedisFrontException;
import com.redisfront.service.ConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Init
 *
 * @author Jin
 */
public class AppInit {

    private static final Logger log = LoggerFactory.getLogger(AppInit.class);

    public static void init() {

        ToolTipManager.sharedInstance().setInitialDelay(0);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof RedisFrontException redisFrontException) {
                if (redisFrontException.showMessage()) {
                    MsgUtil.showErrorDialog("Error", redisFrontException);
                }
            } else {
                MsgUtil.showErrorDialog("Error", new Exception(e.getMessage()));
            }
            log.error("程序运行异常", e);
        });

        LocaleUtil.init();

        DerbyUtil.init();

        //数据库初始化
        if (PrefUtil.getState().getBoolean(Const.KEY_APP_DATABASE_INIT, true)) {
            ConnectService.service.initDatabase();
            PrefUtil.getState().put(Const.KEY_APP_DATABASE_INIT, Boolean.FALSE.toString());
        }
    }

}
