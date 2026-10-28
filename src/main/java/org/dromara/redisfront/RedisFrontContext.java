package org.dromara.redisfront;

import cn.hutool.core.io.FileUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.database.QSDbManager;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventBus;
import org.dromara.quickswing.excutor.QSTaskExecutor;
import org.dromara.quickswing.ui.app.QSContext;
import org.dromara.quickswing.ui.app.QSPrefs;
import org.dromara.quickswing.ui.app.QSWidget;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.FutureUtils;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.commons.utils.UpgradeUtils;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import raven.popup.GlassPanePopup;
import raven.toast.Notifications;

import javax.sql.DataSource;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.Locale;
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
    private static QSEventBus eventBus;
    private static final java.util.concurrent.ThreadPoolExecutor COMMAND_EVENTS = new java.util.concurrent.ThreadPoolExecutor(
            1, 1, 0, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.ArrayBlockingQueue<>(1000), task -> {
                var thread = new Thread(task, "RedisFrontCommandEvents");thread.setDaemon(true);return thread;
            }, new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());

    public RedisFrontContext() {
        RedisFrontContext.eventBus = super.getEventBus();
    }

    private RedisFrontPrefs preferences;
    private volatile QSDbManager databaseManager;

    public static void publishEvent(QSEvent event) {
        if (eventBus != null) COMMAND_EVENTS.execute(() -> eventBus.publish(event));
    }

    @Override
    protected RedisFrontWidget createApplication(String[] args, RedisFrontPrefs preferences) {
        if(SystemInfo.isWindows) {
            setUIFont(new Font("Arial Unicode MS", Font.PLAIN, 13));
        }
        ToolTipManager.sharedInstance().setInitialDelay(5);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        FlatLaf.registerCustomDefaultsSource(Constants.APP_THEME_PACKAGE);
        FlatLaf.setUseNativeWindowDecorations(true);
        FlatMacLightLaf.setup();
        return new RedisFrontWidget(this, Constants.APP_NAME, preferences);
    }

    private static void setUIFont(Font font) {
        UIManager.put("TextArea.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("EditorPane.font", font);
        UIManager.put("TextField.font", font);
    }

    @Override
    protected RedisFrontPrefs loadPreferences(String[] args) {
        this.preferences = new RedisFrontPrefs(FileUtil.getUserHomePath());
        return preferences;
    }

    /**
     * quick-swing 的 SqliteDbManager 拼 JDBC URL 时会在文件名末尾追加 ';'，sqlite-jdbc 把它当成了
     * 文件名的一部分，数据库文件因此被创建成 "redis-front.db;"。这里覆写为自建数据源，使用规范文件名，
     * 历史文件在 {@link #performBeforeInitialization} 中一次性迁移。
     */
    @Override
    public QSDbManager getDatabaseManager() {
        if (databaseManager == null) {
            synchronized (this) {
                if (databaseManager == null) {
                    var datasource = new SimpleDataSource("jdbc:sqlite:" + preferences.getDataPath()
                            + File.separator + preferences.getDbFileName(), null, null, "org.sqlite.JDBC");
                    databaseManager = new QSDbManager() {
                        @Override
                        public void initialize(QSPrefs prefs) {
                        }

                        @Override
                        public DataSource getDatasource() {
                            return datasource;
                        }
                    };
                }
            }
        }
        return databaseManager;
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
    protected void performBeforeInitialization(RedisFrontPrefs preferences) {
        // 设置对话框切换语言时只写入了偏好并临时改了默认 Locale，这里必须在任何
        // ResourceBundle 加载之前恢复，否则重启后仍按操作系统语言取值，语言设置等于没保存
        String languageTag = preferences.getState().get(Constants.KEY_LANGUAGE,
                Locale.SIMPLIFIED_CHINESE.toLanguageTag());
        Locale.setDefault(Locale.forLanguageTag(languageTag));

        String dbPath = preferences.getDataPath() + File.separator + preferences.getDbFileName();
        //历史版本受上述框架 URL 问题影响，数据库文件名末尾带着 ';'，先迁移为规范文件名
        File legacyDbFile = new File(dbPath + ";");
        if (!FileUtil.exist(dbPath) && FileUtil.exist(legacyDbFile.getPath())) {
            try {
                FileUtil.move(legacyDbFile, new File(dbPath), true);
                log.info("历史数据库文件已迁移: {} -> {}", legacyDbFile.getName(), preferences.getDbFileName());
            } catch (Exception e) {
                log.error("数据库文件迁移失败.", e);
                JOptionPane.showMessageDialog(null, e.getMessage(), "数据库初始化失败", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (!preferences.getDBInitialized() || !FileUtil.exist(dbPath)) {
            DataSource datasource = getDatabaseManager().getDatasource();
            try {
//                DbUtil.use(datasource).execute("drop table connect_group");
                DbUtil.use(datasource).execute(Constants.SQL_CREATE_CONNECT_GROUP);
                log.info("创建 connect_group 表完成！");
//                DbUtil.use(datasource).execute("drop table connect_detail");
                DbUtil.use(datasource).execute(Constants.SQL_CREATE_CONNECT_DETAIL);
                log.info("创建 connect_detail 表完成！");
                preferences.setDBInitialized(true);
            } catch (SQLException e) {
                log.error("数据库初始化失败.", e);
                JOptionPane.showMessageDialog(null, e.getMessage(), "数据库初始化失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected void performPostInitialization(QSWidget<RedisFrontPrefs> application, RedisFrontPrefs preferences) {
        GlassPanePopup.install(application);
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof RedisFrontException redisFrontException) {
                log.error("Thread {}", thread.getName(), throwable);
                SwingUtilities.invokeLater(() -> Notifications.getInstance().show(Notifications.Type.ERROR,
                        ExceptionMessageUtils.userMessage(redisFrontException, application::$tr)));
            } else {
                log.error("Thread {}", thread.getName(), throwable);
            }
        });
        FutureUtils.runAsync(() -> UpgradeUtils.checkVersion(version()));
    }

    public String version() {
        return "2026.1";
    }
}
