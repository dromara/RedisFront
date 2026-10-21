package org.dromara.redisfront.ui.components.chart;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jfree.chart.ChartFactory;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import java.util.Date;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import javax.swing.UIManager;
import java.awt.Color;

public class RedisMemoryChart extends AbstractRedisChart {
    private final RedisFrontWidget owner;
    private final TimeSeries used = new TimeSeries("used");
    private final TimeSeries rss = new TimeSeries("rss");
    private final TimeSeries fragmentation = new TimeSeries("fragmentation");
    private double maxMemory;
    public RedisMemoryChart(RedisConnectContext context, RedisFrontWidget owner) {
        super(context);this.owner=owner;rebuildUI();
    }
    @Override public void rebuildUI() {
        used.setKey(owner.$tr("RedisMemoryChart.usedMemorySeries.text"));rss.setKey(owner.$tr("RedisMemoryChart.rssMemorySeries.text"));
        fragmentation.setKey(owner.$tr("RedisMemoryChart.fragmentationSeries.text"));
        var memory=new TimeSeriesCollection(used);memory.addSeries(rss);
        var chart=ChartFactory.createTimeSeriesChart("","",owner.$tr("RedisMemoryChart.memoryAxis.text"),memory,true,true,false);
        var plot=chart.getXYPlot();
        plot.setDataset(1,new TimeSeriesCollection(fragmentation));
        plot.setRangeAxis(1,new NumberAxis(owner.$tr("RedisMemoryChart.fragmentationAxis.text")));
        plot.mapDatasetToRangeAxis(1,1);
        plot.setRenderer(1,new XYLineAndShapeRenderer(true,false));
        setChart(chart);updateLimit();
    }
    private void updateLimit() {
        var plot=getChart().getXYPlot();plot.clearRangeMarkers();
        if(maxMemory>0) {
            var marker=new ValueMarker(maxMemory/1024/1024);marker.setPaint(new Color(0xD95858));
            marker.setLabel(owner.$tr("ReportPageView.maxMemory"));marker.setLabelPaint(UIManager.getColor("Label.foreground"));
            plot.addRangeMarker(marker);
        }
    }
    @Override public void acceptSample(RedisMetricSampler.Sample s) {
        var time=new Millisecond(new Date(s.timestamp()));
        used.addOrUpdate(time,s.usedMemory()/1024/1024);rss.addOrUpdate(time,s.rssMemory()/1024/1024);
        fragmentation.addOrUpdate(time,s.fragmentationRatio());maxMemory=s.maxMemory();updateLimit();
    }
    @Override protected void clearDataset() { used.clear();rss.clear();fragmentation.clear();maxMemory=0;updateLimit(); }
}
