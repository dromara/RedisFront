package org.dromara.redisfront.ui.widget.main.fragment.scaffold.report;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.chart.*;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.dromara.redisfront.ui.components.monitor.RedisTopologyMonitor;
import org.dromara.redisfront.ui.components.monitor.RedisTopologyPanel;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.formdev.flatlaf.FlatClientProperties.STYLE;

public class ReportPageView extends QSPageItem<RedisFrontWidget> {
    private JPanel rootPanel;
    private FlatToggleButton networkTgBtn, memoryTgBtn, connectTgBtn, commandTgBtn, clusterTgBtn;
    private JPanel chartPanel;
    private final RedisFrontWidget owner;
    private final RedisConnectContext context;
    private final RedisNetworkChart redisNetworkChart;
    private final RedisMemoryChart redisMemoryChart;
    private final RedisClientConnectionsChart redisClientConnectionsChart;
    private final RedisCommandStatsChart redisCommandStatsChart;
    private final List<AbstractRedisChart> charts;
    private final RedisTopologyPanel topologyPanel;
    private JComboBox<String> nodeCombo;
    private JComboBox<Integer> historyCombo;
    private JButton refreshButton;
    private JToggleButton pauseButton;
    private JLabel scopeLabel, statusLabel, nodeLabel;
    private Map<String, RedisTopologyMonitor.Node> nodes = Map.of();
    private boolean updatingNodes;
    private boolean active;
    private PollingSession polling;
    private final Timer timer = new Timer(2000, e -> requestSample(false));
    private String displayedAddress;

