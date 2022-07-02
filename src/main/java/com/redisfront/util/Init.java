package com.redisfront.util;

import com.redisfront.constant.Const;
import com.redisfront.service.ConnectService;

import javax.swing.*;

/**
 * Init
 *
 * @author Jin
 */
public class Init {

    public static void init() {
        try {

            LocaleUtil.init();

            DerbyUtil.init();

            //数据库初始化
            if (PrefUtil.getState().getBoolean(Const.KEY_APP_DATABASE_INIT, true)) {
                ConnectService.service.initDatabase();
                PrefUtil.getState().put(Const.KEY_APP_DATABASE_INIT, Boolean.FALSE.toString());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "启动失败\n\n" + ex.getMessage(),
                    "RedisFront", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

    }

}
