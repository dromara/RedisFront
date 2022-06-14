package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.ui.form._DashboardForm;
import cn.devcms.redisfront.ui.form._DatabaseForm;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS;

public class TabbedComponent extends JPanel {
    private final _DashboardForm dashboardForm;
    private final _DatabaseForm databaseForm;

    public TabbedComponent() {
        setLayout(new BorderLayout());
        var contentPanel = new JTabbedPane();
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
        contentPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);

        dashboardForm = new _DashboardForm();
        databaseForm = new _DatabaseForm();
        contentPanel.addTab("数据", new FlatSVGIcon("icons/db_key.svg"), dashboardForm.getContentPanel());
        contentPanel.addTab("命令", new FlatSVGIcon("icons/db_cli.svg"), new TerminalComponent());
        contentPanel.addTab("信息", new FlatSVGIcon("icons/db_report.svg"), databaseForm.getContentPanel());
        contentPanel.addTab("日志", new FlatSVGIcon("icons/db_log.svg"), new JPanel());
        contentPanel.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            var component = tabbedPane.getSelectedComponent();
            if (component instanceof TerminalComponent terminalComponent) {

            }
        });
        add(contentPanel, BorderLayout.CENTER);
    }


}
