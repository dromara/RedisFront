package com.redisfront;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;
import com.redisfront.constant.Const;
import com.redisfront.ui.frame.RedisFrontMainFrame;
import com.redisfront.util.AppInit;
import com.redisfront.util.PrefUtil;
import com.redisfront.util.ThemeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

/**
 * RedisFrontApplication
 *
 * @author Jin
 */
public class RedisFrontApplication {

    public static RedisFrontMainFrame frame;

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

        FlatLaf.registerCustomDefaultsSource(Const.PACKAGE_NAME);

        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#d81e06"));

        System.setProperty(Const.LOG_FILE, Const.LOG_FILE_PATH);

        PrefUtil.init(Const.ROOT_PATH);


        SwingUtilities.invokeLater(() -> {
            ThemeUtil.setupTheme(args);

            AppInit.init();

            FlatInspector.install("ctrl shift alt X");

            FlatUIDefaultsInspector.install("ctrl shift alt Y");

            frame = new RedisFrontMainFrame();
            frame.setMinimumSize(new Dimension(1220, 800));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });

    }


}
