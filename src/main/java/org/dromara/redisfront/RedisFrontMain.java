package org.dromara.redisfront;
import cn.hutool.core.img.ColorUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.QSApplicationInitializer;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.*;

/**
 * Application
 *
 * @author Jin
 */
@Slf4j
public class RedisFrontMain {

    public static QSWidget<RedisFrontPrefs> frame;

    public static void main(String[] args) {

        QSApplicationInitializer.initialize(args, Constants.APP_NAME, () -> {
            RedisFrontContext context = new RedisFrontContext();
            QSWidget<RedisFrontPrefs> application = context.createApplication(args);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
            Color object = UIManager.getColor("ComboBox.buttonBackground");
            ColorUtil.toHex(object);
            System.out.println();
        });

        log.info("RedisFront started");

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RedisFrontException redisFrontException) {
                Notifications.getInstance().show(Notifications.Type.ERROR, redisFrontException.getMessage());
            } else {
                log.error("Thread {}", thread.getName(), throwable);
            }
        });
    }

}
