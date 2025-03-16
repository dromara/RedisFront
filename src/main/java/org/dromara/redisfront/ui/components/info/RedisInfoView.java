package org.dromara.redisfront.ui.components.info;

import com.formdev.flatlaf.FlatClientProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.LogInfo;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class RedisInfoView extends JPanel implements Runnable {
    private static final String[] INFO_SECTIONS = {"server", "memory", "clients", "stats", "cpu", "persistence", "cluster", "keyspace"};
    private static final String[] TABLE_COLUMNS = {"Key", "Value"};

    private final Map<String, DefaultTableModel> tableModels = new WeakHashMap<>();

    private final LinkedBlockingQueue<LogInfo> queue = new LinkedBlockingQueue<>();

    private final RedisConnectContext context;
    private final RedisFrontWidget owner;

    private final JTextArea textArea = new JTextArea();
    private final Boolean running = true;

    public RedisInfoView(RedisFrontWidget owner, RedisConnectContext context) {
        this.context = context;
        this.owner = owner;
        initUI();
        new Thread(this).start();
    }

    public void appendLog(LogInfo logInfo) {
        queue.add(logInfo);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        this.textArea.setEditable(false);
        this.textArea.setLineWrap(true);
        initTabbedPane();
        refreshInfo();
    }

    private String format(LogInfo logInfo) {
        return logInfo.date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .concat(" - ")
                .concat("[" + logInfo.ip() + "]")
                .concat(" : ")
                .concat(logInfo.info()).concat("\n");
    }

    private void initTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
            @Override
            public void setUI(TabbedPaneUI ui) {
                super.setUI(new BoldTitleTabbedPaneUI());
            }
        };
        this.configureTabbedPaneProperties(tabbedPane);

        for (String section : INFO_SECTIONS) {
            tabbedPane.addTab(createTabTitle(section), createTabContent(section));
        }

        tabbedPane.addTab("logs", new JScrollPane(textArea));

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
        SyncLoadingDialog
                .builder(owner, "load info data")
                .showSyncLoadingDialog(() ->
                        Arrays.stream(INFO_SECTIONS)
                                .map(section -> {
                                    try {
                                        LogStatusHolder.ignoredLog();
                                        Map<String, Object> infoData = RedisBasicService.service.getInfo(context, section);
                                        return new LogInfoData(section, infoData);
                                    } finally {
                                        LogStatusHolder.clear();
                                    }
                                }).collect(Collectors.toSet()), (infoData, e) -> {
                    if (e != null) {
                        owner.displayException(e);
                    } else {
                        for (LogInfoData logInfoData : infoData) {
                            updateTableModel(tableModels.get(logInfoData.getKey()), logInfoData.getInfoData());
                        }
                    }
                });
    }

    private void updateTableModel(DefaultTableModel model, Map<String, Object> data) {
        model.setRowCount(0);
        data.forEach((key, value) -> model.addRow(new Object[]{key, value}));
    }

    private void addRefreshButton() {
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(_ -> refreshInfo());
        add(refreshButton, BorderLayout.NORTH);
    }

    @Override
    public void run() {
        while (running) {
            try {
                LogInfo logInfo = queue.take();
                String format = format(logInfo);
                RedisFrontUtils.runEDT(() -> textArea.append(format));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
