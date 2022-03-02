package cn.devcms.redisfront;

import cn.devcms.redisfront.ui.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;

public class RedisFrontApplication {


    public static void main(String[] args) {

        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "RedisFront");
            System.setProperty("apple.awt.application.appearance", "system");
        }

        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup(new FlatDarkLaf());
            JFrame mainWidget = new MainFrame();
            mainWidget.pack();
            mainWidget.setLocationRelativeTo(null);
            mainWidget.setVisible(true);
        });

    }


}
