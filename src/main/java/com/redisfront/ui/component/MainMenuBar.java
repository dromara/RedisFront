package com.redisfront.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.redisfront.RedisFrontApplication;
import com.redisfront.service.ConnectService;
import com.redisfront.ui.dialog.AddConnectDialog;
import com.redisfront.ui.dialog.OpenConnectDialog;
import com.redisfront.ui.dialog.SettingDialog;
import com.redisfront.ui.form.MainWindowForm;
import com.redisfront.util.LocaleUtil;

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

    private static final LocaleUtil.BundleInfo FILE_MENU = LocaleUtil.getMenu("Menu.File");
    private static final LocaleUtil.BundleInfo FILE_MENU_NEW_ITEM = LocaleUtil.getMenu("Menu.File.New");
    private static final LocaleUtil.BundleInfo FILE_MENU_OPEN_ITEM = LocaleUtil.getMenu("Menu.File.Open");
    private static final LocaleUtil.BundleInfo FILE_MENU_IMPORT_ITEM = LocaleUtil.getMenu("Menu.File.Import");
    private static final LocaleUtil.BundleInfo FILE_MENU_EXPORT_ITEM = LocaleUtil.getMenu("Menu.File.Export");
    private static final LocaleUtil.BundleInfo FILE_MENU_EXIT_ITEM = LocaleUtil.getMenu("Menu.File.Exit");
    private static final LocaleUtil.BundleInfo SETTING_MENU = LocaleUtil.getMenu("Menu.Setting");
    private static final LocaleUtil.BundleInfo ABOUT_MENU = LocaleUtil.getMenu("Menu.About");

    public static JMenuBar getInstance() {
        return new MainMenuBar();
    }

    public MainMenuBar() {

        FlatDesktop.setAboutHandler(this::aboutActionPerformed);
        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit);

        var fileMenu = new JMenu(FILE_MENU.title());
        fileMenu.setMnemonic(FILE_MENU.mnemonic());
        fileMenu.setToolTipText(FILE_MENU.desc());
        //新建连接
        var addConnectMenu = new JMenuItem(FILE_MENU_NEW_ITEM.title(), FILE_MENU_NEW_ITEM.mnemonic());
        addConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        addConnectMenu.addActionListener(e -> AddConnectDialog.showAddConnectDialog(MainWindowForm.getInstance()::addActionPerformed));
        fileMenu.add(addConnectMenu);
        //打开连接
        var openConnectMenu = new JMenuItem(FILE_MENU_OPEN_ITEM.title(), FILE_MENU_OPEN_ITEM.mnemonic());
        openConnectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        openConnectMenu.addActionListener(e -> OpenConnectDialog.showOpenConnectDialog(
                //打开连接回调
                (MainWindowForm.getInstance()::addActionPerformed),
                //编辑连接回调
                (connectInfo -> AddConnectDialog.showEditConnectDialog(
                        connectInfo,
                        (MainWindowForm.getInstance()::addActionPerformed)
                )),
                //删除连接回调
                (connectInfo -> ConnectService.service.delete(connectInfo.id())))
        );
        fileMenu.add(openConnectMenu);
        //配置菜单
        fileMenu.add(new JSeparator());
        var importConfigMenu = new JMenuItem(FILE_MENU_IMPORT_ITEM.title());
        fileMenu.add(importConfigMenu);
        var exportConfigMenu = new JMenuItem(FILE_MENU_EXPORT_ITEM.title());
        fileMenu.add(exportConfigMenu);
        //退出程序
        fileMenu.add(new JSeparator());
        var exitMenu = new JMenuItem(FILE_MENU_EXIT_ITEM.title());
        exitMenu.addActionListener(e -> {
            RedisFrontApplication.frame.dispose();
            System.exit(0);
        });
        fileMenu.add(exitMenu);
        add(fileMenu);

        var settingMenu = new JMenu(SETTING_MENU.title());
        settingMenu.setMnemonic(SETTING_MENU.mnemonic());
        settingMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SettingDialog.showSettingDialog();
            }
        });
        add(settingMenu);

        var aboutMenu = new JMenu(ABOUT_MENU.title());
        aboutMenu.setMnemonic(ABOUT_MENU.mnemonic());
        aboutMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                aboutActionPerformed();
            }
        });
        add(aboutMenu);

        add(Box.createGlue());

        var gitBtn = new FlatButton();
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
        JOptionPane.showMessageDialog(RedisFrontApplication.frame, new Object[]{titleLabel, "一款 Redis GUI 工具", " ", linkLabel,}, ABOUT_MENU.title(),
                JOptionPane.PLAIN_MESSAGE);
    }
}
