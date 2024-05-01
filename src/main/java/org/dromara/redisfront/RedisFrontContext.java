package org.dromara.redisfront;

import cn.hutool.core.io.FileUtil;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.redisfront.widget.main.MainWidget;
import lombok.Getter;
import org.dromara.quickswing.ui.app.AppContext;
import org.dromara.quickswing.ui.app.AppWidget;

import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author Jin
 */
@Getter
public class RedisFrontContext extends AppContext<AppWidget<RedisFrontPrefs>, RedisFrontPrefs> {


    @Override
    protected MainWidget createApplication(String[] args, RedisFrontPrefs preferences) {
        ToolTipManager.sharedInstance().setInitialDelay(5);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        FlatLaf.registerCustomDefaultsSource("org.dromara.redisfront.theme");
//        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#d81e06"));
        FlatMacLightLaf.setup();
        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }
        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "RedisFront");
            System.setProperty("apple.awt.application.appearance", "system");
        }
        return new MainWidget(this, "RedisFront", preferences);
    }

    @Override
    protected RedisFrontPrefs loadPreferences(String[] args) {
        return new RedisFrontPrefs(FileUtil.getUserHomePath());
    }

    @Override
    protected String getAppDataPath() {
        return FileUtil.getUserHomePath() + File.separator + ".redis-front";
    }


    @Override
    protected String getAppResourceBundlePath() {
        return "org.dromara.redisfront.RedisFront";
    }

    public <T, R> Future<R> taskSubmit(Callable<T> callable, BiFunction<T, Exception, R> function) {
        return null;
    }

    public <T> void taskExecute(Callable<T> callable, BiConsumer<T, Exception> consumer) {

    }
}