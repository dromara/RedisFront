package org.dromara.redisfront.ui.components.chart;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.monitor.RedisMetricSampler;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import static org.junit.jupiter.api.Assertions.*;

class ChartThemeTest {
    static class Probe extends AbstractRedisChart {
        Probe() { super(new RedisConnectContext());rebuildUI(); }
        @Override protected void rebuildUI() {
            var data=new TimeSeriesCollection(new TimeSeries("sample"));
            var chart=ChartFactory.createTimeSeriesChart("","time","memory",data,true,true,false);
            chart.getXYPlot().setRangeAxis(1,new NumberAxis("ratio"));setChart(chart);
        }
        @Override public void acceptSample(RedisMetricSampler.Sample sample) { }
        @Override protected void clearDataset() { }
    }

    @Test void allAxesAndLegendFollowLightAndDarkThemesWithoutChangingTheGlobalChartTheme() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FlatLaf.registerCustomDefaultsSource(Constants.APP_THEME_PACKAGE);
            var globalTheme=ChartFactory.getChartTheme();
            FlatMacLightLaf.setup();var probe=new Probe();
            for(boolean dark:new boolean[]{false,true,false}) {
                if(dark) FlatMacDarkLaf.setup();else FlatMacLightLaf.setup();
                probe.refreshUI();var chart=probe.getChart();var plot=chart.getXYPlot();
                var color=UIManager.getColor("Label.foreground");
                assertEquals(color,plot.getDomainAxis().getTickLabelPaint());
                assertEquals(color,plot.getRangeAxis(0).getLabelPaint());
                assertEquals(color,plot.getRangeAxis(1).getTickLabelPaint());
                assertEquals(color,chart.getLegend().getItemPaint());
                assertNotEquals(plot.getBackgroundPaint(),color);
                assertTrue(plot.getRangeAxis(0).getLowerBound() >= 0);
                assertTrue(plot.getRangeAxis(0).getRange().getLength() >= 0.5);
                assertSame(globalTheme,ChartFactory.getChartTheme());
            }
        });
    }
}
