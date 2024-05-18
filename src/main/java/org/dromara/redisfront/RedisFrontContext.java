package org.dromara.redisfront;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.DbUtil;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.excutor.TaskExecutor;
import org.dromara.quickswing.ui.app.AppContext;
import org.dromara.quickswing.ui.app.AppWidget;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.widget.MainWidget;
import raven.popup.GlassPanePopup;
import javax.sql.DataSource;
import javax.swing.*;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author Jin
 */
@Getter
@Slf4j
public class RedisFrontContext extends AppContext<AppWidget<RedisFrontPrefs>, RedisFrontPrefs> {

    public final static TaskExecutor TASK_EXECUTOR = new TaskExecutor(Executors.newVirtualThreadPerTaskExecutor());

    @Override
    protected MainWidget createApplication(String[] args, RedisFrontPrefs preferences) {
        ToolTipManager.sharedInstance().setInitialDelay(5);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        FlatLaf.registerCustomDefaultsSource(Const.APP_THEME_PACKAGE);
//        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#b30404"));
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
        return FileUtil.getUserHomePath() + File.separator + "." + Const.APP_NAME.toLowerCase();
    }

    @Override
    protected String getAppResourceBundlePath() {
        return Const.APP_RESOURCE_BUNDLE;
    }

    @Override
    public <T, R> Future<R> taskSubmit(Callable<T> callable, BiFunction<T, Exception, R> function) {
        return TASK_EXECUTOR.submit(callable, function);
    }

    @Override
    public <T> void taskExecute(Callable<T> callable, BiConsumer<T, Exception> consumer) {
        TASK_EXECUTOR.execute(callable, consumer);
    }

    @Override
    protected void performPostInitialization(AppWidget<RedisFrontPrefs> application, RedisFrontPrefs preferences) {
        GlassPanePopup.install(application);
        if (!preferences.getDBInitialized()) {
            DataSource datasource = getDatabaseManager().getDatasource();
            try {
                DbUtil.use(datasource).execute(Const.SQL_CREATE_CONNECT_GROUP);
                log.info("创建 connect_group 表完成！");
                DbUtil.use(datasource).execute(Const.SQL_CREATE_CONNECT_DETAIL);
                log.info("创建 connect_detail 表完成！");
                preferences.setDBInitialized(true);
            } catch (SQLException e) {
                log.error("数据库初始化失败.", e);
                JOptionPane.showMessageDialog(application, e.getMessage(), "数据库初始化失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public String version() {
        return "2024.1";
    }
}