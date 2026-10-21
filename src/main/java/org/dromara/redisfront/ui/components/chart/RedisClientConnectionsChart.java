package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jfree.chart.ChartFactory;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.util.Date;

public class RedisClientConnectionsChart extends AbstractRedisChart {
    private final RedisFrontWidget owner;
    private final TimeSeries clients = new TimeSeries("clients");
    public RedisClientConnectionsChart(RedisConnectContext context, RedisFrontWidget owner) {
        super(context);this.owner=owner;rebuildUI();
    }
    @Override public void rebuildUI() {
        clients.setKey(owner.$tr("RedisClientConnectionsChart.clientConnections.text"));
        setChart(ChartFactory.createTimeSeriesChart("","",owner.$tr("RedisClientConnectionsChart.valueAxisLabel.text"),new TimeSeriesCollection(clients),true,true,false));
    }
    @Override public void acceptSample(RedisMetricSampler.Sample s) {
        clients.addOrUpdate(new Millisecond(new Date(s.timestamp())),s.connectedClients());
    }
    @Override protected void clearDataset() { clients.clear(); }
}
