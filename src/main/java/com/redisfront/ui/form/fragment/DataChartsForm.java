package com.redisfront.ui.form.fragment;

import cn.hutool.core.date.DateUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.component.ChartsPanel;
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
import javax.swing.text.StyleContext;
import java.awt.*;
import java.text.DecimalFormat;
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
    private final ConnectInfo connectInfo;

    private JFreeChart jFreeChart;


    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public void shutDownScheduledExecutorService() {
        scheduledExecutor.shutdown();
    }

    public static DataChartsForm getInstance(final ConnectInfo connectInfo) {
        return new DataChartsForm(connectInfo);
    }

    public JPanel contentPanel() {
        return contentPanel;
    }

    public DataChartsForm(final ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        $$$setupUI$$$();
        createUIComponents();
    }

    private void createUIComponents() {
        contentPanel = this;

        slowLogLabel = new JLabel();
        slowLogTextArea = new JTextArea();

        cpuUsageLabel = new JLabel();
        cpuUsageValue = new JLabel();

        networkLabel = new JLabel();
        networkValue = new JLabel();

        clientsLabel = new JLabel();
        clientsValue = new JLabel();

        processCommandsLabel = new JLabel();
        processCommandsValue = new JLabel();

        chartsPanel = new JPanel();
        chartsPanel.setLayout(new BorderLayout());

        chartsInit();
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

    private void chartsInit() {
        jFreeChart = createChart();
        final var chartPanel = new ChartPanel(jFreeChart);
        chartsPanel.add(chartPanel, BorderLayout.CENTER);
    }

    public void scheduleInit() {
        final var XYPlot = (XYPlot) jFreeChart.getPlot();

        final var lastTimeSecond = new long[]{DateUtil.currentSeconds()};
        final var lastUsedCpuSys = new double[]{0.0};
        final var timeSeries = new TimeSeries("cpu usage");
        final var lastSecond = new Second[]{new Second()};

        scheduledExecutor.scheduleAtFixedRate(() -> {
                    try {
                        var currentTimeSecond = DateUtil.currentSeconds();
                        var cpuInfo = RedisBasicService.service.getCpuInfo(connectInfo);

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

                        var memoryInfo = RedisBasicService.service.getMemoryInfo(connectInfo);
                        var usedMemory = (String) memoryInfo.get("used_memory");

                        if (Fn.isNotEmpty(usedMemory)) {
                            timeSeries.add(lastSecond[0], Double.parseDouble(usedMemory));
                            lastSecond[0] = (Second) lastSecond[0].next();
                            XYPlot.setDataset(new TimeSeriesCollection(timeSeries));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                , 0, 5, TimeUnit.SECONDS);
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
        Font cpuUsageLabelFont = this.$$$getFont$$$(null, -1, 18, cpuUsageLabel.getFont());
        if (cpuUsageLabelFont != null) cpuUsageLabel.setFont(cpuUsageLabelFont);
        cpuUsageLabel.setText("Cpu Usage");
        panel2.add(cpuUsageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font processCommandsLabelFont = this.$$$getFont$$$(null, -1, 18, processCommandsLabel.getFont());
        if (processCommandsLabelFont != null) processCommandsLabel.setFont(processCommandsLabelFont);
        processCommandsLabel.setText("Process Commands");
        panel2.add(processCommandsLabel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font networkLabelFont = this.$$$getFont$$$(null, -1, 18, networkLabel.getFont());
        if (networkLabelFont != null) networkLabel.setFont(networkLabelFont);
        networkLabel.setText("Network");
        panel2.add(networkLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font clientsLabelFont = this.$$$getFont$$$(null, -1, 18, clientsLabel.getFont());
        if (clientsLabelFont != null) clientsLabel.setFont(clientsLabelFont);
        clientsLabel.setText("Clients");
        panel2.add(clientsLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font processCommandsValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, processCommandsValue.getFont());
        if (processCommandsValueFont != null) processCommandsValue.setFont(processCommandsValueFont);
        processCommandsValue.setText("");
        panel2.add(processCommandsValue, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font clientsValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, clientsValue.getFont());
        if (clientsValueFont != null) clientsValue.setFont(clientsValueFont);
        clientsValue.setText("");
        panel2.add(clientsValue, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font networkValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, networkValue.getFont());
        if (networkValueFont != null) networkValue.setFont(networkValueFont);
        networkValue.setText("");
        panel2.add(networkValue, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        Font cpuUsageValueFont = this.$$$getFont$$$("Arial Black", Font.BOLD, 20, cpuUsageValue.getFont());
        if (cpuUsageValueFont != null) cpuUsageValue.setFont(cpuUsageValueFont);
        cpuUsageValue.setText("");
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
        final Spacer spacer1 = new Spacer();
        panel4.add(spacer1, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        Font slowLogLabelFont = this.$$$getFont$$$(null, Font.BOLD, 18, slowLogLabel.getFont());
        if (slowLogLabelFont != null) slowLogLabel.setFont(slowLogLabelFont);
        slowLogLabel.setText("slowLog");
        panel4.add(slowLogLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

}
