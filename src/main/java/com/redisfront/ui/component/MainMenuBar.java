package com.redisfront.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.UI;
import com.redisfront.service.ConnectService;
import com.redisfront.ui.dialog.AddConnectDialog;
import com.redisfront.ui.dialog.OpenConnectDialog;
import com.redisfront.ui.dialog.SettingDialog;
import com.redisfront.ui.form.MainWindowForm;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LocaleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
        addConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        addConnectMenu.addActionListener(e -> AddConnectDialog.showAddConnectDialog(((connectInfo) -> MainWindowForm.getInstance().addTabActionPerformed(connectInfo))));
        fileMenu.add(addConnectMenu);

        //打开连接
        var openConnectMenu = new JMenuItem(){
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
        var importConfigMenu = new JMenuItem(){
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.Import").title());
            }
        };
        fileMenu.add(importConfigMenu);

        var exportConfigMenu = new JMenuItem(){
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.File.Export").title());
            }
        };
        fileMenu.add(exportConfigMenu);

        //退出程序
        fileMenu.add(new JSeparator());
        var exitMenu = new JMenuItem(){
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

        var settingMenu = new JMenu(){
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.Setting").title());
                setMnemonic(LocaleUtils.getMenu("Menu.Setting").mnemonic());
            }
        };
        var settingMenuItem = new JMenuItem(){
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

        var aboutMenu = new JMenu(){
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMenu("Menu.Help").title());
            }
        };
        aboutMenu.setMnemonic(LocaleUtils.getMenu("Menu.Help").mnemonic());

        var aboutMenuItem = new JMenuItem(){
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
        JOptionPane.showMessageDialog(RedisFrontApplication.frame, new Object[]{titleLabel, "一款 Redis GUI 工具", " ", linkLabel,}, LocaleUtils.getMenu("Menu.Help.About").title(),
                JOptionPane.PLAIN_MESSAGE);
    }
}
