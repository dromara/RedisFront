
package cn.devcms.redisfront.ui.frame;

import cn.devcms.redisfront.ui.dialog.AddConnectDialog;
import cn.devcms.redisfront.ui.dialog.SettingDialog;
import cn.devcms.redisfront.ui.form.MainForm;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.formdev.flatlaf.extras.components.FlatButton;
import org.jdesktop.swingx.JXFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JXFrame {
    MainForm mainForm;

    SettingDialog settingDialog;


    public MainFrame() {
        super(" RedisFront ", true);
        setIconImages(FlatSVGUtils.createWindowIconImages("/svg/redis.svg"));
        UIManager.put("TitlePane.unifiedBackground", false);
        menuBarInit();
        initComponents();
        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit);
    }

    private void initComponents() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        mainForm = new MainForm(this);
        settingDialog = new SettingDialog(this);
        container.add(mainForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    }


    private void menuBarInit() {
        setJMenuBar(new JMenuBar() {
            {
                JMenu menuA = new JMenu("文件");
                menuA.setMnemonic('F');
                JMenuItem add = new JMenuItem("新建连接",KeyEvent.VK_A);
                add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
                add.addActionListener(e -> {
                    AddConnectDialog dialog = new AddConnectDialog(cn.devcms.redisfront.ui.frame.MainFrame.this);
                    dialog.setLocationRelativeTo(null);
                    dialog.pack();
                    dialog.setVisible(true);
                    mainForm.addAction();
                });
                menuA.add(add);
                JMenuItem open = new JMenuItem("打开连接",KeyEvent.VK_S);
                open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
                open.addActionListener(e -> {
                    AddConnectDialog dialog = new AddConnectDialog(cn.devcms.redisfront.ui.frame.MainFrame.this);
                    dialog.setLocationRelativeTo(null);
                    dialog.pack();
                    dialog.setVisible(true);
                });
                menuA.add(open);
                add(menuA);
                JMenu menuB = new JMenu("编辑");
                add(menuB);
                JMenu menuC = new JMenu("设置");
                menuC.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        settingDialog.setMinimumSize(new Dimension(500, 400));
                        settingDialog.setResizable(false);
                        settingDialog.setLocationRelativeTo(cn.devcms.redisfront.ui.frame.MainFrame.this);
                        settingDialog.pack();
                        settingDialog.setVisible(true);
                    }
                });
                add(menuC);
                JMenu menuD = new JMenu("关于");
                add(menuD);
                add(Box.createGlue());
                FlatButton giteeBtn = new FlatButton();
                giteeBtn.setIcon(new FlatSVGIcon("icons/gitee.svg"));
                giteeBtn.setButtonType(FlatButton.ButtonType.toolBarButton);
                giteeBtn.setFocusable(false);
                giteeBtn.setToolTipText(" 去码云给个star吧 :) ");
                giteeBtn.setRolloverIcon(new FlatSVGIcon("icons/gitee_red.svg"));
                giteeBtn.addActionListener(e -> JOptionPane.showMessageDialog(cn.devcms.redisfront.ui.frame.MainFrame.this, "Hello User! How are you?", "User", JOptionPane.INFORMATION_MESSAGE));
                add(giteeBtn);
            }
        });

    }


}
