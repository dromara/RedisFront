package org.dromara.redisfront.ui.components.chart;

import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMonitor;
import org.dromara.redisfront.ui.components.monitor.RedisUsageInfo;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

@Slf4j
public class RedisNetworkChart extends AbstractRedisChart {
    private TimeSeries inSeries;
    private TimeSeries outSeries;
    private final RedisMonitor redisMonitor;
    private final RedisFrontWidget owner;


    public RedisNetworkChart(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        super(redisConnectContext);
        this.redisMonitor = new RedisMonitor(owner,redisConnectContext);
        this.owner = owner;
        this.initializeUI();
    }

    private void initializeUI() {
        TimeSeriesCollection dataset = createDataset();
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",
                "",
                owner.$tr("RedisNetworkChart.valueAxisLabel.text"),
                dataset,
                true,
                true,
                false
        );
        setChart(chart);
    }

    private TimeSeriesCollection createDataset() {
        if (inSeries == null) {
            inSeries = new TimeSeries(owner.$tr("RedisNetworkChart.inSeries.text"));
            inSeries.setMaximumItemCount(30);
            inSeries.removeAgedItems(false);
        }
        if (outSeries == null) {
            outSeries = new TimeSeries(owner.$tr("RedisNetworkChart.outSeries.text"));
            outSeries.setMaximumItemCount(30);
            outSeries.removeAgedItems(false);
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
        try {
            updateData();
        } catch (Exception e) {
            log.error("updateData error", e);
        }
    }

    @Override
    protected void clearDataset() {
        this.inSeries.clear();
        this.outSeries.clear();
    }

    private void updateData() {
        RedisUsageInfo.NetworkStats networkStats = redisMonitor.calculateNetworkRate();
        Millisecond now = new Millisecond();
        inSeries.addOrUpdate(now, networkStats.inputRate() / 1024);
        outSeries.addOrUpdate(now, networkStats.outputRate() / 1024);
    }


}
