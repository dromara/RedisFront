package com.redisfront;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.exception.GlobalExceptionHandler;
import com.redisfront.commons.util.DerbyUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.PrefUtils;
import com.redisfront.commons.util.ThemeUtils;
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

        ToolTipManager.sharedInstance().setInitialDelay(3);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);

        FlatInspector.install("ctrl shift alt X");

        FlatUIDefaultsInspector.install("ctrl shift alt Y");

        SwingUtilities.invokeLater(() -> {

            PrefUtils.init(Const.ROOT_PATH);

            GlobalExceptionHandler.init();

            ThemeUtils.setupTheme(args);

            FutureUtils.init();

            DerbyUtils.init();

            frame = new RedisFrontMainFrame();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            if (screenSize.getWidth() > 1280) {
                frame.setMinimumSize(new Dimension(1280, 800));
            } else {
                frame.setMinimumSize(new Dimension(960, 640));
            }

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });

    }


}
