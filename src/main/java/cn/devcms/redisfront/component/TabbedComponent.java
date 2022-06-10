package cn.devcms.redisfront.component;

import cn.devcms.redisfront.ui.form.DashboardForm;
import cn.devcms.redisfront.ui.form.DatabaseForm;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS;

public class TabbedComponent extends JPanel {
    private final JTabbedPane contentPanel;
    private final DashboardForm dashboardForm;
    private final DatabaseForm databaseForm;

    @SneakyThrows
    public TabbedComponent() {
        setLayout(new BorderLayout());
        contentPanel = new JTabbedPane();
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
        contentPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);

        dashboardForm = new DashboardForm();
        databaseForm = new DatabaseForm();
        contentPanel.addTab("数据", new FlatSVGIcon("icons/db_key.svg"), dashboardForm.getContentPanel());
        contentPanel.addTab("命令", new FlatSVGIcon("icons/db_cli.svg"), new TerminalComponent());
        contentPanel.addTab("信息", new FlatSVGIcon("icons/db_report.svg"), databaseForm.getContentPanel());
        contentPanel.addTab("日志", new FlatSVGIcon("icons/db_log.svg"), new JPanel());
        contentPanel.addChangeListener(e -> {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            Component component = tabbedPane.getSelectedComponent();
            if (component instanceof TerminalComponent terminalComponent) {
                terminalComponent.pollInputStart();
            }

        });
        add(contentPanel, BorderLayout.CENTER);
    }

}
