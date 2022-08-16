package com.redisfront.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeSeriesChart {
    ChartPanel frame1;
    XYPlot xyplot;
    public TimeSeriesChart(Object[] columnTitle, Object[][] tableData, String fileName, int[] picks) {
        XYDataset xydataset = createDataset(columnTitle, tableData, picks);
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(fileName, "时间/min", "数值", xydataset, true, true,
                true);
        xyplot = (XYPlot) jfreechart.getPlot();
        DateAxis dateaxis = (DateAxis) xyplot.getDomainAxis();
//        dateaxis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        dateaxis.setTickUnit(new DateTickUnit(DateTickUnitType.MINUTE, 1, new SimpleDateFormat("mm")));
        dateaxis.setAutoRange(true);
        frame1 = new ChartPanel(jfreechart, true);
        dateaxis.setLabelFont(new Font("黑体", Font.BOLD, 14)); // 水平底部标题
        dateaxis.setTickLabelFont(new Font("华文楷体", Font.BOLD, 12)); // 垂直标题
        ValueAxis rangeAxis = xyplot.getRangeAxis();// 获取柱状
        rangeAxis.setLabelFont(new Font("华文楷体", Font.BOLD, 15));
        jfreechart.getLegend().setItemFont(new Font("华文楷体", Font.BOLD, 15));
        jfreechart.getTitle().setFont(new Font("宋体", Font.BOLD, 20));// 设置标题字体
    }
    public void updateData(Object[] columnTitle, Object[][] tableData, String fileName, int[] picks) {
        XYDataset xydataset = createDataset(columnTitle, tableData, picks);
        xyplot.setDataset(xydataset);
    }
    private XYDataset createDataset(Object[] columnTitle, Object[][] tableData, int[] picks) { //
        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int a = 0; a < picks.length; a++) {
            if (picks[a] == 1) {
                TimeSeries timeseries = new TimeSeries(columnTitle[a].toString());
                for (Object[] datas : tableData) {
                    try {
                        timeseries.add(new Second(format.parse(datas[0].toString())),
                                Double.valueOf(datas[a].toString()));
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                timeseriescollection.addSeries(timeseries);
            }
        }
        return timeseriescollection;
    }
    public ChartPanel getChartPanel() {
        return frame1;
    }
}