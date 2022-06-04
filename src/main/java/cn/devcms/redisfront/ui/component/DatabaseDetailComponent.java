package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.ui.form.CommandForm;
import cn.devcms.redisfront.ui.form.DashboardForm;
import cn.devcms.redisfront.ui.form.DatabaseForm;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class DatabaseDetailComponent extends JPanel {
    private final JTabbedPane contentPanel;
    private final CommandForm commandForm;
    private final DashboardForm dashboardForm;
    private final DatabaseForm databaseForm;


    public DatabaseDetailComponent() {
        setLayout(new BorderLayout());
        contentPanel = new JTabbedPane();
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
//        contentPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, TABBED_PANE_TAB_TYPE_UNDERLINED);

        dashboardForm = new DashboardForm();
        databaseForm = new DatabaseForm();
        commandForm = new CommandForm();

        contentPanel.addTab("数据库", new FlatSVGIcon("icons/db_key.svg"), dashboardForm.$$$getRootComponent$$$());
        contentPanel.addTab("命令行", new FlatSVGIcon("icons/db_cli.svg"), commandForm.$$$getRootComponent$$$());
        contentPanel.addTab("信息", new FlatSVGIcon("icons/db_report.svg"), databaseForm.$$$getRootComponent$$$());
        contentPanel.addTab("日志", new FlatSVGIcon("icons/db_log.svg"), new JPanel());
        add(contentPanel, BorderLayout.CENTER);
    }

}
