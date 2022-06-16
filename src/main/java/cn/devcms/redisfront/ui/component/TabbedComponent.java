package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.ui.form._DashboardForm;
import cn.devcms.redisfront.ui.form._DatabaseForm;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS;

public class TabbedComponent extends JPanel {
    private final _DashboardForm dashboardForm;
    private final _DatabaseForm databaseForm;

    public TabbedComponent() {
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

        var hostInfo = new FlatLabel();
        hostInfo.setText("127.0.0.1:6379");
        hostInfo.setIcon(new FlatSVGIcon("icons/host.svg"));
        leftToolBar.add(hostInfo);

        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);

        var rightToolBar = new FlatToolBar();
        var rightToolBarLayout = new FlowLayout();
        rightToolBarLayout.setAlignment(FlowLayout.CENTER);
        rightToolBar.setLayout(rightToolBarLayout);

        var keysInfo = new FlatLabel();
        keysInfo.setText("60001");
        keysInfo.setIcon(new FlatSVGIcon("icons/key.svg"));
        rightToolBar.add(keysInfo);

        var cupInfo = new FlatLabel();
        cupInfo.setText("100%");
        cupInfo.setIcon(new FlatSVGIcon("icons/CPU.svg"));
        rightToolBar.add(cupInfo);

        var memoryInfo = new FlatLabel();
        memoryInfo.setText("825.26K");
        memoryInfo.setIcon(new FlatSVGIcon("icons/memory.svg"));
        rightToolBar.add(memoryInfo);

        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);

        dashboardForm = new _DashboardForm();
        databaseForm = new _DatabaseForm();
        contentPanel.addTab("数据", new FlatSVGIcon("icons/db_key2.svg"), dashboardForm.getContentPanel());
        contentPanel.addTab("命令", new FlatSVGIcon("icons/db_cli2.svg"), new TerminalComponent());
        contentPanel.addTab("信息", new FlatSVGIcon("icons/db_report2.svg"), databaseForm.getContentPanel());
        contentPanel.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            var component = tabbedPane.getSelectedComponent();
            if (component instanceof TerminalComponent terminalComponent) {

            }
        });
        add(contentPanel, BorderLayout.CENTER);
    }


}
