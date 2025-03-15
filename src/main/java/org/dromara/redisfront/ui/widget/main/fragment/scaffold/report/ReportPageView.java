package org.dromara.redisfront.ui.widget.main.fragment.scaffold.report;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.chart.RedisClientConnectionsChart;
import org.dromara.redisfront.ui.components.chart.RedisCommandStatsChart;
import org.dromara.redisfront.ui.components.chart.RedisMemoryChart;
import org.dromara.redisfront.ui.components.chart.RedisNetworkChart;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import static com.formdev.flatlaf.FlatClientProperties.STYLE;

/**
 * DataChartsForm
 *
 * @author Jin
 */
public class ReportPageView extends QSPageItem<RedisFrontWidget> {

    private JPanel rootPanel;
    private FlatToggleButton networkTgBtn;
    private FlatToggleButton memoryTgBtn;
    private FlatToggleButton connectTgBtn;
    private FlatToggleButton commandTgBtn;
    private FlatToggleButton clusterTgBtn;
    private JPanel chartPanel;
    private final RedisFrontWidget owner;
    private final RedisNetworkChart redisNetworkChart;
    private final RedisMemoryChart redisMemoryChart;
    private final RedisClientConnectionsChart redisClientConnectionsChart;
    private final RedisCommandStatsChart redisCommandStatsChart;


    public ReportPageView(final RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        this.owner = owner;
        this.redisNetworkChart = new RedisNetworkChart(redisConnectContext, owner);
        this.redisMemoryChart = new RedisMemoryChart(redisConnectContext, owner);
        this.redisClientConnectionsChart = new RedisClientConnectionsChart(redisConnectContext, owner);
        this.redisCommandStatsChart = new RedisCommandStatsChart(redisConnectContext, owner);
        $$$setupUI$$$();
        this.chartPanel.add(redisNetworkChart, BorderLayout.CENTER);
        this.initializeUI();
        this.setupUI();
    }

    private void configureToggleButton(FlatToggleButton button) {
        button.setButtonType(FlatButton.ButtonType.tab);
        button.setFocusable(false);
        button.setTabUnderlineHeight(2);
        button.putClientProperty(STYLE,
                "tab.underlineHeight:1;" +
                "[dark]tab.selectedForeground:$ToggleButton.tab.underlineColor;"
                + "[light]tab.selectedForeground:$RedisFront.main.background;"
                + "[light]tab.underlineColor:$RedisFront.main.background;"
        );
    }

    private void initializeUI() {
        this.networkTgBtn.setSelected(true);

        this.configureToggleButton(networkTgBtn);
        this.networkTgBtn.addActionListener(_ -> {
            if (networkTgBtn.isSelected()) {
                this.chartPanel.removeAll();
                this.chartPanel.add(redisNetworkChart, BorderLayout.CENTER);
                this.chartPanel.updateUI();
                this.memoryTgBtn.setSelected(false);
                this.connectTgBtn.setSelected(false);
                this.commandTgBtn.setSelected(false);
                this.clusterTgBtn.setSelected(false);
            } else {
                this.networkTgBtn.setSelected(true);
            }
        });

        this.configureToggleButton(memoryTgBtn);
        this.memoryTgBtn.addActionListener(_ -> {
            if (memoryTgBtn.isSelected()) {
                this.chartPanel.removeAll();
                this.chartPanel.add(redisMemoryChart, BorderLayout.CENTER);
                this.chartPanel.updateUI();
                this.networkTgBtn.setSelected(false);
                this.connectTgBtn.setSelected(false);
                this.commandTgBtn.setSelected(false);
                this.clusterTgBtn.setSelected(false);
            } else {
                this.memoryTgBtn.setSelected(true);
            }
        });

        this.configureToggleButton(connectTgBtn);
        this.connectTgBtn.addActionListener(_ -> {
            if (connectTgBtn.isSelected()) {
                this.chartPanel.removeAll();
                this.chartPanel.add(redisClientConnectionsChart, BorderLayout.CENTER);
                this.chartPanel.updateUI();
                this.networkTgBtn.setSelected(false);
                this.memoryTgBtn.setSelected(false);
                this.commandTgBtn.setSelected(false);
                this.clusterTgBtn.setSelected(false);
            } else {
                this.connectTgBtn.setSelected(true);
            }
        });

        this.configureToggleButton(commandTgBtn);
        this.commandTgBtn.addActionListener(_ -> {
            if (commandTgBtn.isSelected()) {
                this.chartPanel.removeAll();
                this.chartPanel.add(redisCommandStatsChart, BorderLayout.CENTER);
                this.chartPanel.updateUI();
                this.networkTgBtn.setSelected(false);
                this.memoryTgBtn.setSelected(false);
                this.connectTgBtn.setSelected(false);
                this.clusterTgBtn.setSelected(false);
            } else {
                this.commandTgBtn.setSelected(true);
            }
        });

        this.configureToggleButton(clusterTgBtn);
        this.clusterTgBtn.addActionListener(_ -> {
            if (clusterTgBtn.isSelected()) {
                this.chartPanel.removeAll();
                this.chartPanel.add(redisMemoryChart, BorderLayout.CENTER);
                this.chartPanel.updateUI();
                this.networkTgBtn.setSelected(false);
                this.memoryTgBtn.setSelected(false);
                this.connectTgBtn.setSelected(false);
                this.commandTgBtn.setSelected(false);
            } else {
                this.clusterTgBtn.setSelected(true);
            }
        });
    }

    @Override
    public void updateUI() {
        if (redisNetworkChart != null) {
            redisNetworkChart.refreshUI();
        }
        if (redisMemoryChart != null) {
            redisMemoryChart.refreshUI();
        }
        if (redisClientConnectionsChart != null) {
            redisClientConnectionsChart.refreshUI();
        }
        if (redisCommandStatsChart != null) {
            redisCommandStatsChart.refreshUI();
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
