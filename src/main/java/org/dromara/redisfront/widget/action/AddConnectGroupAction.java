package org.dromara.redisfront.widget.action;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.database.QSDbManager;
import org.dromara.quickswing.ui.app.QSAction;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.event.RefreshConnectTreeEvent;
import raven.toast.Notifications;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Slf4j
public class AddConnectGroupAction extends QSAction<MainWidget> {

    public AddConnectGroupAction(MainWidget app, String key) {
        super(app, key);
    }

    @Override
    public void handleAction(ActionEvent e) {
        Notifications.getInstance().setJFrame(app);
        String value = (String) JOptionPane.showInputDialog(app, "分组名称", "添加分组", JOptionPane.PLAIN_MESSAGE,null,null,"新建分组");
        if (StrUtil.isEmpty(value)) {
            return;
        }
        RedisFrontContext context = (RedisFrontContext) app.getContext();
        QSDbManager databaseManager = context.getDatabaseManager();
        context.taskExecute(() -> {
            Entity connectGroup = Entity.create("connect_group");
            connectGroup.set("group_name", value);
            DbUtil.use(databaseManager.getDatasource()).insert(connectGroup);
            context.getEventBus().publish(new RefreshConnectTreeEvent(null));
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
