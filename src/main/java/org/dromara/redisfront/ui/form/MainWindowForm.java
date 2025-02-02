package org.dromara.redisfront.ui.form;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import org.dromara.redisfront.RedisFrontMain;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.utils.*;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.panel.MainTabbedPanel;
import org.dromara.redisfront.ui.dialog.LogsDialog;
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
        contentPanel.add(MainNoneForm.getInstance(), BorderLayout.CENTER);
    }

    public void addTabActionPerformed(final RedisConnectContext redisConnectContext) {
        var addTabWorker = new SwingWorker<RedisConnectContext, Integer>() {
            @Override
            protected void done() {
                try {
                    var connectInfo = get();
                    FutureUtils.runAsync(LoadingUtils::closeDialog);
                    var mainTabbedPanel = MainTabbedPanel.newInstance(connectInfo);
                    tabPanel.addTab(get().getTitle(), Icons.MAIN_TAB_DATABASE_ICON, mainTabbedPanel);
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
            protected RedisConnectContext doInBackground() {
                redisConnectContext.setRedisMode(RedisBasicService.service.getRedisModeEnum(redisConnectContext));
                var prototype = redisConnectContext.clone();

                FutureUtils.runAsync(() -> {
                    if (Fn.equal(prototype.getId(), 0)) {
//                        ConnectDetailDao.DAO.save(prototype);
                    } else {
//                        ConnectDetailDao.DAO.update(prototype);
                    }
                });

                if (RedisMode.SENTINEL == redisConnectContext.getRedisMode()) {
                    var masterList = LettuceUtils.sentinelExec(redisConnectContext, RedisSentinelCommands::masters);
                    var master = masterList.stream().findAny().orElseThrow();
                    var ip = master.get("ip");
                    var port = master.get("port");
                    LoadingUtils.closeDialog();
                    var ret = JOptionPane.showConfirmDialog(RedisFrontMain.frame, String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.JOptionPane.showConfirmDialog.message"), ip, port), "连接提示", JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        redisConnectContext.setHost(ip);
                        redisConnectContext.setPort(Integer.valueOf(port));
                    }
                    try {
                        LettuceUtils.run(redisConnectContext, BaseRedisCommands::ping);
                    } catch (Exception e) {
                        if (e instanceof RedisException redisException) {
                            var ex = redisException.getCause();
                            if (Fn.equal(ex.getMessage(), "WRONGPASS invalid username-password pair or user is disabled.")) {
                                var password = JOptionPane.showInputDialog(RedisFrontMain.frame, String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.JOptionPane.showInputDialog.message"), ip, port));
                                if (ret == JOptionPane.YES_OPTION) {
                                    redisConnectContext.setPassword(password);
                                    LettuceUtils.run(redisConnectContext, BaseRedisCommands::ping);
                                }
                            }
                        } else if (Fn.isNotNull(e.getCause())) {
                            var ex = e.getCause();
                            AlertUtils.showErrorDialog("Error", ex);
                        } else {
                            throw e;
                        }
                    }
                    LoadingUtils.showDialog(String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.connection.message"), redisConnectContext.getHost(), redisConnectContext.getPort()));
                }
                return redisConnectContext;
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
                    contentPanel.add(MainNoneForm.getInstance(), BorderLayout.CENTER, 0);
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

            var logViewBtn = new JButton(Icons.LOGS_ICON) {
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
