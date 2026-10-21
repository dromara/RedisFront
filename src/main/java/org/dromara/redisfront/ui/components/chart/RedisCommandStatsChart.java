package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jfree.chart.ChartFactory;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.util.Date;

import javax.swing.*;
import java.awt.BorderLayout;

public class RedisCommandStatsChart extends AbstractRedisChart {
    private final RedisFrontWidget owner;
    private final TimeSeries qps = new TimeSeries("QPS");
    private final JLabel total = new JLabel();
    private long commandCount;
    public RedisCommandStatsChart(RedisConnectContext context, RedisFrontWidget owner) {
        super(context);this.owner=owner;rebuildUI();
    }
    @Override public void rebuildUI() {
        qps.setKey(owner.$tr("RedisCommandStatsChart.qps.text"));
        total.setText(owner.$tr("RedisCommandStatsChart.commandProcessed.text")+": "+commandCount);
        total.setBorder(BorderFactory.createEmptyBorder(5,12,0,0));add(total,BorderLayout.NORTH);
        setChart(ChartFactory.createTimeSeriesChart("","",owner.$tr("RedisCommandStatsChart.qps.text"),new TimeSeriesCollection(qps),true,true,false));
    }
    @Override public void acceptSample(RedisMetricSampler.Sample s) {
        commandCount=s.commandsProcessed();total.setText(owner.$tr("RedisCommandStatsChart.commandProcessed.text")+": "+commandCount);
        qps.addOrUpdate(new Millisecond(new Date(s.timestamp())),s.qps());
    }
    @Override protected void clearDataset() { qps.clear();commandCount=0;total.setText(owner.$tr("RedisCommandStatsChart.commandProcessed.text")+": 0"); }
}
