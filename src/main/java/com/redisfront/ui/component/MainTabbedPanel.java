package com.redisfront.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.redisfront.constant.UI;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
import com.redisfront.ui.form.fragment.DataChartsForm;
import com.redisfront.util.ExecutorUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainTabbedPanel extends JPanel {

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public static MainTabbedPanel newInstance(ConnectInfo connectInfo) {
        return new MainTabbedPanel(connectInfo);
    }

    public void shutdownScheduled() {
        scheduledExecutor.shutdown();
    }

    public MainTabbedPanel(ConnectInfo connectInfo) {
        setLayout(new BorderLayout());
        var contentPanel = new JTabbedPane();
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        var leftToolBar = new FlatToolBar();
        var leftToolBarLayout = new FlowLayout();
        leftToolBarLayout.setAlignment(FlowLayout.CENTER);
        leftToolBar.setLayout(leftToolBarLayout);
        leftToolBar.setPreferredSize(new Dimension(50, -1));

        //host info
        var hostInfo = new FlatLabel();
        hostInfo.setText(connectInfo.host() + ":" + connectInfo.port() + " - " + connectInfo.redisModeEnum().modeName);

        var buf = new StringBuilder(1500);
        buf.append("<html><style>");
        buf.append("td { padding: 0 10 0 0; }");
        buf.append("</style><table>");
        var serverInfo = RedisService.service.getServerInfo(connectInfo);
        String version = (String) serverInfo.get("redis_version");
        appendRow(buf, "Redis版本", version);

        String port = (String) serverInfo.get("tcp_port");
        appendRow(buf, "连接端口", port);

        String os = (String) serverInfo.get("os");
        appendRow(buf, "操作系统", os);

        String redisMode = (String) serverInfo.get("redis_mode");
        appendRow(buf, "Redis模式", redisMode);

        String configFile = (String) serverInfo.get("config_file");
        appendRow(buf, "配置文件", configFile);

        buf.append("</td></tr>");
        buf.append("</table></html>");
        hostInfo.setToolTipText(buf.toString());
        hostInfo.setIcon(UI.CONTENT_TAB_HOST_ICON);

        leftToolBar.add(hostInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);

        //host info
        var rightToolBar = new FlatToolBar();
        var rightToolBarLayout = new FlowLayout();
        rightToolBarLayout.setAlignment(FlowLayout.CENTER);
        rightToolBar.setLayout(rightToolBarLayout);

        rightToolBar.setPreferredSize(new Dimension(50, -1));

        //keysInfo
        var keysInfo = new FlatLabel();
        keysInfo.setText("0");
        keysInfo.setIcon(UI.CONTENT_TAB_KEYS_ICON);
        rightToolBar.add(keysInfo);

        //cupInfo
        var cupInfo = new FlatLabel();
        cupInfo.setText("0");
        cupInfo.setIcon(UI.CONTENT_TAB_CPU_ICON);
        rightToolBar.add(cupInfo);

        //memoryInfo
        var memoryInfo = new FlatLabel();
        memoryInfo.setText("0.0");
        memoryInfo.setIcon(UI.CONTENT_TAB_MEMORY_ICON);
        rightToolBar.add(memoryInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);

        contentPanel.addTab("数据", UI.CONTENT_TAB_DATA_ICON, DataSplitPanel.newInstance(connectInfo));
        contentPanel.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, TerminalComponent.newInstance(connectInfo));
        contentPanel.addTab("信息", UI.CONTENT_TAB_INFO_ICON, DataChartsForm.getInstance().contentPanel());

        //tab 切换事件
        contentPanel.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            var component = tabbedPane.getSelectedComponent();
            if (component instanceof TerminalComponent terminalComponent) {
                terminalComponent.ping();
            }
            if (component instanceof DataSplitPanel dataSplitPanel) {
                dataSplitPanel.ping();
            }
        });

        add(contentPanel, BorderLayout.CENTER);
        threadInit(connectInfo, keysInfo, cupInfo, memoryInfo);
    }

    private void threadInit(ConnectInfo connectInfo, FlatLabel keysInfo, FlatLabel cupInfo, FlatLabel memoryInfo) {
        scheduledExecutor.scheduleAtFixedRate(() ->
                CompletableFuture.allOf(
                        CompletableFuture.supplyAsync(() -> RedisService.service.countDatabaseKey(connectInfo), ExecutorUtil.getExecutorService()).thenAccept(keysCount ->
                                SwingUtilities.invokeLater(() -> {
                                    keysInfo.setText(keysCount.toString());
                                    keysInfo.setToolTipText("Key数量：" + keysCount);
                                })),
                        CompletableFuture.supplyAsync(() -> RedisService.service.getStatInfo(connectInfo), ExecutorUtil.getExecutorService()).thenAccept(stats ->
                                SwingUtilities.invokeLater(() -> {
                                    cupInfo.setText((String) stats.get("instantaneous_ops_per_sec"));
                                    cupInfo.setToolTipText("每秒命令数：" + stats.get("instantaneous_ops_per_sec"));
                                })),
                        CompletableFuture.supplyAsync(() -> RedisService.service.getMemoryInfo(connectInfo), ExecutorUtil.getExecutorService()).thenAccept(memory ->
                                SwingUtilities.invokeLater(() -> {
                                    memoryInfo.setText((String) memory.get("used_memory_human"));
                                    memoryInfo.setToolTipText("内存占用：" + memory.get("used_memory_human"));
                                }))), 0, 5, TimeUnit.SECONDS);
    }

    private void appendRow(StringBuilder buf, String key, String value) {
        buf.append("<tr><td valign=\"top\">")
                .append(key)
                .append(":</td><td>")
                .append(value)
                .append("</td></tr>");
    }


}
