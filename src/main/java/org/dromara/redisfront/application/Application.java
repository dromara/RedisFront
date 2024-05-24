package org.dromara.redisfront.application;
import org.dromara.quickswing.QSApplicationInitializer;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.commons.constant.Const;

/**
 * Application
 *
 * @author Jin
 */
public class Application {

    public static QSWidget<RedisFrontPrefs> frame;

    public static void main(String[] args) {
        QSApplicationInitializer.initialize(args, Const.APP_NAME, () -> {
            RedisFrontContext context = new RedisFrontContext();
            QSWidget<RedisFrontPrefs> application = context.createApplication(args);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });
    }

}
