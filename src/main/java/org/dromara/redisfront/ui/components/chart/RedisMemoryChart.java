package org.dromara.redisfront.ui.components.chart;

import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMonitor;
import org.dromara.redisfront.ui.components.monitor.RedisUsageInfo;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;

import static org.jfree.chart.ui.RectangleAnchor.BOTTOM_RIGHT;
import static org.jfree.chart.ui.RectangleAnchor.TOP_LEFT;
import static org.jfree.chart.ui.TextAnchor.BOTTOM_LEFT;
import static org.jfree.chart.ui.TextAnchor.TOP_RIGHT;

@Slf4j
public class RedisMemoryChart extends AbstractRedisChart {
    private TimeSeries usedMemorySeries;
    private TimeSeries rssMemorySeries;
    private TimeSeries fragmentationSeries;
    private final RedisMonitor redisMonitor;
    private final RedisFrontWidget owner;

    private XYPlot plot;

    public RedisMemoryChart(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        super(redisConnectContext);
        this.redisMonitor = new RedisMonitor(owner,redisConnectContext);
        this.owner = owner;
        initializeUI();
    }

    private void initializeUI() {
        if (usedMemorySeries == null) {
            usedMemorySeries = new TimeSeries(owner.$tr("RedisMemoryChart.usedMemorySeries.text"));
            usedMemorySeries.setMaximumItemCount(30);
            usedMemorySeries.removeAgedItems(false);
        }
        if (rssMemorySeries == null) {
            rssMemorySeries = new TimeSeries(owner.$tr("RedisMemoryChart.rssMemorySeries.text"));
            rssMemorySeries.setMaximumItemCount(30);
            rssMemorySeries.removeAgedItems(false);
        }
        if (fragmentationSeries == null) {
            fragmentationSeries = new TimeSeries(owner.$tr("RedisMemoryChart.fragmentationSeries.text"));
            fragmentationSeries.setMaximumItemCount(30);
            fragmentationSeries.removeAgedItems(false);
        }

        TimeSeriesCollection memoryDataset = new TimeSeriesCollection();
        memoryDataset.addSeries(usedMemorySeries);

        TimeSeriesCollection rssDataset = new TimeSeriesCollection();
        rssDataset.addSeries(rssMemorySeries);

        TimeSeriesCollection fragmentationDataset = new TimeSeriesCollection();
        fragmentationDataset.addSeries(fragmentationSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",
                "",
                owner.$tr("RedisMemoryChart.valueAxisLabel.text"),
                memoryDataset
        );
        plot = chart.getXYPlot();

        Color flatBackground = UIManager.getColor("ChartPanel.textColor");

        NumberAxis memoryAxis = new NumberAxis(owner.$tr("RedisMemoryChart.memoryAxis.text"));
        memoryAxis.setLabelPaint(flatBackground);
        memoryAxis.setTickLabelPaint(flatBackground);
        plot.setRangeAxis(0, memoryAxis);

        NumberAxis fragmentationAxis = new NumberAxis(owner.$tr("RedisMemoryChart.fragmentationAxis.text"));
        fragmentationAxis.setLabelPaint(flatBackground);
        fragmentationAxis.setTickLabelPaint(flatBackground);
        fragmentationAxis.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, fragmentationAxis);

        plot.setDataset(0, memoryDataset);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, rssDataset);
        plot.mapDatasetToRangeAxis(1, 0);

        plot.setDataset(2, fragmentationDataset);
        plot.mapDatasetToRangeAxis(2, 1);

        XYLineAndShapeRenderer memoryRenderer = new XYLineAndShapeRenderer(true, false);
        memoryRenderer.setSeriesPaint(0, new Color(0x3A8FD8));
        plot.setRenderer(0, memoryRenderer);


        XYLineAndShapeRenderer rssRenderer = new XYLineAndShapeRenderer(true, false);
        rssRenderer.setSeriesPaint(0, new Color(0xD83A8F));
        plot.setRenderer(1, rssRenderer);


        XYLineAndShapeRenderer fragmentationRenderer = new XYLineAndShapeRenderer(true, false);
        fragmentationRenderer.setSeriesPaint(0, new Color(0x00AE57));
        plot.setRenderer(2, fragmentationRenderer);

        double maxMemory = 1024;
        ValueMarker maxMemoryMarker = new ValueMarker(maxMemory);
        maxMemoryMarker.setPaint(Color.RED);
        maxMemoryMarker.setStroke(new BasicStroke(2.0f));
        maxMemoryMarker.setLabel("Max Memory Limit");
        maxMemoryMarker.setLabelAnchor(TOP_LEFT);
        maxMemoryMarker.setLabelTextAnchor(BOTTOM_LEFT);
        plot.addRangeMarker(maxMemoryMarker);

        setChart(chart);
    }

    private void updateData() {
        Millisecond now = new Millisecond();
        RedisUsageInfo.MemoryUsage memoryUsage = redisMonitor.memoryUsageInfo();
        usedMemorySeries.add(now, memoryUsage.usedMemory());
        rssMemorySeries.add(now, memoryUsage.usedMemoryRss());
        fragmentationSeries.add(now, memoryUsage.fragmentationRatio());

        plot.clearDomainMarkers();

        double fragmentationThreshold = 1.5;
        if (memoryUsage.fragmentationRatio() > fragmentationThreshold) {
            Marker warningMarker = new ValueMarker(now.getMillisecond());
            warningMarker.setPaint(Color.RED);
            warningMarker.setStroke(new BasicStroke(1.5f));
            warningMarker.setLabel("⚠ High Fragmentation");
            warningMarker.setLabelAnchor(BOTTOM_RIGHT);
            warningMarker.setLabelTextAnchor(TOP_RIGHT);
            plot.addDomainMarker(warningMarker);
        }
    }

    @Override
    public void rebuildUI() {
        initializeUI();
    }

    @Override
    protected void updateDataset() {
        try {
            updateData();
        } catch (Exception e) {
            log.error("updateData error", e);
        }
    }

    @Override
    protected void clearDataset() {
        this.usedMemorySeries.clear();
        this.rssMemorySeries.clear();
        this.fragmentationSeries.clear();
    }
}
