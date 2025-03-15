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
public class RedisCommandStatsChart extends AbstractRedisChart {
    private TimeSeries commandProcessedSeries;
    private TimeSeries qpsSeries;

    private final RedisMonitor redisMonitor;
    private final RedisFrontWidget owner;

    private long lastCommandCount = 0;
    private long lastTimestamp = System.currentTimeMillis();

    public RedisCommandStatsChart(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
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
                owner.$tr("RedisCommandStatsChart.valueAxisLabel"),
                dataset,
                true,
                true,
                false
        );
        setChart(chart);
    }

    private TimeSeriesCollection createDataset() {
        if (commandProcessedSeries == null) {
            commandProcessedSeries = new TimeSeries(owner.$tr("RedisCommandStatsChart.commandProcessed.text"));
        }
        if (qpsSeries == null) {
            qpsSeries = new TimeSeries(owner.$tr("RedisCommandStatsChart.qps.text"));
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(commandProcessedSeries);
        dataset.addSeries(qpsSeries);
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

    private void updateData() {
        RedisUsageInfo redisUsageInfo = redisMonitor.getUsageInfo();
        Millisecond now = new Millisecond();

        // 获取当前命令执行总数
        long currentCommandCount = redisUsageInfo.getCommandsProcessed();
        long currentTime = System.currentTimeMillis();

        // 计算 QPS
        double qps = 0;
        if (lastTimestamp != 0) {
            long timeDiff = currentTime - lastTimestamp;
            if (timeDiff > 0) {
                qps = (currentCommandCount - lastCommandCount) * 1000.0 / timeDiff;
            }
        }

        // 更新数据
        commandProcessedSeries.addOrUpdate(now, currentCommandCount);
        qpsSeries.addOrUpdate(now, qps);

        // 记录本次数据
        lastCommandCount = currentCommandCount;
        lastTimestamp = currentTime;
    }
}
