package org.dromara.redisfront.ui.form.fragment;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.commons.util.FutureUtils;
import org.dromara.redisfront.commons.util.LettuceUtils;
import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.component.ChartsPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * DataChartsForm
 *
 * @author Jin
 */
public class DataChartsForm extends ChartsPanel {

    private JPanel contentPanel;
    private JPanel chartsPanel;
    private JLabel cpuUsageValue;
    private JLabel cpuUsageLabel;
    private JLabel processCommandsLabel;
    private JLabel networkLabel;
    private JLabel clientsLabel;
    private JLabel processCommandsValue;
    private JLabel networkValue;
    private JLabel clientsValue;
    private JLabel slowLogLabel;
    private JTextArea slowLogTextArea;
    private JRadioButton autoRefreshBtn;
    private final ConnectInfo connectInfo;
    private Boolean scheduleStarted = false;

    private ScheduledExecutorService scheduledExecutor;

    public static DataChartsForm getInstance(final ConnectInfo connectInfo) {
        return new DataChartsForm(connectInfo);
    }

    public DataChartsForm(final ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        $$$setupUI$$$();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        var flatLineBorder = new FlatLineBorder(new Insets(0, 0, 0, 0), UIManager.getColor("Component.borderColor"));
        setBorder(flatLineBorder);
    }

    private void createUIComponents() {
        contentPanel = this;
        slowLogTextArea = new JTextArea();
        chartsPanel = new JPanel();
        chartsPanel.setLayout(new BorderLayout());
        chartsInit();
    }

    private void chartsInit() {
        JFreeChart jFreeChart = createChart();
        var chartPanel = new ChartPanel(jFreeChart);
        chartPanel.setBackground(Color.BLACK);
        chartPanel.setForeground(Color.BLACK);
        chartsPanel.add(chartPanel, BorderLayout.CENTER);
        final var XYPlot = (XYPlot) jFreeChart.getPlot();

        final var lastTimeSecond = new long[]{DateUtil.currentSeconds()};
        final var lastUsedCpuSys = new double[]{0.0};
        final var timeSeries = new TimeSeries("used memory");
        final var lastSecond = new Second[]{new Second()};

        scheduledExecutor.scheduleAtFixedRate(() -> {
                    if (scheduleStarted) {
                        FutureUtils.supplyAsync(() -> RedisBasicService.service.getStatInfo(connectInfo), statInfo -> {
                                    var instantaneousInputKbps = (String) statInfo.get("instantaneous_input_kbps");
                                    var instantaneousOutputKbps = (String) statInfo.get("instantaneous_output_kbps");
                                    var instantaneousOpsPerSec = (String) statInfo.get("instantaneous_ops_per_sec");
                                    if (Fn.isNotEmpty(instantaneousOpsPerSec)) {
                                        SwingUtilities.invokeLater(() -> processCommandsValue.setText(instantaneousOpsPerSec));
                                    }
                                    if (Fn.isNotEmpty(instantaneousInputKbps) && Fn.isNotEmpty(instantaneousOutputKbps)) {
                                        float in = Float.parseFloat(instantaneousInputKbps) * 1024;
                                        float out = Float.parseFloat(instantaneousOutputKbps) * 1024;
                                        var value = DataSizeUtil.format((long) in) + "/" + DataSizeUtil.format((long) out);
                                        SwingUtilities.invokeLater(() -> networkValue.setText(value));
                                    }
                                }
                        );

                        FutureUtils.supplyAsync(() -> RedisBasicService.service.getClientInfo(connectInfo), clientInfo -> {
                                    var connectedClients = (String) clientInfo.get("connected_clients");
                                    if (Fn.isNotEmpty(connectedClients)) {
                                        SwingUtilities.invokeLater(() -> clientsValue.setText(connectedClients));
                                    }
                                }
                        );

                        if (autoRefreshBtn.isSelected()) {
                            slowLogActionPerformed();
                        }

                        FutureUtils.supplyAsync(() -> RedisBasicService.service.getCpuInfo(connectInfo), cpuInfo -> {
                                    var currentTimeSecond = DateUtil.currentSeconds();
                                    var usedCpuSys = (String) cpuInfo.get("used_cpu_sys");
                                    if (Fn.isNotEmpty(usedCpuSys)) {
                                        var currentUsedCpuSys = Double.parseDouble(usedCpuSys);
                                        if (lastUsedCpuSys[0] != 0L && lastUsedCpuSys[0] != currentUsedCpuSys && lastTimeSecond[0] != currentTimeSecond) {
                                            var percentStr = ((currentUsedCpuSys - lastUsedCpuSys[0]) / (currentTimeSecond - lastTimeSecond[0]));
                                            var df = new DecimalFormat("0.00%");
                                            var percent = df.format(percentStr);
                                            System.out.println(percent);
                                            SwingUtilities.invokeLater(() -> cpuUsageValue.setText(percent));
                                        }
                                        lastTimeSecond[0] = currentTimeSecond;
                                        lastUsedCpuSys[0] = currentUsedCpuSys;
                                    }
                                }
                        );

                        FutureUtils.supplyAsync(() -> RedisBasicService.service.getMemoryInfo(connectInfo), memoryInfo -> {
                            var usedMemory = (String) memoryInfo.get("used_memory");

                            if (Fn.isNotEmpty(usedMemory)) {
                                timeSeries.add(lastSecond[0], Double.parseDouble(usedMemory));
                                lastSecond[0] = (Second) lastSecond[0].next();
                                XYPlot.setDataset(new TimeSeriesCollection(timeSeries));
                            }
                        });
                    }
                }
                , 0, 5, TimeUnit.SECONDS);

    }

