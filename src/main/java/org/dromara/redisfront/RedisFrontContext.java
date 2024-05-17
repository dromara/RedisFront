package org.dromara.redisfront;

import cn.hutool.core.io.FileUtil;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.dromara.quickswing.excutor.TaskExecutor;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.widget.MainWidget;
import lombok.Getter;
import org.dromara.quickswing.ui.app.AppContext;
import org.dromara.quickswing.ui.app.AppWidget;
import javax.swing.*;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author Jin
 */
@Getter
public class RedisFrontContext extends AppContext<AppWidget<RedisFrontPrefs>, RedisFrontPrefs> {

    public final static TaskExecutor TASK_EXECUTOR = new TaskExecutor(Executors.newVirtualThreadPerTaskExecutor());

    @Override
    protected MainWidget createApplication(String[] args, RedisFrontPrefs preferences) {
        ToolTipManager.sharedInstance().setInitialDelay(5);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        FlatLaf.registerCustomDefaultsSource("org.dromara.redisfront.theme");
        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#b30404"));
        FlatLaf.setUseNativeWindowDecorations(true);
        FlatMacLightLaf.setup();
        return new MainWidget(this, Const.APP_NAME, preferences);
    }

    @Override
    protected RedisFrontPrefs loadPreferences(String[] args) {
        return new RedisFrontPrefs(FileUtil.getUserHomePath());
    }

    @Override
    protected String getAppDataPath() {
        return FileUtil.getUserHomePath() + File.separator + "." + "RedisFront";
    }


    @Override
    protected String getAppResourceBundlePath() {
        return "org.dromara.redisfront.RedisFront";
    }

    @Override
    public <T, R> Future<R> taskSubmit(Callable<T> callable, BiFunction<T, Exception, R> function) {
        return TASK_EXECUTOR.submit(callable, function);
    }

    @Override
    public <T> void taskExecute(Callable<T> callable, BiConsumer<T, Exception> consumer) {
        TASK_EXECUTOR.execute(callable, consumer);
    }


    public String version() {
        return "2024.1";
    }
}