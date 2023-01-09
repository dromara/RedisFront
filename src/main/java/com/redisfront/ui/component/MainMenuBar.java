package com.redisfront.ui.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.util.SystemInfo;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LocaleUtils;
import com.redisfront.service.ConnectService;
import com.redisfront.ui.dialog.AddConnectDialog;
import com.redisfront.ui.dialog.ImportConfigDialog;
import com.redisfront.ui.dialog.OpenConnectDialog;
import com.redisfront.ui.dialog.SettingDialog;
import com.redisfront.ui.form.MainWindowForm;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * MainMenuBar
 *
 * @author Jin
 */
public class MainMenuBar extends JMenuBar {

    public static JMenuBar getInstance() {
        return new MainMenuBar();
    }

    public MainMenuBar() {
        FlatDesktop.setAboutHandler(this::aboutActionPerformed);
        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit);
        initMenu();
    }

    public void initMenu() {

        var fileMenu = new JMenu() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File").title());
            }
        };
        fileMenu.setMnemonic(LocaleUtils.getMenu("Menu.File").mnemonic());
        //新建连接
        var addConnectMenu = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.New").title());
                setMnemonic(LocaleUtils.getMenu("Menu.File.New").mnemonic());
            }
        };
        if (SystemInfo.isMacOS) {
            addConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        } else {
            addConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        }

        addConnectMenu.addActionListener(e -> AddConnectDialog.showAddConnectDialog(((connectInfo) -> MainWindowForm.getInstance().addTabActionPerformed(connectInfo))));
        fileMenu.add(addConnectMenu);

        //打开连接
        var openConnectMenu = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.Open").title());
                setMnemonic(LocaleUtils.getMenu("Menu.File.Open").mnemonic());
            }
        };
        if (SystemInfo.isMacOS) {
            openConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        } else {
            openConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        }

        openConnectMenu.addActionListener(e -> OpenConnectDialog.showOpenConnectDialog(
                //打开连接回调
                ((connectInfo) -> MainWindowForm.getInstance().addTabActionPerformed(connectInfo)),
                //编辑连接回调
                (connectInfo -> AddConnectDialog.showEditConnectDialog(connectInfo, (connectInfo1) -> MainWindowForm.getInstance().addTabActionPerformed(connectInfo1))),
                //删除连接回调
                (connectInfo -> ConnectService.service.delete(connectInfo.id())))
        );
        fileMenu.add(openConnectMenu);
        //配置菜单
        fileMenu.add(new JSeparator());

        // 导入配置
        var importConfigMenu = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.Import").title());
            }
        };
        importConfigMenu.addActionListener(e -> ImportConfigDialog.showImportDialog());
        fileMenu.add(importConfigMenu);

        // 导出配置
        var exportConfigMenu = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.Export").title());
            }
        };
        // 导出配置逻辑
        exportConfigMenu.addActionListener((e) -> showFileSaveDialog());
        fileMenu.add(exportConfigMenu);

        //退出程序
        fileMenu.add(new JSeparator());
        var exitMenu = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.Exit").title());
            }
        };
        exitMenu.addActionListener(e -> {
            RedisFrontApplication.frame.dispose();
            System.exit(0);
        });
        fileMenu.add(exitMenu);
        add(fileMenu);

        var settingMenu = new JMenu() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.Setting").title());
                setMnemonic(LocaleUtils.getMenu("Menu.Setting").mnemonic());
            }
        };
        var settingMenuItem = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.Setting.global").title());
            }
        };
        settingMenuItem.addActionListener(e -> FutureUtils.runAsync(SettingDialog::showSettingDialog));
        settingMenu.add(settingMenuItem);


//        var languageMenu = new JMenu("语言设置");
//        var chineseItem = new JMenuItem("简体中文");
//        languageMenu.add(chineseItem);
//        var englishItem = new JMenuItem("英文");
//        languageMenu.add(englishItem);
//        settingMenu.add(languageMenu);

        add(settingMenu);

        var aboutMenu = new JMenu() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.Help").title());
            }
        };
        aboutMenu.setMnemonic(LocaleUtils.getMenu("Menu.Help").mnemonic());

        var aboutMenuItem = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.Help.About").title());
                setMnemonic(LocaleUtils.getMenu("Menu.Help.About").mnemonic());
            }
        };

        aboutMenuItem.addActionListener((e) -> FutureUtils.runAsync(this::aboutActionPerformed));
        aboutMenu.add(aboutMenuItem);
        aboutMenu.add(new JSeparator());
        var dromaraItem = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText("Dromara - 开源技术社区");
            }
        };
        dromaraItem.addActionListener((e) -> FutureUtils.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://dromara.org/"));
            } catch (IOException | URISyntaxException ex) {
                AlertUtils.showInformationDialog("打开浏览器失败！");
            }
        }));
        aboutMenu.add(dromaraItem);