    private void slowLogActionPerformed() {
        FutureUtils.runAsync(() -> {
            List<Object> objectList = LettuceUtils.exec(connectInfo, commands -> commands.slowlogGet(128));
            if (Fn.isNotEmpty(objectList)) {
                var slowLogShowTextStrBuilder = new StringBuilder();
                for (Object slowLogObj : objectList) {
                    if (slowLogObj instanceof List<?> slowLogs) {
                        var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        var unixTime = ((Long) slowLogs.get(1)) * 1000;
                        var dateTime = dateFormat.format(unixTime);
                        var processTime = ((Long) slowLogs.get(2));

                        slowLogShowTextStrBuilder.append(dateTime)
                                .append(" - ")
                                .append("command")
                                .append(" [ ");

                        if (slowLogs.get(3) instanceof List<?> commands) {
                            for (Object command : commands) {
                                slowLogShowTextStrBuilder.append(command).append(" ");
                            }
                        }

                        slowLogShowTextStrBuilder.append("] ")
                                .append(" - ")
                                .append(processTime)
                                .append("\n");

                    }
                }
                System.out.println(slowLogShowTextStrBuilder.toString());
                SwingUtilities.invokeLater(() -> {
                    int numLinesToTrunk = slowLogTextArea.getLineCount();
                    int posOfLastLineToTrunk = 0;
                    try {
                        posOfLastLineToTrunk = slowLogTextArea.getLineEndOffset(numLinesToTrunk - 1);
                    } catch (BadLocationException e) {
                        throw new RuntimeException(e);
                    }
//                            slowLogTextArea.append(slowLogShowTextStrBuilder.toString()); 原来使用的是追加方式，预览效果不佳
                    slowLogTextArea.replaceRange(slowLogShowTextStrBuilder.toString(), 0, posOfLastLineToTrunk);
                });

            }
        });

    }

    protected JFreeChart createChart() {
        return ChartFactory.createTimeSeriesChart(
                "Used Memory",
                "",
                "",
                new TimeSeriesCollection(),
                false,
                true,
                false);
    }


