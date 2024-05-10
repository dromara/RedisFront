package org.dromara.redisfront.widget;


import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.quickswing.constant.OS;
import org.dromara.quickswing.ui.app.AppContext;
import org.dromara.quickswing.ui.app.AppWidget;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.commons.constant.Res;

import javax.swing.*;
import java.awt.*;

public class MainWidget extends AppWidget<RedisFrontPrefs> {
    public MainWidget(AppContext<? extends AppWidget<RedisFrontPrefs>, RedisFrontPrefs> context, String title, RedisFrontPrefs prefs) throws HeadlessException {
        super(context, title, prefs);
        if (SystemInfo.isWindows) {
            FlatLaf.setUseNativeWindowDecorations(true);
            this.rootPane.putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
        }
        if (SystemInfo.isMacFullWindowContentSupported) {
            this.rootPane.putClientProperty("apple.awt.fullWindowContent", true);
            this.rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
            this.rootPane.putClientProperty("apple.awt.windowTitleVisible", false);
            this.rootPane.putClientProperty(FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING,
                    FlatClientProperties.MACOS_WINDOW_BUTTONS_SPACING_MEDIUM);
        }
        this.setResizable(true);
        this.setSize(960, 600);
        this.setIconImages(Res.MAIN_FRAME_ICON_IMAGES);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(new MainComponent(this));
    }


    public boolean isFullScreen() {
        boolean isFullScreen = false;
        if (this.getOS() == OS.WINDOWS) {
            isFullScreen = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.NORMAL;
        } else if (getOS() == OS.MAC_OS_X) {
            GraphicsEnvironment evn = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreenDevice = evn.getDefaultScreenDevice();
            int screenWidth = (int) defaultScreenDevice.getDefaultConfiguration().getBounds().getWidth();
            int screenHeight = (int) defaultScreenDevice.getDefaultConfiguration().getBounds().getHeight();
            int screenMenuBarHeight = Toolkit.getDefaultToolkit().getScreenInsets(this.getGraphicsConfiguration()).top;
            int ownerWidth = this.getWidth();
            int ownerHeight = this.getHeight();
            isFullScreen = (screenWidth == ownerWidth) && ((screenHeight - (screenMenuBarHeight - 1)) == ownerHeight);
        }
        return isFullScreen;
    }

    @Override
    protected void preMenuBarInit(RedisFrontPrefs redisFrontPrefs, SplashScreen splashScreen) {

    }
}
