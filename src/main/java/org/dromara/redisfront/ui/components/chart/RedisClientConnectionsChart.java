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
public class RedisClientConnectionsChart extends AbstractRedisChart {
    private TimeSeries clientConnectionsSeries;
    private final RedisMonitor redisMonitor;
    private final RedisFrontWidget owner;

    public RedisClientConnectionsChart(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        super(redisConnectContext);
        this.redisMonitor = new RedisMonitor(owner,redisConnectContext);
        this.owner = owner;
        this.initializeUI();
    }

    private void initializeUI() {
        if (clientConnectionsSeries == null) {
            clientConnectionsSeries = new TimeSeries(owner.$tr("RedisClientConnectionsChart.clientConnections.text"));
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(clientConnectionsSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "",
                "",
                owner.$tr("RedisClientConnectionsChart.valueAxisLabel.text"),
                dataset,
                true,
                true,
                false
        );
        setChart(chart);
    }

    @Override
    public void rebuildUI() {
        this.initializeUI();
    }


    private void updateData() {
        RedisUsageInfo redisUsageInfo = redisMonitor.getUsageInfo();
        Millisecond now = new Millisecond();
        clientConnectionsSeries.addOrUpdate(now, redisUsageInfo.getConnectedClients());
    }

    @Override
    protected void updateDataset() {
        try {
            updateData();
        } catch (Exception e) {
            log.error("updateData error", e);
        }
    }
}