    public void scheduleInit() {
        if (!scheduleStarted) {
            scheduleStarted = Boolean.TRUE;
            slowLogActionPerformed();
        } else {
            scheduleStarted = Boolean.FALSE;
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
        createUIComponents();
        contentPanel.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(panel1, BorderLayout.CENTER);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        cpuUsageLabel = new JLabel();
        Font cpuUsageLabelFont = this.$$$getFont$$$(null, -1, 18, cpuUsageLabel.getFont());
        if (cpuUsageLabelFont != null) cpuUsageLabel.setFont(cpuUsageLabelFont);
        cpuUsageLabel.setText("Cpu Usage");
        panel2.add(cpuUsageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processCommandsLabel = new JLabel();
        Font processCommandsLabelFont = this.$$$getFont$$$(null, -1, 18, processCommandsLabel.getFont());
        if (processCommandsLabelFont != null) processCommandsLabel.setFont(processCommandsLabelFont);
        processCommandsLabel.setText("Process Commands");
        panel2.add(processCommandsLabel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        networkLabel = new JLabel();
        Font networkLabelFont = this.$$$getFont$$$(null, -1, 18, networkLabel.getFont());
        if (networkLabelFont != null) networkLabel.setFont(networkLabelFont);
        networkLabel.setText("Network");
        panel2.add(networkLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clientsLabel = new JLabel();
        Font clientsLabelFont = this.$$$getFont$$$(null, -1, 18, clientsLabel.getFont());
        if (clientsLabelFont != null) clientsLabel.setFont(clientsLabelFont);
        clientsLabel.setText("Clients");
        panel2.add(clientsLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        processCommandsValue = new JLabel();
        Font processCommandsValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, processCommandsValue.getFont());
        if (processCommandsValueFont != null) processCommandsValue.setFont(processCommandsValueFont);
        processCommandsValue.setHorizontalAlignment(0);
        processCommandsValue.setHorizontalTextPosition(0);
        processCommandsValue.setText("0");
        panel2.add(processCommandsValue, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clientsValue = new JLabel();
        Font clientsValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, clientsValue.getFont());
        if (clientsValueFont != null) clientsValue.setFont(clientsValueFont);
        clientsValue.setHorizontalAlignment(0);
        clientsValue.setHorizontalTextPosition(0);
        clientsValue.setText("0");
        panel2.add(clientsValue, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        networkValue = new JLabel();
        Font networkValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, networkValue.getFont());
        if (networkValueFont != null) networkValue.setFont(networkValueFont);
        networkValue.setHorizontalAlignment(0);
        networkValue.setHorizontalTextPosition(0);
        networkValue.setText("0KB/0KB");
        panel2.add(networkValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cpuUsageValue = new JLabel();
        Font cpuUsageValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, cpuUsageValue.getFont());
        if (cpuUsageValueFont != null) cpuUsageValue.setFont(cpuUsageValueFont);
        cpuUsageValue.setHorizontalAlignment(0);
        cpuUsageValue.setHorizontalTextPosition(0);
        cpuUsageValue.setText("0.0%");
        panel2.add(cpuUsageValue, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        separator1.setOrientation(0);
        panel3.add(separator1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel5.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        slowLogTextArea.setColumns(50);
        slowLogTextArea.setEditable(false);
        scrollPane1.setViewportView(slowLogTextArea);
        slowLogLabel = new JLabel();
        Font slowLogLabelFont = this.$$$getFont$$$(null, Font.BOLD, 18, slowLogLabel.getFont());
        if (slowLogLabelFont != null) slowLogLabel.setFont(slowLogLabelFont);
        slowLogLabel.setText("slowLog");
        panel4.add(slowLogLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        autoRefreshBtn = new JRadioButton();
        autoRefreshBtn.setText("自动刷新");
        panel4.add(autoRefreshBtn, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel3.add(separator2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel3.add(chartsPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

    @Override
    protected ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }
}
