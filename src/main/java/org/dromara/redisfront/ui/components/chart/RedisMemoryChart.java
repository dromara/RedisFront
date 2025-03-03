package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

import static org.jfree.chart.ui.RectangleAnchor.BOTTOM_RIGHT;
import static org.jfree.chart.ui.RectangleAnchor.TOP_LEFT;
import static org.jfree.chart.ui.TextAnchor.BOTTOM_LEFT;
import static org.jfree.chart.ui.TextAnchor.TOP_RIGHT;

public class RedisMemoryChart extends AbstractRedisChart {
    private XYSeries usedMemorySeries;
    private XYSeries rssMemorySeries;
    private XYSeries fragmentationSeries;

    private XYPlot plot;
    private int timeCounter = 1;

    public RedisMemoryChart(RedisConnectContext redisConnectContext) {
        super(redisConnectContext);
        initializeUI();
    }

    private void initializeUI() {
        usedMemorySeries = new XYSeries("使用内存 (MB)");
        rssMemorySeries = new XYSeries("使用内存 RSS (MB)");
        fragmentationSeries = new XYSeries("碎片率");

        XYSeriesCollection memoryDataset = new XYSeriesCollection();
        memoryDataset.addSeries(usedMemorySeries);

        XYSeriesCollection rssDataset = new XYSeriesCollection();
        rssDataset.addSeries(rssMemorySeries);

        XYSeriesCollection fragmentationDataset = new XYSeriesCollection();
        fragmentationDataset.addSeries(fragmentationSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Redis内存使用",
                "时间",
                "内存 (MB)",
                memoryDataset
        );
        plot = chart.getXYPlot();

        NumberAxis memoryAxis = new NumberAxis("内存 (MB)");
        plot.setRangeAxis(0, memoryAxis);


        NumberAxis fragmentationAxis = new NumberAxis("碎片率 Ratio");
        fragmentationAxis.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, fragmentationAxis);

        plot.setDataset(0, memoryDataset);
        plot.mapDatasetToRangeAxis(0, 0);

        plot.setDataset(1, rssDataset);
        plot.mapDatasetToRangeAxis(1, 0);

        plot.setDataset(2, fragmentationDataset);
        plot.mapDatasetToRangeAxis(2, 1);


        XYLineAndShapeRenderer memoryRenderer = new XYLineAndShapeRenderer(true, false);
        memoryRenderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(0, memoryRenderer);


        XYLineAndShapeRenderer rssRenderer = new XYLineAndShapeRenderer(true, false);
        rssRenderer.setSeriesPaint(0, Color.ORANGE);
        plot.setRenderer(1, rssRenderer);


        XYLineAndShapeRenderer fragmentationRenderer = new XYLineAndShapeRenderer(true, false);
        fragmentationRenderer.setSeriesPaint(0, Color.MAGENTA);
        plot.setRenderer(2, fragmentationRenderer);

        double maxMemory = 1200;
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
        double usedMemory = 500 + timeCounter * 50 + (Math.random() * 30);
        double usedMemoryRss = usedMemory * (1 + Math.random() * 0.3);
        double fragmentationRatio = usedMemoryRss / usedMemory;

        usedMemorySeries.add(timeCounter, usedMemory);
        rssMemorySeries.add(timeCounter, usedMemoryRss);
        fragmentationSeries.add(timeCounter, fragmentationRatio);

        plot.clearDomainMarkers();

        double fragmentationThreshold = 1.5;
        if (fragmentationRatio > fragmentationThreshold) {
            Marker warningMarker = new ValueMarker(timeCounter);
            warningMarker.setPaint(Color.RED);
            warningMarker.setStroke(new BasicStroke(1.5f));
            warningMarker.setLabel("⚠ High Fragmentation");
            warningMarker.setLabelAnchor(BOTTOM_RIGHT);
            warningMarker.setLabelTextAnchor(TOP_RIGHT);
            plot.addDomainMarker(warningMarker);
        }

        timeCounter++;
    }

    @Override
    public void rebuildUI() {
        initializeUI();
    }

    @Override
    protected void updateDataset() {
        updateData();
    }
}
