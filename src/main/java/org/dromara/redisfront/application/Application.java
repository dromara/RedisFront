package org.dromara.redisfront.application;

import org.dromara.quickswing.evn.QSInitializer;
import org.dromara.quickswing.ui.app.AppWidget;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.commons.constant.Const;

/**
 * RedisFrontApplication
 *
 * @author Jin
 */
public class Application {

    public static AppWidget<RedisFrontPrefs> frame;

    public static void main(String[] args) {
        QSInitializer.initialize(args, Const.APP_NAME, () -> {
            RedisFrontContext context = new RedisFrontContext();
            AppWidget<RedisFrontPrefs> application = context.createApplication(args);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });
    }

}
