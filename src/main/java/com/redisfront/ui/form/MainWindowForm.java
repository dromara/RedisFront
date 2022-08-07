package com.redisfront.ui.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.commons.util.LoadingUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.ConnectService;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.component.MainTabbedPanel;
import com.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * MainWindowForm
 *
 * @author Jin
 */
public class MainWindowForm {
    private JPanel contentPanel;
    private JTabbedPane tabPanel;
    private JToolBar toolBar;
    private static MainWindowForm mainWindowForm;

    public static MainWindowForm getInstance() {
        if (Fn.isNull(mainWindowForm)) {
            mainWindowForm = new MainWindowForm();
        }
        return mainWindowForm;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public MainWindowForm() {
        $$$setupUI$$$();
        contentPanel.add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER);
    }

    public void addTabActionPerformed(ConnectInfo connectInfo) {
        {  //环境判断
            FutureUtils.supplyAsync(() -> {
                connectInfo.setRedisModeEnum(RedisBasicService.service.getRedisModeEnum(connectInfo));
                var prototype = connectInfo.clone();
                if (Enum.RedisMode.SENTINEL == connectInfo.redisModeEnum()) {
                    var masterList = LettuceUtils.sentinelExec(connectInfo, RedisSentinelCommands::masters);
                    var master = masterList.stream().findAny().orElseThrow();
                    var ip = master.get("ip");
                    var port = master.get("port");
                    LoadingUtils.closeDialog();
                    var ret = JOptionPane.showConfirmDialog(RedisFrontApplication.frame, "您连接的主机为Sentinel节点，是否重定向的到Master[ " + ip + "/" + port + " ]节点？", "连接提示", JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        connectInfo.setHost(ip);
                        connectInfo.setPort(Integer.valueOf(port));
                    }
                    try {
                        LettuceUtils.run(connectInfo, BaseRedisCommands::ping);
                    } catch (Exception e) {
                        if (e instanceof RedisException redisException) {
                            var ex = redisException.getCause();
                            if (Fn.equal(ex.getMessage(), "WRONGPASS invalid username-password pair or user is disabled.")) {
                                var password = JOptionPane.showInputDialog(RedisFrontApplication.frame, "您输入Master[ " + ip + "/" + port + " ]节点的密码！");
                                if (ret == JOptionPane.YES_OPTION) {
                                    connectInfo.setPassword(password);
                                    LettuceUtils.run(connectInfo, BaseRedisCommands::ping);
                                }
                            }
                        } else if (Fn.isNotNull(e.getCause())) {
                            var ex = e.getCause();
                            AlertUtils.showErrorDialog("Error", ex);
                        } else {
                            throw e;
                        }
                    }
                    LoadingUtils.showDialog(String.format("正在连接 - %s:%s", connectInfo.host(), connectInfo.port()));
                }
                return prototype;
            }, prototype -> {
                if (Fn.equal(prototype.id(), 0)) {
                    ConnectService.service.save(prototype);
                } else {
                    ConnectService.service.update(prototype);
                }
            }).join();
        }

        { //主界面加载
            FutureUtils.runAsync(() -> {
                var mainTabbedPanel = MainTabbedPanel.newInstance(connectInfo);
                SwingUtilities.invokeLater(() -> {
                    LoadingUtils.closeDialog();
                    this.tabPanel.addTab(connectInfo.title(), UI.MAIN_TAB_DATABASE_ICON, mainTabbedPanel);
                    this.tabPanel.setSelectedIndex(tabPanel.getTabCount() - 1);
                    this.contentPanel.add(tabPanel, BorderLayout.CENTER, 0);
                    this.toolBar.setVisible(true);
                    LoadingUtils.showDialog("loading key...");
                });
            }, throwable -> {
                LoadingUtils.closeDialog();
                AlertUtils.showErrorDialog("Error", throwable.getCause().getCause());
            }).join();
        }
    }

    private void createUIComponents() {
        UIManager.put("TabbedPane.closeHoverForeground", Color.red);
        UIManager.put("TabbedPane.selectHighlight", Color.red);
        UIManager.put("*.selectionBackground", Color.red);
        UIManager.put("TabbedPane.closePressedForeground", Color.red);
        UIManager.put("TabbedPane.closeHoverBackground", new Color(0, true));
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());
        tabPanel = new JTabbedPane();
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        //SHOW LINE
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        //SHOW CLOSE BUTTON
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);

        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_PLACEMENT, FlatClientProperties.TABBED_PANE_PLACEMENT_BOTH);
        //SHOW CLOSE BUTTON
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "关闭连接");
        //SHOW CLOSE BUTTON Callback
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
            Component component = tabbedPane.getComponentAt(tabIndex);
            if (component instanceof MainTabbedPanel mainTabbedPanel) {
                mainTabbedPanel.shutdownScheduled();
            }
            tabbedPane.removeTabAt(tabIndex);
            if (tabbedPane.getTabCount() == 0) {
                toolBar.setVisible(false);
                contentPanel.add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER, 0);
            }
            System.gc();
        });

        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, SwingConstants.LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        toolBar = new JToolBar();
        toolBar.setMinimumSize(new Dimension(-1, 200));
        toolBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        toolBar.setLayout(new BorderLayout());

        var newBtn = new JButton("日志", UI.LOGS_ICON);
        newBtn.setToolTipText("查看日志");
        newBtn.addActionListener((event) -> LogsDialog.showLogsDialog());
        toolBar.add(new JPanel() {
            {
                setLayout(new BorderLayout());
                add(newBtn, BorderLayout.CENTER);
            }
        }, BorderLayout.SOUTH);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setEnabled(true);
        tabPanel.setTabLayoutPolicy(1);
        tabPanel.setTabPlacement(2);
        contentPanel.add(tabPanel, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
