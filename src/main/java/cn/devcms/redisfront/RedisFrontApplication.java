package cn.devcms.redisfront;

import cn.devcms.redisfront.ui.frame.MainFrame;
import cn.devcms.redisfront.utils.DemoPrefs;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;

public class RedisFrontApplication {

    static boolean screenshotsMode = Boolean.parseBoolean( System.getProperty( "flatlaf.demo.screenshotsMode" ) );

    public static void main(String[] args) {

        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "RedisFront");
            System.setProperty("apple.awt.application.appearance", "system");
        }

        if( RedisFrontApplication.screenshotsMode && !SystemInfo.isJava_9_orLater && System.getProperty( "flatlaf.uiScale" ) == null )
            System.setProperty( "flatlaf.uiScale", "2x" );

        FlatLaf.registerCustomDefaultsSource( "cn.devcms.redisfront" );

        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup(new FlatDraculaIJTheme());
            FlatInspector.install( "ctrl shift alt X" );
            FlatUIDefaultsInspector.install( "ctrl shift alt Y" );
            MainFrame frame = new MainFrame();
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

    }


}
