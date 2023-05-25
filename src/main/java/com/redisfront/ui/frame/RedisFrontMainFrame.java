
package com.redisfront.ui.frame;

import com.redisfront.commons.constant.UI;
import com.redisfront.service.ConnectService;
import com.redisfront.ui.component.MainMenuBar;
import com.redisfront.ui.dialog.AddConnectDialog;
import com.redisfront.ui.dialog.OpenConnectDialog;
import com.redisfront.ui.form.MainWindowForm;
import org.jdesktop.swingx.JXFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * RedisFrontFrame
 *
 * @author Jin
 */
public class RedisFrontMainFrame extends JXFrame {

    public RedisFrontMainFrame() {
        super("RedisFront", true);
        setIconImages(UI.MAIN_FRAME_ICON_IMAGES);
        UIManager.put("TitlePane.unifiedBackground", false);
        setJMenuBar(MainMenuBar.getInstance());
        initComponents();
        initEvnet();
    }

    private void initComponents() {
        var container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(MainWindowForm.getInstance().getContentPanel(), BorderLayout.CENTER);
    }

    private void initEvnet() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                OpenConnectDialog.showOpenConnectDialog(
                        //打开连接回调
                        ((connectInfo) -> MainWindowForm.getInstance().addTabActionPerformed(connectInfo)),
                        //编辑连接回调
                        (connectInfo -> AddConnectDialog.showEditConnectDialog(connectInfo, (connectInfo1) -> MainWindowForm.getInstance().addTabActionPerformed(connectInfo1))),
                        //删除连接回调
                        (connectInfo -> ConnectService.service.delete(connectInfo.id()))
                );
            }
        });
    }
}
