package org.dromara.redisfront.ui.components.info;

import com.formdev.flatlaf.FlatClientProperties;
import cn.hutool.core.collection.CollUtil;
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
import java.util.Objects;
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

    private final LinkedBlockingQueue<LogInfo> queue = new LinkedBlockingQueue<>(1000);

    private final RedisConnectContext redisConnectContext;
    private final RedisFrontContext redisFrontContext;
    private final RedisFrontWidget owner;

    private final JTextArea textArea1 = new JTextArea();
    private final JTextArea textArea2 = new JTextArea();
    private JTabbedPane tabbedPane;
    private volatile boolean running = true;
    private final Thread worker;

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
        worker = new Thread(this, "RedisInfoView-" + redisConnectContext.getId());
        worker.setDaemon(true);
        worker.start();
    }

    public void appendLog(LogInfo logInfo) {
        if (!running) return;
        if (!queue.offer(logInfo)) { queue.poll();queue.offer(logInfo); }
    }

    public void stop() {
        running = false;
        worker.interrupt();
        queue.clear();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        this.textArea1.setEditable(false);
        this.textArea2.setEditable(false);
        this.textArea1.setLineWrap(true);
        this.textArea2.setLineWrap(true);
        this.initTabbedPane();
        // 这里不要预加载 refreshInfo()：面板只有在用户点 More Info 时才会挂到对话框上，
        // 而点击处理器会再调一次 refreshInfo()，预加载的结果从未展示过，
        // 却让每次打开连接都要白跑一轮 INFO 采集（集群 + SSH 还要做完整拓扑发现）并弹加载框。
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
                .builder(owner, owner.$tr("RedisInfoView.loading.message"))
                .showSyncLoadingDialog(() -> {
                            Turbo2<String, Set<LogInfoData>> turbo2 = new Turbo2<>();
                            String sshInfoFormated = "";
                            Map<String, Object> serverInfo = RedisBasicService.service.getServerInfo(redisConnectContext);
                            if (RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
                                String localHost = "127.0.0.1";
                                if (redisConnectContext.getRedisMode().equals(RedisMode.CLUSTER)) {
                                    Partitions clusterPartitions = LettuceUtils.getRedisClusterPartitions(redisConnectContext);
                                    sshInfoFormated = clusterPartitions.stream().map(clusterNode -> {
                                        var uri = clusterNode.getUri();
                                        Map<String, Integer> clusterLocalPort = redisConnectContext.getClusterLocalPort();
                                        // 映射 key 是 host:port；拓扑可能在建映射之后变化，取不到就显示占位符
                                        Integer localPort = CollUtil.isEmpty(clusterLocalPort) ? null
                                                : clusterLocalPort.get(RedisConnectContext.clusterAddress(uri.getHost(), uri.getPort()));
                                        return String.format(SSH_MAPPING, localHost,
                                                localPort == null ? "-" : localPort,
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
                                    Objects.toString(serverInfo.get("redis_version"), ""),
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

    @Override
    public void run() {
        while (running) {
            try {
                LogInfo logInfo = queue.take();
                var batch = new java.util.ArrayList<LogInfo>();batch.add(logInfo);queue.drainTo(batch, 199);
                String format = batch.stream().map(this::format).collect(Collectors.joining());
                SwingUtilities.invokeAndWait(() -> {
                    if (!running) return;
                    textArea1.append(format);
                    if (textArea1.getDocument().getLength() > 500_000) {
                        try { textArea1.getDocument().remove(0, textArea1.getDocument().getLength() - 400_000); }
                        catch (javax.swing.text.BadLocationException ignored) { }
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("RedisInfoView worker error", e);
            }
        }
    }
}
