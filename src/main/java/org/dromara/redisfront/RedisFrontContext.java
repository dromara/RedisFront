package org.dromara.redisfront;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.DbUtil;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.excutor.QSTaskExecutor;
import org.dromara.quickswing.ui.app.QSContext;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.ui.widget.main.MainWidget;
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
public class RedisFrontContext extends QSContext<QSWidget<RedisFrontPrefs>, RedisFrontPrefs> {

    public final static QSTaskExecutor TASK_EXECUTOR = new QSTaskExecutor(Executors.newVirtualThreadPerTaskExecutor());

    @Override
    protected MainWidget createApplication(String[] args, RedisFrontPrefs preferences) {
        ToolTipManager.sharedInstance().setInitialDelay(5);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        FlatLaf.registerCustomDefaultsSource(Constants.APP_THEME_PACKAGE);
        FlatLaf.setUseNativeWindowDecorations(true);
        FlatMacLightLaf.setup();
        return new MainWidget(this, Constants.APP_NAME, preferences);
    }

    @Override
    protected RedisFrontPrefs loadPreferences(String[] args) {
        return new RedisFrontPrefs(FileUtil.getUserHomePath());
    }

    @Override
    protected String getAppDataPath() {
        return FileUtil.getUserHomePath() + File.separator + "." + Constants.APP_NAME.toLowerCase();
    }

    @Override
    protected String getAppResourceBundlePath() {
        return Constants.APP_RESOURCE_BUNDLE;
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
    protected void populatePreferencesFromApplication(QSWidget<RedisFrontPrefs> app, RedisFrontPrefs preferences) {

    }

    @Override
    protected void performPostInitialization(QSWidget<RedisFrontPrefs> application, RedisFrontPrefs preferences) {
        GlassPanePopup.install(application);
        if (!preferences.getDBInitialized()) {
            DataSource datasource = getDatabaseManager().getDatasource();
            try {
                DbUtil.use(datasource).execute(Constants.SQL_CREATE_CONNECT_GROUP);
                log.info("创建 connect_group 表完成！");
                DbUtil.use(datasource).execute(Constants.SQL_CREATE_CONNECT_DETAIL);
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