package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMonitor;
import org.dromara.redisfront.ui.components.monitor.RedisUsageInfo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class RedisNetworkChart extends AbstractRedisChart {
    private TimeSeries inSeries;
    private TimeSeries outSeries;
    private final RedisMonitor redisMonitor;


    public RedisNetworkChart(RedisConnectContext redisConnectContext) {
        super(redisConnectContext);
        this.redisMonitor = new RedisMonitor(redisConnectContext);
        this.initializeUI();
    }

    private void initializeUI() {
        TimeSeriesCollection dataset = createDataset();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",
                "",
                "带宽 (KB/s)",
                dataset,
                true,
                true,
                false
        );
        setChart(chart);
    }

    private TimeSeriesCollection createDataset() {
        if (inSeries == null) {
            inSeries = new TimeSeries("进流量");
        }
        if (outSeries == null) {
            outSeries = new TimeSeries("出流量");
        }
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(inSeries);
        dataset.addSeries(outSeries);
        return dataset;
    }

    @Override
    public void rebuildUI() {
        this.initializeUI();
    }

    @Override
    protected void updateDataset() {
        RedisUsageInfo.NetworkStats networkStats = redisMonitor.calculateNetworkRate();
        Millisecond now = new Millisecond();
        inSeries.addOrUpdate(now, networkStats.inputRate());
        outSeries.addOrUpdate(now, networkStats.outputRate());
    }


}
