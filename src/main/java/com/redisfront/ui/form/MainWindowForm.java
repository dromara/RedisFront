package com.redisfront.ui.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.*;
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
import javax.swing.text.StyleContext;
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

    public void addTabActionPerformed(final ConnectInfo connectInfo) {
        var addTabWorker = new SwingWorker<ConnectInfo, Integer>() {
            @Override
            protected void done() {
                try {
                    var connectInfo = get();
                    FutureUtils.runAsync(LoadingUtils::closeDialog);
                    var mainTabbedPanel = MainTabbedPanel.newInstance(connectInfo);
                    tabPanel.addTab(get().title(), UI.MAIN_TAB_DATABASE_ICON, mainTabbedPanel);
                    tabPanel.setSelectedIndex(tabPanel.getTabCount() - 1);
                    contentPanel.add(tabPanel, BorderLayout.CENTER, 0);
                    toolBar.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LoadingUtils.closeDialog();
                    if (ex.getCause() != null) {
                        AlertUtils.showErrorDialog("Error", ex.getCause().getCause());
                    } else {
                        AlertUtils.showErrorDialog("Error", ex);
                    }
                }

            }

            @Override
            protected ConnectInfo doInBackground() {
                connectInfo.setRedisModeEnum(RedisBasicService.service.getRedisModeEnum(connectInfo));
                var prototype = connectInfo.clone();

                FutureUtils.runAsync(() -> {
                    if (Fn.equal(prototype.id(), 0)) {
                        ConnectService.service.save(prototype);
                    } else {
                        ConnectService.service.update(prototype);
                    }
                });

                if (Enum.RedisMode.SENTINEL == connectInfo.redisModeEnum()) {
                    var masterList = LettuceUtils.sentinelExec(connectInfo, RedisSentinelCommands::masters);
                    var master = masterList.stream().findAny().orElseThrow();
                    var ip = master.get("ip");
                    var port = master.get("port");
                    LoadingUtils.closeDialog();
                    var ret = JOptionPane.showConfirmDialog(RedisFrontApplication.frame, String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.JOptionPane.showConfirmDialog.message"), ip, port), "连接提示", JOptionPane.YES_NO_OPTION);
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
                                var password = JOptionPane.showInputDialog(RedisFrontApplication.frame, String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.JOptionPane.showInputDialog.message"), ip, port));
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
                    LoadingUtils.showDialog(String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.connection.message"), connectInfo.host(), connectInfo.port()));
                }
                return connectInfo;
            }
        };
        addTabWorker.execute();
    }

    private void createUIComponents() {
        UIManager.put("TabbedPane.closeHoverForeground", Color.red);
        UIManager.put("TabbedPane.selectHighlight", Color.red);
        UIManager.put("*.selectionBackground", Color.red);
        UIManager.put("TabbedPane.closePressedForeground", Color.red);
        UIManager.put("TabbedPane.closeHoverBackground", new Color(0, true));
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());
        {
            tabPanel = new JTabbedPane();
            tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
            //SHOW LINE
            tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
            //SHOW CLOSE BUTTON
            tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);

            tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_PLACEMENT, FlatClientProperties.TABBED_PANE_PLACEMENT_BOTH);
            //SHOW CLOSE BUTTON
            tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, LocaleUtils.getMessageFromBundle("MainWindowForm.tabPanel.closeTooltip.text"));
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
        }
        {
            toolBar = new JToolBar();
            toolBar.setMinimumSize(new Dimension(-1, 200));
            toolBar.setBorder(new EmptyBorder(10, 10, 10, 10));
            toolBar.setLayout(new BorderLayout());

            var logViewBtn = new JButton(UI.LOGS_ICON) {
                @Override
                public void updateUI() {
                    super.updateUI();
                    var newFont = UIManager.getFont("defaultFont").deriveFont(12f);
                    setFont(newFont);
                    setText(LocaleUtils.getMessageFromBundle("MainWindowForm.logViewBtn.title"));
                    setToolTipText(LocaleUtils.getMessageFromBundle("MainWindowForm.logViewBtn.ToolTipText"));
                    setForeground(new Color(112, 112, 112));
                    setBorder(new FlatLineBorder(new Insets(2, 2, 2, 2), new Color(112, 112, 112), 1, 10));
                }
            };
            logViewBtn.addActionListener((event) -> LogsDialog.showLogsDialog());
            toolBar.add(logViewBtn, BorderLayout.SOUTH);
            tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);
        }
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
