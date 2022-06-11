
package cn.devcms.redisfront.ui;

import cn.devcms.redisfront.ui.dialog.AddConnectDialog;
import cn.devcms.redisfront.ui.dialog.OpenConnectDialog;
import cn.devcms.redisfront.ui.dialog.SettingDialog;
import cn.devcms.redisfront.ui.form.MainContentForm;
import com.formdev.flatlaf.FlatClientProperties;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RedisFrontFrame extends JXFrame {
    MainContentForm mainContentForm;


    public RedisFrontFrame() {
        super(" RedisFront ", true);
        setIconImages(FlatSVGUtils.createWindowIconImages("/svg/redis.svg"));
        UIManager.put("TitlePane.unifiedBackground", false);
        menuBarInit();
        initComponents();
        FlatDesktop.setAboutHandler(this::aboutActionPerformed);
        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit);
    }

    private void initComponents() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        mainContentForm = new MainContentForm(this);
        container.add(mainContentForm.getContentPanel(), BorderLayout.CENTER);
    }


    private void menuBarInit() {
        setJMenuBar(new JMenuBar() {
            {
                JMenu fileMenu = new JMenu("文件");
                fileMenu.setMnemonic('F');
                //新建连接
                JMenuItem addConnectMenu = new JMenuItem("新建连接", KeyEvent.VK_A);
                addConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
                addConnectMenu.addActionListener(e -> AddConnectDialog.showAddConnectDialog(RedisFrontFrame.this, (connectInfo -> {
                    mainContentForm.addAction();
                    System.out.println(connectInfo);
                })));
                fileMenu.add(addConnectMenu);
                //打开连接
                JMenuItem openConnectMenu = new JMenuItem("打开连接", KeyEvent.VK_S);
                openConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
                openConnectMenu.addActionListener(e -> OpenConnectDialog.showOpenConnectDialog(RedisFrontFrame.this));
                fileMenu.add(openConnectMenu);
                //配置菜单
                fileMenu.add(new JSeparator());
                JMenuItem importConfigMenu = new JMenuItem("加载配置");
                fileMenu.add(importConfigMenu);
                JMenuItem exportConfigMenu = new JMenuItem("导出配置");
                fileMenu.add(exportConfigMenu);
                //退出程序
                fileMenu.add(new JSeparator());
                JMenuItem exitMenu = new JMenuItem("退出程序");
                exitMenu.addActionListener(e -> {
                    RedisFrontFrame.this.dispose();
                    System.exit(0);
                });
                fileMenu.add(exitMenu);
                add(fileMenu);
                JMenu settingMenu = new JMenu("设置");
                fileMenu.setMnemonic('S');
                settingMenu.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        SettingDialog settingDialog = new SettingDialog(RedisFrontFrame.this);
                        settingDialog.setMinimumSize(new Dimension(500, 400));
                        settingDialog.setLocationRelativeTo(RedisFrontFrame.this);
                        settingDialog.pack();
                        settingDialog.setVisible(true);
                    }
                });
                add(settingMenu);
                JMenu aboutMenu = new JMenu("关于");
                fileMenu.setMnemonic('A');
                aboutMenu.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        aboutActionPerformed();
                    }
                });
                add(aboutMenu);
                add(Box.createGlue());
                FlatButton gitBtn = new FlatButton();
                gitBtn.setIcon(new FlatSVGIcon("icons/gitee.svg"));
                gitBtn.setButtonType(FlatButton.ButtonType.toolBarButton);
                gitBtn.setFocusable(false);
                gitBtn.setToolTipText(" 去码云给个star吧 :) ");
                gitBtn.setRolloverIcon(new FlatSVGIcon("icons/gitee_red.svg"));
                gitBtn.addActionListener(e -> {
                    try {
                        Desktop.getDesktop().browse(new URI("https://gitee.com/westboy/redis-front"));
                    } catch (IOException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                add(gitBtn);
            }
        });
    }


    private void aboutActionPerformed() {
        JLabel titleLabel = new JLabel("RedisFront");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        String link = "https://gitee.com/westboy/redis-front";
        JLabel linkLabel = new JLabel("<html><a href=\"#\">" + link + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (IOException | URISyntaxException ex) {
                    JOptionPane.showMessageDialog(linkLabel, "Failed to open '" + link + "' in browser.", "About",
                            JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        JOptionPane.showMessageDialog(RedisFrontFrame.this, new Object[]{titleLabel, "一款 Redis GUI 工具", " ", linkLabel,}, "关于",
                JOptionPane.PLAIN_MESSAGE);
    }

}
