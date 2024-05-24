package org.dromara.redisfront.widget.action;

import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.quickswing.ui.app.QSAction;
import org.dromara.redisfront.widget.MainWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class EditOpenDialogAction extends QSAction<MainWidget> {

    public EditOpenDialogAction(MainWidget app, String key) {
        super(app, key);
    }

    @Override
    public void handleAction(ActionEvent e) {
        System.out.println("Show Open Dialog");
    }

    @Override
    public KeyStroke getKeyStroke() {
        return SystemInfo.isMacOS ?
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()):
                KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
    }
}