//        aboutMenu.add(new JSeparator());
        var aliYunItem = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText("<html><b color=\"orange\">阿里云</b><i color=\"red\">~新人特惠</i></html>");
            }
        };
        aliYunItem.addActionListener((e) -> FutureUtils.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.aliyun.com/daily-act/ecs/activity_selection?userCode=fdfwuy9i"));
            } catch (IOException | URISyntaxException ex) {
                AlertUtils.showInformationDialog("打开浏览器失败！");
            }
        }));
//        aboutMenu.add(aliYunItem);
        var qiNiuItem = new JMenuItem() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText("<html><b color=\"#07BEFF\">七牛云存储</b><i color=\"red\">~免费20G</i></html>");
            }
        };
        qiNiuItem.addActionListener((e) -> FutureUtils.runAsync(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://s.qiniu.com/7V7vAb"));
            } catch (IOException | URISyntaxException ex) {
                AlertUtils.showInformationDialog("打开浏览器失败！");
            }
        }));
//        aboutMenu.add(qiNiuItem);

        add(aboutMenu);

        add(Box.createGlue());

        var gitBtn = new FlatButton();
        gitBtn.setIcon(UI.GITHUB_ICON);
        gitBtn.setButtonType(FlatButton.ButtonType.toolBarButton);
        gitBtn.setFocusable(false);
        gitBtn.setToolTipText("https://github.com/westboy/redisfront");
        gitBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/westboy/redisfront"));
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
        add(gitBtn);
    }

    private void aboutActionPerformed() {
        var titleLabel = new JLabel("RedisFront");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        var link = "https://www.redisfront.com";
        var linkLabel = new JLabel("<html><a href=\"#\">" + link + "</a></html>");
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

        JOptionPane.showMessageDialog(RedisFrontApplication.frame, new Object[]{
                        new JPanel() {
                            {
                                setLayout(new BorderLayout());
                                add(new JLabel(UI.REDIS_ICON), BorderLayout.WEST);
                                add(new JPanel() {
                                    {
                                        setLayout(new BorderLayout());
                                        setBorder(new EmptyBorder(10, 10, 10, 10));
                                        add(titleLabel, BorderLayout.NORTH);
                                        add(new JLabel("Cross-platform redis gui clinet"), BorderLayout.CENTER);
                                        add(new JPanel() {
                                            {
                                                setLayout(new BorderLayout());
                                                add(new JLabel("Version " + Const.APP_VERSION), BorderLayout.NORTH);
                                                add(linkLabel, BorderLayout.CENTER);
                                            }
                                        }, BorderLayout.SOUTH);

                                    }
                                }, BorderLayout.CENTER);
                            }
                        }
                }, LocaleUtils.getMenu("Menu.Help.About").title(),
                JOptionPane.PLAIN_MESSAGE);
    }

    /*
     * 选择文件保存路径
     */
    private void showFileSaveDialog() {
        if (!FileUtil.exist(Const.CONFIG_DATA_PATH)) {
            FileUtil.mkdir(Const.CONFIG_DATA_PATH);
        }
        // 创建一个默认的文件选取器
        var fileChooser = new JFileChooser(Const.CONFIG_DATA_PATH);
        var getAllConnectListFuture = FutureUtils.supplyAsync(ConnectService.service::getAllConnectList);
        // 设置打开文件选择框后默认输入的文件名
        fileChooser.setSelectedFile(new File("configure.json"));
        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        var result = fileChooser.showSaveDialog(RedisFrontApplication.frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            getAllConnectListFuture.thenAccept(connectList -> {
                // 如果点击了"保存", 则获取选择的保存路径
                var configFile = fileChooser.getSelectedFile();
                var configJson = JSONUtil.toJsonStr(connectList);
                FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(configJson), configFile);
            });
        }
    }
}
