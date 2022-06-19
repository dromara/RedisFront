package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.service.RedisService;
import cn.devcms.redisfront.ui.form._DashboardForm;
import cn.devcms.redisfront.ui.form._DatabaseForm;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS;

public class TabbedComponent extends JPanel {

    public TabbedComponent(ConnectInfo connectInfo) {
        setLayout(new BorderLayout());
        var contentPanel = new JTabbedPane();
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
        contentPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        var leftToolBar = new FlatToolBar();
        var leftToolBarLayout = new FlowLayout();
        leftToolBarLayout.setAlignment(FlowLayout.CENTER);
        leftToolBar.setLayout(leftToolBarLayout);

        //host info
        var hostInfo = new FlatLabel();
        hostInfo.setText(connectInfo.host() + ":" + connectInfo.port());
        hostInfo.setIcon(new FlatSVGIcon("icons/host.svg"));
        leftToolBar.add(hostInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);

        //host info
        var rightToolBar = new FlatToolBar();
        var rightToolBarLayout = new FlowLayout();
        rightToolBarLayout.setAlignment(FlowLayout.CENTER);
        rightToolBar.setLayout(rightToolBarLayout);

        //keysInfo
        var keysInfo = new FlatLabel();
        keysInfo.setText("0");
        keysInfo.setIcon(new FlatSVGIcon("icons/key.svg"));
        rightToolBar.add(keysInfo);

        //cupInfo
        var cupInfo = new FlatLabel();
        cupInfo.setText("0");
        cupInfo.setIcon(new FlatSVGIcon("icons/process.svg"));
        rightToolBar.add(cupInfo);

        //memoryInfo
        var memoryInfo = new FlatLabel();
        memoryInfo.setText("0.0");
        memoryInfo.setIcon(new FlatSVGIcon("icons/memory.svg"));
        rightToolBar.add(memoryInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);

        contentPanel.addTab("数据", new FlatSVGIcon("icons/db_key2.svg"), new _DashboardForm().getContentPanel());
        contentPanel.addTab("命令", new FlatSVGIcon("icons/db_cli2.svg"), new TerminalComponent());
        contentPanel.addTab("信息", new FlatSVGIcon("icons/db_report2.svg"), new _DatabaseForm().getContentPanel());

        contentPanel.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            var component = tabbedPane.getSelectedComponent();
            if (component instanceof TerminalComponent terminalComponent) {

            }
        });
        add(contentPanel, BorderLayout.CENTER);

        var timer = new Timer(1000, (e) -> {
            Long keysCount = RedisService.service.getKeyCount(connectInfo);
            keysInfo.setText(keysCount.toString());

            Map<String, Object> stats = RedisService.service.getStatInfo(connectInfo);
            cupInfo.setText((String) stats.get("instantaneous_ops_per_sec"));
            cupInfo.setToolTipText("每秒命令数：" + stats.get("instantaneous_ops_per_sec"));

            Map<String, Object> memory = RedisService.service.getMemoryInfo(connectInfo);
            memoryInfo.setText((String) memory.get("used_memory_human"));
            memoryInfo.setToolTipText("内存占用：" + (String) memory.get("used_memory_human"));
        });
        timer.start();
    }


}
