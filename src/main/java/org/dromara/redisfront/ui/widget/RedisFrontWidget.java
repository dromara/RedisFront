package org.dromara.redisfront.ui.widget;


import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.constant.QSOs;
import org.dromara.quickswing.ui.app.QSContext;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.RedisFrontEventListener;

import javax.swing.*;
import java.awt.*;

@Getter
@Slf4j
public class RedisFrontWidget extends QSWidget<RedisFrontPrefs> {

    private final RedisFrontComponent redisFrontComponent;
    private final RedisFrontEventListener eventListener;

    public RedisFrontWidget(QSContext<? extends QSWidget<RedisFrontPrefs>, RedisFrontPrefs> context, String title, RedisFrontPrefs prefs) throws HeadlessException {
        super(context, title, prefs);
        if (SystemInfo.isWindows || SystemInfo.isLinux) {
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
        this.setSize(1260, 800);
        this.setIconImages(Icons.MAIN_FRAME_ICON_IMAGES);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.eventListener = new RedisFrontEventListener(this);
        this.redisFrontComponent = new RedisFrontComponent(this);
        this.setContentPane(redisFrontComponent);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                if (redisFrontComponent.confirmCloseAll()) { redisFrontComponent.closeAll();dispose();System.exit(0); }
            }
        });
    }

    @Override
    public void displayException(Throwable error) {
        displayException($tr("Error.title"), error);
    }

    @Override
    public void displayException(String title, Throwable error) {
        log.error("{}", title, error);
        String message = ExceptionMessageUtils.userMessage(error, this::$tr);
        Runnable show = () -> JOptionPane.showMessageDialog(this, createErrorText(message), title, JOptionPane.ERROR_MESSAGE);
        if (SwingUtilities.isEventDispatchThread()) show.run();
        else SwingUtilities.invokeLater(show);
    }

    private static JTextArea createErrorText(String message) {
        // Wrap long server messages and treat their contents as plain text.
        var text = new JTextArea(message, 0, 40);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setFont(UIManager.getFont("Label.font"));
        text.setForeground(UIManager.getColor("Label.foreground"));
        // Measure wrapped lines before JOptionPane packs; otherwise it allocates one line of height.
        int width = text.getPreferredSize().width;
        text.setSize(width, Short.MAX_VALUE);
        text.setPreferredSize(new Dimension(width, text.getPreferredSize().height));
        return text;
    }

    public boolean isFullScreen() {
        boolean isFullScreen = false;
        if (this.getOS() == QSOs.WINDOWS) {
            isFullScreen = (getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.NORMAL;
        } else if (getOS() == QSOs.MAC_OS_X) {
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
        FlatDesktop.setQuitHandler(response -> {
            if (redisFrontComponent == null || redisFrontComponent.confirmCloseAll()) {
                if (redisFrontComponent != null) redisFrontComponent.closeAll();
                response.performQuit();
            } else response.cancelQuit();
        });
    }
}