    private static final class PollingSession {
        final RedisTopologyMonitor monitor;
        final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
            var t = new Thread(r, "RedisReportMonitor");
            t.setDaemon(true);
            return t;
        });
        final AtomicBoolean busy = new AtomicBoolean();
        RedisMetricSampler sampler = new RedisMetricSampler();
        String address;

        PollingSession(RedisConnectContext context) {
            monitor = new RedisTopologyMonitor(context);
        }
    }

    public ReportPageView(RedisConnectContext context, RedisFrontWidget owner) {
        this.context = context;
        this.owner = owner;
        redisNetworkChart = new RedisNetworkChart(context, owner);
        redisMemoryChart = new RedisMemoryChart(context, owner);
        redisClientConnectionsChart = new RedisClientConnectionsChart(context, owner);
        redisCommandStatsChart = new RedisCommandStatsChart(context, owner);
        charts = List.of(redisNetworkChart, redisMemoryChart, redisClientConnectionsChart, redisCommandStatsChart);
        topologyPanel = new RedisTopologyPanel(owner::$tr, address -> nodeCombo.setSelectedItem(address));
        $$$setupUI$$$();
        initializeUI();
        setupUI();
    }

    private String tr(String key) {
        return owner.$tr("ReportPageView." + key);
    }

    private void initializeUI() {
        chartPanel.setLayout(new CardLayout());
        for (int i = 0; i < charts.size(); i++) chartPanel.add(charts.get(i), String.valueOf(i));
        chartPanel.add(topologyPanel, "4");
        var buttons = List.of(networkTgBtn, memoryTgBtn, connectTgBtn, commandTgBtn, clusterTgBtn);
        var group = new ButtonGroup();
        for (int i = 0; i < buttons.size(); i++) {
            var button = buttons.get(i);
            int index = i;
            button.setButtonType(FlatButton.ButtonType.tab);
            button.setFocusable(false);
            button.setTabUnderlineHeight(2);
            button.putClientProperty(STYLE, "tab.underlineHeight:1;[dark]tab.selectedForeground:$ToggleButton.tab.underlineColor;[light]tab.selectedForeground:$RedisFront.main.background;[light]tab.underlineColor:$RedisFront.main.background;");
            group.add(button);
            button.addActionListener(e -> ((CardLayout) chartPanel.getLayout()).show(chartPanel, String.valueOf(index)));
        }
        clusterTgBtn.setVisible(true);
        networkTgBtn.setSelected(true);
        nodeLabel = new JLabel();
        nodeCombo = new JComboBox<>();
        nodeCombo.addItem("");
        nodeCombo.setPrototypeDisplayValue("255.255.255.255:65535 · replica");
        nodeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                String address = Objects.toString(value, "");
                var node = nodes.get(address);
                setText(address.isEmpty() ? tr("followConnection") : address + (node == null ? "" : " · " + topologyPanel.role(node.role())));
                return this;
            }
        });
        nodeCombo.addActionListener(e -> {
            if (updatingNodes) return;
            charts.forEach(AbstractRedisChart::resetData);
            displayedAddress = null;
            requestSample(true);
        });
        refreshButton = new JButton();
        refreshButton.addActionListener(e -> requestSample(true));
        pauseButton = new JToggleButton();
        pauseButton.addActionListener(e -> {
            relocalize();
            if (pauseButton.isSelected()) timer.stop();
            else {
                if (active) timer.start();
                requestSample(true);
            }
        });
        historyCombo = new JComboBox<>(new Integer[]{1, 5, 15});
        historyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setText(String.format(tr("history"), value));
                return this;
            }
        });
        historyCombo.addActionListener(e -> charts.forEach(chart -> chart.setHistoryMinutes((Integer) historyCombo.getSelectedItem())));
        var left = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        left.add(nodeLabel);
        left.add(nodeCombo);
        left.add(refreshButton);
        var right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 0));
        right.add(historyCombo);
        right.add(pauseButton);
        var controls = new JPanel(new BorderLayout());
        controls.add(left, BorderLayout.WEST);
        controls.add(right, BorderLayout.EAST);
        scopeLabel = new JLabel();
        statusLabel = new JLabel();
        var details = new JPanel(new BorderLayout(8, 0));
        details.add(scopeLabel, BorderLayout.CENTER);
        details.add(statusLabel, BorderLayout.EAST);
        var header = new JPanel(new BorderLayout(0, 8));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.add(controls, BorderLayout.NORTH);
        header.add(details, BorderLayout.SOUTH);
        rootPanel.add(header, BorderLayout.NORTH);
        relocalize();
        scopeLabel.setText(tr("scope"));
        statusLabel.setText(tr("waiting"));
    }

    private void relocalize() {
        networkTgBtn.setText(tr("networkBtn.text"));
        memoryTgBtn.setText(tr("memoryBtn.text"));
        connectTgBtn.setText(tr("clientsBtn.text"));
        commandTgBtn.setText(tr("commandBtn.text"));
        clusterTgBtn.setText(tr("topology"));
        nodeLabel.setText(tr("node"));
        refreshButton.setText(tr("refresh"));
        pauseButton.setText(tr(pauseButton.isSelected() ? "resume" : "pause"));
        nodeCombo.repaint();
        historyCombo.repaint();
    }

    @Override
    public void onChange() {
        if (active) return;
        active = true;
        polling = new PollingSession(context);
        if (!pauseButton.isSelected()) timer.start();
        requestSample(true);
    }

    @Override
    public void onClose() {
        active = false;
        timer.stop();
        var old = polling;
        polling = null;
        if (old != null) {
            old.worker.execute(old.monitor::close);
            old.worker.shutdown();
        }
    }

    private void requestSample(boolean manual) {
        var session = polling;
        if (!active || session == null || !session.busy.compareAndSet(false, true)) return;
        String requested = Objects.toString(nodeCombo.getSelectedItem(), "");
        refreshButton.setEnabled(false);
        session.worker.execute(() -> {
            RedisTopologyMonitor.Snapshot snapshot = null;
            RedisMetricSampler.Sample sample = null;
            String failure = "";
            try {
                snapshot = session.monitor.collect(requested);
                if (!snapshot.info().isEmpty()) {
                    if (!snapshot.selected().address().equals(session.address)) {
                        session.address = snapshot.selected().address();
                        session.sampler = new RedisMetricSampler();
                    }
                    sample = session.sampler.sample(snapshot.info(), System.nanoTime(), snapshot.sampledAt());
                }
            } catch (Exception e) {
                failure = ExceptionMessageUtils.bestMessage(e);
            }
            var result = snapshot;
            var metrics = sample;
            var error = failure;
            SwingUtilities.invokeLater(() -> {
                session.busy.set(false);
                if (polling != session || !active) return;
                refreshButton.setEnabled(true);
                if (!requested.equals(Objects.toString(nodeCombo.getSelectedItem(), ""))) {
                    requestSample(true);
                    return;
                }
                if (pauseButton.isSelected() && !manual) return;
                if (result == null) {
                    statusLabel.setText(tr("error"));
                    statusLabel.setToolTipText(error);
                    return;
                }
                updateNodes(result);
                topologyPanel.setSnapshot(result);
                scopeLabel.setText(result.selected().address() + " · " + topologyPanel.role(result.selected().role()) + " · " + tr("scope"));
                if (metrics != null) {
                    if (!result.selected().address().equals(displayedAddress)) {
                        charts.forEach(AbstractRedisChart::resetData);
                        displayedAddress = result.selected().address();
                    }
                    for (var chart : charts) chart.acceptSample(metrics);
                    statusLabel.setText(tr("updated") + " " + DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(result.sampledAt())));
                    statusLabel.setToolTipText(String.format(tr("sampleSummary"), metrics.cpuPercent(), metrics.qps()));
                } else {
                    statusLabel.setText(tr("error"));
                    statusLabel.setToolTipText(result.error());
                }
            });
        });
    }

    private void updateNodes(RedisTopologyMonitor.Snapshot snapshot) {
        var map = new LinkedHashMap<String, RedisTopologyMonitor.Node>();
        snapshot.nodes().forEach(node -> map.put(node.address(), node));
        String selected = Objects.toString(nodeCombo.getSelectedItem(), "");
        nodes = map;
        var addresses = new ArrayList<String>();
        addresses.add("");
        addresses.addAll(map.keySet());
        updatingNodes = true;
        try {
            var current = new ArrayList<String>();
            for (int i = 0; i < nodeCombo.getItemCount(); i++) current.add(nodeCombo.getItemAt(i));
            if (!current.equals(addresses)) {
                nodeCombo.setModel(new DefaultComboBoxModel<>(addresses.toArray(String[]::new)));
                nodeCombo.setSelectedItem(map.containsKey(selected) ? selected : "");
            }
            nodeCombo.repaint();
        } finally {
            updatingNodes = false;
        }
    }

    @Override
    public void updateUI() {
        if (charts != null) charts.forEach(AbstractRedisChart::refreshUI);
        if (nodeCombo != null) {
            relocalize();
            topologyPanel.relocalize();
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        rootPanel.add(panel1, BorderLayout.SOUTH);
        networkTgBtn = new FlatToggleButton();
        this.$$$loadButtonText$$$(networkTgBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "ReportPageView.networkBtn.text"));
        panel1.add(networkTgBtn);
        memoryTgBtn = new FlatToggleButton();
        this.$$$loadButtonText$$$(memoryTgBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "ReportPageView.memoryBtn.text"));
        panel1.add(memoryTgBtn);
        connectTgBtn = new FlatToggleButton();
        this.$$$loadButtonText$$$(connectTgBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "ReportPageView.clientsBtn.text"));
        connectTgBtn.setVisible(true);
        panel1.add(connectTgBtn);
        commandTgBtn = new FlatToggleButton();
        this.$$$loadButtonText$$$(commandTgBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "ReportPageView.commandBtn.text"));
        commandTgBtn.setVisible(true);
        panel1.add(commandTgBtn);
        clusterTgBtn = new FlatToggleButton();
        clusterTgBtn.setText(" 集群参数");
        clusterTgBtn.setVisible(false);
        panel1.add(clusterTgBtn);
        chartPanel = new JPanel();
        chartPanel.setLayout(new BorderLayout(0, 0));
        rootPanel.add(chartPanel, BorderLayout.CENTER);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    /**
     * @noinspection ALL
     */
    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }


    @Override
    public RedisFrontWidget getApp() {
        return owner;
    }

    @Override
    protected JComponent getContentPanel() {
        return rootPanel;
    }

}
