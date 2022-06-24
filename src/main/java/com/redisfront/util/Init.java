package com.redisfront.util;

import com.formdev.flatlaf.FlatLaf;
import com.redisfront.constant.Constant;
import com.redisfront.service.ConnectService;
import com.redisfront.util.DerbyUtil;
import com.redisfront.util.LocaleUtil;
import com.redisfront.util.MsgUtil;
import com.redisfront.util.PrefUtil;

import javax.swing.*;
import java.io.File;
import java.util.Collections;

/**
 * Init
 *
 * @author Jin
 */
public class Init {

    public static void init() {
        try {
            FlatLaf.registerCustomDefaultsSource("com.redisfront");
            FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#d81e06"));

            System.setProperty("LOG_FILE", Constant.DATA_PATH + File.separator + "logs" + File.separator + "redis-front.log");

            PrefUtil.init("/redis-front");

            LocaleUtil.init();

            DerbyUtil.init();

            //数据库初始化
            if (PrefUtil.getState().getBoolean(Constant.KEY_APP_DATABASE_INIT, true)) {
                ConnectService.service.initDatabase();
                PrefUtil.getState().put(Constant.KEY_APP_DATABASE_INIT, Boolean.FALSE.toString());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "启动失败\n\n" + ex.getMessage(),
                    "RedisFront", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

    }

}
