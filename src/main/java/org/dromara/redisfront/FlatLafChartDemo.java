package org.dromara.redisfront;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class FlatLafChartDemo {

    public static void main(String[] args) {
        // 初始化FlatLaf主题
        FlatMacLightLaf.setup();

        // 创建数据集
        XYSeries series = new XYSeries("Sample Data");
        double[] xValues = {1.249860779781E12, 1.249861186272E12, 1.249861592763E12,
                          1.249861999254E12, 1.249862405745E12};
        String[] labels = {"Hades", "Hagen", "Herakles", "Hermes", "Horowitz"};

        for (int i = 0; i < xValues.length; i++) {
            series.add(xValues[i], Math.random() * 100);
        }

        // 创建图表
        JFreeChart chart = ChartFactory.createXYLineChart(
                "FlatLaf Style Chart",
                "X-Axis",
                "Y-Axis",
                new XYSeriesCollection(series)
        );

        // 获取FlatLaf颜色配置
        Color bgColor = UIManager.getColor("Panel.background");
        Color textColor = UIManager.getColor("Label.foreground");

        // 配置图表样式
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(bgColor);
        plot.setDomainGridlinePaint(UIManager.getColor("Component.borderColor"));
        plot.setRangeGridlinePaint(UIManager.getColor("Component.borderColor"));

        // 配置渲染器
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        renderer.setSeriesPaint(0, UIManager.getColor("Component.accentColor")); // 使用主题色
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6)); // 数据点形状

        // 配置坐标轴
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setTickLabelPaint(textColor);
        xAxis.setLabelPaint(textColor);
        xAxis.setTickLabelFont(UIManager.getFont("Label.font")); // 同步系统字体

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setTickLabelPaint(textColor);
        yAxis.setLabelPaint(textColor);
        yAxis.setTickLabelFont(UIManager.getFont("Label.font"));

        // 配置图表全局样式
        chart.setBackgroundPaint(bgColor);
        chart.getTitle().setPaint(textColor);
        chart.getLegend().setBackgroundPaint(bgColor);
        chart.getLegend().setItemPaint(textColor);

        // 创建并显示窗口
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paint(g2);
                g2.dispose();
            }
        };
        chartPanel.setPreferredSize(new Dimension(800, 600));

        frame.add(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
