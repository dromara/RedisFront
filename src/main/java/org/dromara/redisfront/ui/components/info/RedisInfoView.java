package org.dromara.redisfront.ui.components.info;

import com.formdev.flatlaf.FlatClientProperties;
import io.lettuce.core.cluster.models.partitions.Partitions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.LogInfo;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.turbo.Turbo2;
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
import java.util.Set;
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

    private final RedisConnectContext redisConnectContext;
    private final RedisFrontContext redisFrontContext;
    private final RedisFrontWidget owner;

    private final JTextArea textArea1 = new JTextArea();
    private final JTextArea textArea2 = new JTextArea();
    private JTabbedPane tabbedPane;
    private Boolean running = true;

    private final static String SSH_MAPPING = "[Local] %s:%s ==> [Remote] %s:%s %s \n";
    private final static String NORMAL_MAPPING = "[Remote] %s:%s %s \n";
    private final static String FORMAT_MESSAGE = """
            
            RedisHost: %s
            RedisPort: %s
            RedisMode: %s
            RedisConnectType: %s
            RedisVersion：%s \n
            
            %s
            
            """;

    public RedisInfoView(RedisFrontWidget owner, RedisConnectContext redisConnectContext) {
        this.redisConnectContext = redisConnectContext;
        this.owner = owner;
        this.redisFrontContext = (RedisFrontContext) owner.getContext();
        initUI();
        new Thread(this).start();
    }

    public void appendLog(LogInfo logInfo) {
        queue.add(logInfo);
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        this.textArea1.setEditable(false);
        this.textArea2.setEditable(false);
        this.textArea1.setLineWrap(true);
        this.textArea2.setLineWrap(true);
        this.initTabbedPane();
        this.refreshInfo();
    }

    private String format(LogInfo logInfo) {
        return logInfo.date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .concat(" - ")
                .concat("[" + logInfo.ip() + "]")
                .concat(" : ")
                .concat(logInfo.info()).concat("\n");
    }

    private void initTabbedPane() {
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
            @Override
            public void setUI(TabbedPaneUI ui) {
                super.setUI(new BoldTitleTabbedPaneUI());
            }
        };
        this.configureTabbedPaneProperties(tabbedPane);
        this.tabbedPane.addTab("Logs".toUpperCase(), new JScrollPane(textArea1));
        this.tabbedPane.addTab("SSH".toUpperCase(), new JScrollPane(textArea2));
        for (String section : INFO_SECTIONS) {
            this.tabbedPane.addTab(createTabTitle(section).toUpperCase(), createTabContent(section));
        }
        this.add(this.tabbedPane, BorderLayout.CENTER);

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
                .showSyncLoadingDialog(() -> {
                            Turbo2<String, Set<LogInfoData>> turbo2 = new Turbo2<>();
                            String sshInfoFormated = "";
                            Map<String, Object> serverInfo = RedisBasicService.service.getServerInfo(redisConnectContext);
                            if (redisConnectContext.getConnectTypeMode().equals(ConnectType.SSH)) {
                                String localHost = "127.0.0.1";
                                if (redisConnectContext.getRedisMode().equals(RedisMode.CLUSTER)) {
                                    Partitions clusterPartitions = LettuceUtils.getRedisClusterPartitions(redisConnectContext);
                                    sshInfoFormated = clusterPartitions.stream().map(clusterNode -> {
                                        var uri = clusterNode.getUri();
                                        Map<Integer, Integer> clusterLocalPort = redisConnectContext.getClusterLocalPort();
                                        return String.format(SSH_MAPPING, localHost,
                                                clusterLocalPort.get(uri.getPort()).toString(),
                                                uri.getHost(),
                                                uri.getPort(),
                                                clusterNode.getFlags()
                                        );
                                    }).collect(Collectors.joining(""));
                                } else {
                                    sshInfoFormated = String.format(SSH_MAPPING, localHost,
                                            redisConnectContext.getLocalPort(),
                                            redisConnectContext.getHost(),
                                            redisConnectContext.getPort()
                                            , ""
                                    );
                                }
                            } else {
                                if (redisConnectContext.getRedisMode().equals(RedisMode.CLUSTER)) {
                                    Partitions clusterPartitions = LettuceUtils.getRedisClusterPartitions(redisConnectContext);
                                    sshInfoFormated = clusterPartitions.stream().map(clusterNode -> {
                                        var uri = clusterNode.getUri();
                                        return String.format(NORMAL_MAPPING,
                                                uri.getHost(),
                                                uri.getPort(),
                                                clusterNode.getFlags()
                                        );
                                    }).collect(Collectors.joining(""));
                                }
                            }

                            turbo2.setT1(String.format(FORMAT_MESSAGE,
                                    redisConnectContext.getHost(),
                                    redisConnectContext.getPort(),
                                    redisConnectContext.getRedisMode(),
                                    redisConnectContext.getConnectTypeMode(),
                                    serverInfo.get("redis_version").toString(),
                                    sshInfoFormated
                            ));

                            Set<LogInfoData> loginInfoData = Arrays.stream(INFO_SECTIONS)
                                    .map(section -> {
                                        try {
                                            LogStatusHolder.ignoredLog();
                                            Map<String, Object> infoData = RedisBasicService.service.getInfo(redisConnectContext, section);
                                            return new LogInfoData(section, infoData);
                                        } finally {
                                            LogStatusHolder.clear();
                                        }
                                    }).collect(Collectors.toSet());
                            turbo2.setT2(loginInfoData);
                            return turbo2;
                        }
                        , (result, e) -> {
                            if (e != null) {
                                owner.displayException(e);
                            } else {
                                for (LogInfoData logInfoData : result.getT2()) {
                                    updateTableModel(tableModels.get(logInfoData.getKey()), logInfoData.getInfoData());
                                }
                                textArea2.setText(result.getT1());
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
                RedisFrontUtils.runEDT(() -> textArea1.append(format));
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
