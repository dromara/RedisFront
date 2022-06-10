package cn.devcms.redisfront;

import cn.devcms.redisfront.common.util.PrefUtil;
import cn.devcms.redisfront.common.util.ThemeUtil;
import cn.devcms.redisfront.ui.RedisFrontFrame;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.util.SystemInfo;
import redis.clients.jedis.JedisPool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.Collections;
import java.util.Locale;

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

        if (!SystemInfo.isJava_9_orLater && System.getProperty("flatlaf.uiScale") == null)
            System.setProperty("flatlaf.uiScale", "2x");

        FlatLaf.registerCustomDefaultsSource("cn.devcms.redisfront");
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#d81e06"));


        SwingUtilities.invokeLater(() -> {

            PrefUtil.init("/redis-front");
            ThemeUtil.setupTheme(args);

            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");

            RedisFrontFrame frame = new RedisFrontFrame();
            frame.addWindowListener(new WindowAdapter() {

            });

            frame.setMinimumSize(new Dimension(1200, 800));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }


}
