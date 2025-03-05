package org.dromara.redisfront.ui.components.chart;

import lombok.Getter;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;

@Getter
public abstract class AbstractRedisChart extends JPanel {
    protected final RedisConnectContext redisConnectContext;

    protected AbstractRedisChart(RedisConnectContext redisConnectContext) {
        this.redisConnectContext = redisConnectContext;
        this.setLayout(new BorderLayout());
        Timer timer = new Timer(2000, _ -> updateDataset());
        timer.start();
    }

    protected void setChart(JFreeChart chart) {
        Color flatBackground = UIManager.getColor("Panel.background");
        chart.setBackgroundPaint(flatBackground);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(flatBackground);
            chart.getLegend().setFrame(BlockBorder.NONE);
        }

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(flatBackground);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);
        plot.setDomainZeroBaselinePaint(new Color(220, 220, 220));
        plot.setRangeZeroBaselinePaint(new Color(220, 220, 220));

        plot.getRenderer().setSeriesPaint(0, new Color(0x3A8FD8));
        plot.getRenderer().setSeriesPaint(1, new Color(0xD83A8F));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        chartPanel.setBackground(flatBackground);
        chartPanel.setOpaque(true);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(chartPanel, BorderLayout.CENTER);
    }

    private void applyTheme() {
        StandardChartTheme flatTheme = new StandardChartTheme("Flat");
        Font baseFont = UIManager.getFont("Label.font");
        Color foreground = UIManager.getColor("Label.foreground");
        Color background = UIManager.getColor("Panel.background");
        flatTheme.setExtraLargeFont(baseFont);
        flatTheme.setLargeFont(baseFont);
        flatTheme.setRegularFont(baseFont);
        flatTheme.setTitlePaint(foreground);
        flatTheme.setChartBackgroundPaint(background);
        flatTheme.setPlotBackgroundPaint(background);
        flatTheme.setLegendBackgroundPaint(background);
        flatTheme.setDomainGridlinePaint(new Color(220, 220, 220));
        flatTheme.setRangeGridlinePaint(new Color(220, 220, 220));
        flatTheme.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        flatTheme.setShadowVisible(false);
        ChartFactory.setChartTheme(flatTheme);
    }

    public void refreshUI() {
        this.applyTheme();
        this.removeAll();
        this.rebuildUI();
        this.revalidate();
        this.repaint();
    }

    protected abstract void rebuildUI();

    protected abstract void updateDataset();

}
