package com.redisfront.ui.component;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.ConnectService;
import com.redisfront.ui.dialog.AddConnectDialog;
import com.redisfront.ui.dialog.OpenConnectDialog;
import com.redisfront.ui.dialog.SettingDialog;
import com.redisfront.ui.form.MainNoneForm;
import com.redisfront.ui.form.MainWindowForm;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LocaleUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        addConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
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
        openConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
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
        importConfigMenu.addActionListener((e) -> showFileImportDialog());
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
        add(aboutMenu);

        add(Box.createGlue());

        var gitBtn = new FlatButton();
        gitBtn.setIcon(UI.GITEE_ICON);
        gitBtn.setButtonType(FlatButton.ButtonType.toolBarButton);
        gitBtn.setFocusable(false);
        gitBtn.setToolTipText(" 去码云给个star吧 :) ");
        gitBtn.setRolloverIcon(UI.GITEE_RED_ICON);
        gitBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://gitee.com/westboy/redis-front"));
            } catch (IOException | URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        });
        add(gitBtn);
    }

    private void aboutActionPerformed() {
        var titleLabel = new JLabel("RedisFront");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");
        var link = "https://gitee.com/westboy/redis-front";
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
        JOptionPane.showMessageDialog(RedisFrontApplication.frame, new Object[]{titleLabel, "Cross-platform redis gui clinet", "Version " + Const.APP_VERSION, linkLabel}, LocaleUtils.getMenu("Menu.Help.About").title(),
                JOptionPane.PLAIN_MESSAGE);
    }

    /*
     * 选择文件保存路径
     */
    private void showFileSaveDialog() {
        // 创建一个默认的文件选取器
        var fileChooser = new JFileChooser();
        var connectInfos = ConnectService.service.getAllConnectList();
        var configure = JSONUtil.toJsonStr(connectInfos);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        // 设置打开文件选择框后默认输入的文件名
        fileChooser.setSelectedFile(new File("configure.json"));
        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        var result = fileChooser.showSaveDialog(MainNoneForm.getInstance().getContentPanel());

        if (result == JFileChooser.APPROVE_OPTION) {
            // 如果点击了"保存", 则获取选择的保存路径
            var file = fileChooser.getSelectedFile();
            try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                writer.write(configure);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showFileImportDialog() {
        // 创建一个默认的文件选取器
        var fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        // 仅支持选择文件
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 设置文件过滤，仅支持json配置导入
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.getName().endsWith("json")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return ".json";
            }
        });
        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        var result = fileChooser.showDialog(MainNoneForm.getInstance().getContentPanel(), LocaleUtils.getMessageFromBundle("ConfigImport.saveBtn.title"));

        if (result == JFileChooser.APPROVE_OPTION) { // 说明选中了文件
            var configFile = fileChooser.getSelectedFile();
            var fileReader = new FileReader(configFile);
            if (!StrUtil.isBlankIfStr(fileReader.readString()) && JSONUtil.isTypeJSON(fileReader.readString())) {
                try {
                    if (JSONUtil.isTypeJSONObject(fileReader.readString())) {
                        var connectInfo = JSONUtil.toBean(fileReader.readString(), ConnectInfo.class);
                        ConnectService.service.save(connectInfo);
                    } else {
                        var connectInfos = JSONUtil.toList(fileReader.readString(), ConnectInfo.class);
                        for (ConnectInfo connectInfo : connectInfos) {
                            ConnectService.service.save(connectInfo);
                        }
                    }
                    AlertUtils.showInformationDialog("导入完成");
                } catch (IORuntimeException e) {
                    AlertUtils.showErrorDialog("配置读取异常", e);
                } catch (JSONException je) {
                    AlertUtils.showErrorDialog("配置文件转换出现异常", je);
                }
            } else {
                AlertUtils.showInformationDialog("配置无法正常读取");
            }
        }
    }
}
