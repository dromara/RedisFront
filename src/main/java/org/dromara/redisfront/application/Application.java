package org.dromara.redisfront.application;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.ByteUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.derby.iapi.util.UTF8Util;
import org.bouncycastle.util.encoders.UTF8;
import org.dromara.quickswing.QSApplicationInitializer;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import raven.toast.Notifications;

/**
 * Application
 *
 * @author Jin
 */
@Slf4j
public class Application {

    public static QSWidget<RedisFrontPrefs> frame;

    public static void main(String[] args) {

        QSApplicationInitializer.initialize(args, Const.APP_NAME, () -> {
            RedisFrontContext context = new RedisFrontContext();
            QSWidget<RedisFrontPrefs> application = context.createApplication(args);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RedisFrontException redisFrontException) {
                Notifications.getInstance().show(Notifications.Type.ERROR, redisFrontException.getMessage());
            } else {
                log.error("Thread {} ", thread.getName(), throwable);
            }
        });
    }

}
