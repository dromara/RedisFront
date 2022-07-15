package com.redisfront;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.exception.ExceptionHandler;
import com.redisfront.commons.util.*;
import com.redisfront.ui.frame.RedisFrontMainFrame;

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

        System.setProperty(Const.LOG_FILE, Const.LOG_FILE_PATH);

        FlatLaf.registerCustomDefaultsSource(Const.PACKAGE_NAME);

        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#d81e06"));

        ToolTipManager.sharedInstance().setInitialDelay(0);

        SwingUtilities.invokeLater(() -> {

            PrefUtil.init(Const.ROOT_PATH);

            ExceptionHandler.init();

            ThemeUtil.setupTheme(args);

            ExecutorUtil.init();

            LocaleUtil.init();

            DerbyUtil.init();

            FlatInspector.install("ctrl shift alt X");

            FlatUIDefaultsInspector.install("ctrl shift alt Y");

            frame = new RedisFrontMainFrame();
            frame.setMinimumSize(new Dimension(1024, 768));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });

    }


}
