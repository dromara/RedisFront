package org.dromara.redisfront.ui.components.info;

import com.formdev.flatlaf.FlatClientProperties;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Map;
import java.util.WeakHashMap;

public class RedisInfoPanel extends JPanel {
    private static final String[] INFO_SECTIONS = {"server", "memory", "clients", "stats", "cpu", "persistence", "cluster", "keyspace"};
    private static final String[] TABLE_COLUMNS = {"Key", "Value"};

    private final Map<String, DefaultTableModel> tableModels = new WeakHashMap<>();
    private final RedisConnectContext context;

    public RedisInfoPanel(RedisConnectContext context) {
        this.context = context;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        initTabbedPane();
        refreshInfo();
//        addRefreshButton();
    }

    private void initTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
            @Override
            public void setUI(TabbedPaneUI ui) {
                super.setUI(new BoldTitleTabbedPaneUI());
            }
        };
        configureTabbedPaneProperties(tabbedPane);

        for (String section : INFO_SECTIONS) {
            tabbedPane.addTab(createTabTitle(section), createTabContent(section));
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void configureTabbedPaneProperties(JTabbedPane pane) {
        pane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        pane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        pane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        pane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_CONTENT_SEPARATOR, false);
    }

    private String createTabTitle(String section) {
        return section.substring(0, 1).toUpperCase() + section.substring(1);
    }

    private JScrollPane createTabContent(String section) {
        DefaultTableModel model = new DefaultTableModel(TABLE_COLUMNS, 0);
        JTable table = createTable(model);
        tableModels.put(section, model);
        return new JScrollPane(table);
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column == 1) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        };
        table.setAutoCreateRowSorter(true);
        return table;
    }

    public void refreshInfo() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    for (String section : INFO_SECTIONS) {
                        Map<String, Object> infoData = RedisBasicService.service.getInfo(context, section);
                        if (infoData != null) {
                            updateTableModel(tableModels.get(section), infoData);
                        }
                    }
                } catch (Exception e) {
                    RedisFrontUtils.runEDT(() ->
                            JOptionPane.showMessageDialog(RedisInfoPanel.this, "数据加载失败: " + e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    private void updateTableModel(DefaultTableModel model, Map<String, Object> data) {
        RedisFrontUtils.runEDT(() -> {
            model.setRowCount(0);
            data.forEach((key, value) -> model.addRow(new Object[]{key, value}));
        });
    }

    private void addRefreshButton() {
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshInfo());
        add(refreshButton, BorderLayout.NORTH);
    }
}
