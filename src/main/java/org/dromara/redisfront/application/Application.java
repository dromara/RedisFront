package org.dromara.redisfront.application;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.quickswing.ui.app.AppWidget;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.exception.GlobalExceptionHandler;
import org.dromara.redisfront.commons.util.*;
import org.dromara.redisfront.ui.frame.RedisFrontMainFrame;
import org.dromara.redisfront.widget.ui.ThemesChange;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;

/**
 * RedisFrontApplication
 *
 * @author Jin
 */
public class Application {

    public static RedisFrontMainFrame frame;

    public static void main2(String[] args) {

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

        SwingUtilities.invokeLater(() -> {

            PrefUtils.init(Const.ROOT_PATH);

            GlobalExceptionHandler.init();

            ThemeUtils.setupTheme(args);

            FutureUtils.init();

            DerbyUtils.init();


            frame = new RedisFrontMainFrame();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            if (screenSize.getWidth() > 1280) {
                frame.setPreferredSize(new Dimension(1280, 800));
            } else if (screenSize.getWidth() > 1024) {
                frame.setPreferredSize(new Dimension(1200, 768));
            } else {
                frame.setPreferredSize(new Dimension(960, 640));
            }

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RedisFrontContext redisFrontContext = new RedisFrontContext();
            AppWidget<RedisFrontPrefs> application = redisFrontContext.createApplication(args);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });
    }

}
