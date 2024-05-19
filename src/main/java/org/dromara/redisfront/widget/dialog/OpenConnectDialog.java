package org.dromara.redisfront.widget.dialog;

import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.widget.MainWidget;

import javax.swing.*;
import java.awt.*;

public class OpenConnectDialog extends QSDialog<MainWidget> {

    protected OpenConnectDialog(MainWidget owner, String title) {
        super(owner, title);
        this.setModal(true);
        this.setMinimumSize(new Dimension(500, 400));
        this.setResizable(false);
    }

    @Override
    protected JPanel getBodyPanel() {
        return new JPanel();
    }

    @Override
    protected void initialize(MainWidget mainWidget) {

    }
}
