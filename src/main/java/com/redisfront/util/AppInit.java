package com.redisfront.util;

import com.redisfront.constant.Const;
import com.redisfront.exception.RedisFrontException;
import com.redisfront.service.ConnectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Init
 *
 * @author Jin
 */
public class AppInit {

    private static final Logger log = LoggerFactory.getLogger(AppInit.class);

    public static void init() {

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof RedisFrontException redisFrontException) {
                if (redisFrontException.showMessage()) {
                    MsgUtil.showErrorDialog("程序出现异常", redisFrontException);
                }
            } else {
                MsgUtil.showErrorDialog("程序出现异常", new Exception(e.getMessage()));
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
