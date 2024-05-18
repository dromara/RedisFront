package org.dromara.redisfront.widget.action;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.database.DatabaseManager;
import org.dromara.quickswing.ui.app.AppAction;
import org.dromara.quickswing.ui.app.AppContext;
import org.dromara.quickswing.ui.app.AppWidget;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.RedisFrontPrefs;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.event.ConnectTreeEvent;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

@Slf4j
public class AddConnectGroupAction extends AppAction<MainWidget> {

    public AddConnectGroupAction(MainWidget app, String key) {
        super(app, key);
    }

    @Override
    public void handleAction(ActionEvent e) {
        Notifications.getInstance().setJFrame(app);
        String value = JOptionPane.showInputDialog(app, "分组名称", "添加分组", JOptionPane.PLAIN_MESSAGE);
        if (StrUtil.isEmpty(value)) {
            Notifications.getInstance().show(Notifications.Type.ERROR, "分组名称能为空！");
        }
        RedisFrontContext context = (RedisFrontContext) app.getContext();
        DatabaseManager databaseManager = context.getDatabaseManager();
        context.taskExecute(() -> {
            Entity connectGroup = Entity.create("connect_group");
            connectGroup.set("group_name", value);
            DbUtil.use(databaseManager.getDatasource()).insert(connectGroup);
            context.getEventBus().publish(new ConnectTreeEvent(null));
            return null;
        }, (result, exception) -> {
            if (exception != null) {
                log.error(exception.getMessage());
                Notifications.getInstance().show(Notifications.Type.ERROR, exception.getMessage());
            } else {
                Notifications.getInstance().show(Notifications.Type.SUCCESS, "添加成功！");
            }
        });
    }
}
