package com.redisfront.ui.component;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.redisfront.constant.UI;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
import com.redisfront.ui.form.fragment.DataChartsForm;
import com.redisfront.ui.form.fragment.DataSearchForm;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
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

        contentPanel.addTab("数据", UI.CONTENT_TAB_DATA_ICON, DataSplitPanel.newInstance(connectInfo), "数据界面");
        contentPanel.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, TerminalComponent.newInstance(connectInfo), "命令界面");
        contentPanel.addTab("信息", UI.CONTENT_TAB_INFO_ICON, DataChartsForm.getInstance().contentPanel(), "信息界面");

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
        scheduledExecutor.scheduleAtFixedRate(() -> {
            Long keysCount = RedisService.service.countDatabaseKey(connectInfo);
            keysInfo.setText(keysCount.toString());
            Map<String, Object> stats = RedisService.service.getStatInfo(connectInfo);
            cupInfo.setText((String) stats.get("instantaneous_ops_per_sec"));
            cupInfo.setToolTipText("每秒命令数：" + stats.get("instantaneous_ops_per_sec"));

            Map<String, Object> memory = RedisService.service.getMemoryInfo(connectInfo);
            memoryInfo.setText((String) memory.get("used_memory_human"));
            memoryInfo.setToolTipText("内存占用：" + memory.get("used_memory_human"));
        }, 0, 5, TimeUnit.SECONDS);
    }


}
