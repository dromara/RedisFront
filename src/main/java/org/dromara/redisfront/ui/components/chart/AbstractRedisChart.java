package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.TimeSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;

public abstract class AbstractRedisChart extends JPanel {
    protected final RedisConnectContext redisConnectContext;
    private JFreeChart chart;
    private int historyMinutes = 1;

    protected AbstractRedisChart(RedisConnectContext context) {
        this.redisConnectContext = context;
        setLayout(new BorderLayout());
    }

    public JFreeChart getChart() { return chart; }
    public void resetData() { clearDataset(); }
    public abstract void acceptSample(RedisMetricSampler.Sample sample);

    protected void setChart(JFreeChart chart) {
        this.chart = chart;
        Color background = UIManager.getColor("Panel.background");
        Color foreground = UIManager.getColor("Label.foreground");
        Font font = UIManager.getFont("Label.font");
        Color grid = new Color((background.getRed()*4+foreground.getRed())/5,
                (background.getGreen()*4+foreground.getGreen())/5,
                (background.getBlue()*4+foreground.getBlue())/5);
        chart.setBackgroundPaint(background);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(background);
            chart.getLegend().setItemPaint(foreground);
            chart.getLegend().setItemFont(font);
            chart.getLegend().setFrame(BlockBorder.NONE);
        }
        var plot = chart.getXYPlot();
        plot.setBackgroundPaint(background);
        plot.setDomainGridlinePaint(grid);
        plot.setRangeGridlinePaint(grid);
        plot.setOutlineVisible(false);
        for (int i=0;i<plot.getDomainAxisCount();i++) styleAxis(plot.getDomainAxis(i), foreground, font);
        for (int i=0;i<plot.getRangeAxisCount();i++) styleAxis(plot.getRangeAxis(i), foreground, font);
        if (plot.getDomainAxis() instanceof DateAxis date) date.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss"));
        for (int i=0;i<plot.getRendererCount();i++) {
            var renderer=plot.getRenderer(i);
            if (renderer!=null) {
                renderer.setSeriesPaint(0, new Color(i==0?0x3A8FD8:0x00AE57));
                renderer.setSeriesPaint(1, new Color(0xD83A8F));
                renderer.setDefaultStroke(new BasicStroke(1.8f));
                if (renderer instanceof XYLineAndShapeRenderer lines) {
                    lines.setDefaultShapesVisible(true);
                    lines.setDefaultShape(new java.awt.geom.Ellipse2D.Double(-1.5,-1.5,3,3));
                }
            }
        }
        setHistoryMinutes(historyMinutes);
        var panel = new ChartPanel(chart);
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(panel, BorderLayout.CENTER);
    }

    private void styleAxis(ValueAxis axis, Color foreground, Font font) {
        if (axis==null) return;
        axis.setLabelPaint(foreground);
        axis.setTickLabelPaint(foreground);
        axis.setAxisLinePaint(foreground);
        axis.setTickMarkPaint(foreground);
        axis.setLabelFont(font);
        axis.setTickLabelFont(font);
        if (axis instanceof NumberAxis number) {
            number.setRangeType(RangeType.POSITIVE);
            number.setAutoRangeMinimumSize(1.0);
        }
    }

    public void setHistoryMinutes(int minutes) {
        historyMinutes=minutes;
        if (chart==null) return;
        var plot=chart.getXYPlot();
        if (plot.getDomainAxis() instanceof DateAxis axis) axis.setFixedAutoRange(minutes*60_000.0);
        // Keep 15 minutes even when the visible window is shorter.
        for(int i=0;i<plot.getDatasetCount();i++) {
            if(plot.getDataset(i) instanceof TimeSeriesCollection data) {
                for(int j=0;j<data.getSeriesCount();j++) data.getSeries(j).setMaximumItemCount(450);
            }
        }
    }

    public void refreshUI() {
        removeAll();
        rebuildUI();
        revalidate();
        repaint();
    }
    protected abstract void rebuildUI();
    protected abstract void clearDataset();
}
