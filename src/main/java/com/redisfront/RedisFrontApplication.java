package com.redisfront;

import com.redisfront.common.constant.Constant;
import com.redisfront.common.util.PrefUtil;
import com.redisfront.common.util.ThemeUtil;
import com.redisfront.service.ConnectService;
import com.redisfront.ui.RedisFrontFrame;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

public class RedisFrontApplication {

    public static void main(String[] args) {
        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "RedisFront");
            System.setProperty("apple.awt.application.appearance", "system");
        }

        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        if (!SystemInfo.isJava_9_orLater && System.getProperty("flatlaf.uiScale") == null) {
            System.setProperty("flatlaf.uiScale", "2x");
        }

        FlatLaf.registerCustomDefaultsSource("cn.devcms.redisfront");
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#d81e06"));

        SwingUtilities.invokeLater(() -> {

            PrefUtil.init("/redis-front");
            ThemeUtil.setupTheme(args);

            if (PrefUtil.getState().getBoolean(Constant.KEY_APP_DATABASE_INIT, true)) {
                ConnectService.service.initDatabase();
                PrefUtil.getState().put(Constant.KEY_APP_DATABASE_INIT, Boolean.FALSE.toString());
            }

            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");

            var frame = new RedisFrontFrame();
            frame.setMinimumSize(new Dimension(1024, 768));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }


}
