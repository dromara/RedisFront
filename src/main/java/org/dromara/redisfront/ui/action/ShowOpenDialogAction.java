package org.dromara.redisfront.ui.action;

import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.quickswing.ui.app.QSAction;
import org.dromara.redisfront.dialog.AddConnectDialog;
import org.dromara.redisfront.ui.widget.MainWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ShowOpenDialogAction extends QSAction<MainWidget> {

    public ShowOpenDialogAction(MainWidget app, String key) {
        super(app, key);
    }

    @Override
    public void handleAction(ActionEvent e) {

    }


}
