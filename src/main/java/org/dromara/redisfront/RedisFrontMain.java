package org.dromara.redisfront;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.QSApplicationInitializer;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.commons.constant.Constants;

/**
 * Application
 *
 * @author Jin
 */
@Slf4j
public class RedisFrontMain {
    public static void main(String[] args) {
        QSApplicationInitializer.initialize(args, Constants.APP_NAME, () -> {
            RedisFrontContext context = new RedisFrontContext();
            QSWidget<RedisFrontPrefs> application = context.createApplication(args);
            application.setLocationRelativeTo(null);
            application.setVisible(true);
        });
        log.info("RedisFront started");
    }

}
