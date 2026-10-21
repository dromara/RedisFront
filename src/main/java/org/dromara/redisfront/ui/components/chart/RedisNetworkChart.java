package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jfree.chart.ChartFactory;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.util.Date;

public class RedisNetworkChart extends AbstractRedisChart {
    private final RedisFrontWidget owner;
    private final TimeSeries input = new TimeSeries("input");
    private final TimeSeries output = new TimeSeries("output");
    public RedisNetworkChart(RedisConnectContext context, RedisFrontWidget owner) {
        super(context);this.owner=owner;rebuildUI();
    }
    @Override public void rebuildUI() {
        input.setKey(owner.$tr("RedisNetworkChart.inSeries.text"));
        output.setKey(owner.$tr("RedisNetworkChart.outSeries.text"));
        var data=new TimeSeriesCollection(input);data.addSeries(output);
        setChart(ChartFactory.createTimeSeriesChart("","",owner.$tr("RedisNetworkChart.valueAxisLabel.text"),data,true,true,false));
    }
    @Override public void acceptSample(RedisMetricSampler.Sample s) {
        var time=new Millisecond(new Date(s.timestamp()));
        input.addOrUpdate(time,s.inputRate()/1024);output.addOrUpdate(time,s.outputRate()/1024);
    }
    @Override protected void clearDataset() { input.clear();output.clear(); }
}
