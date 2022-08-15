package com.redisfront.ui.component;

import com.formdev.flatlaf.FlatLaf;
import com.redisfront.commons.theme.RedisFrontDarkLaf;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ChartsPanel {


    public ChartsPanel() {

        MeterPlot plot = new MeterPlot(new DefaultValueDataset(120.0));
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(200, 100,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
        g2.dispose();

        JFrame jFrame = new JFrame();
        ChartPanel chartPanel = new ChartPanel(chart);
        jFrame.setContentPane(chartPanel);
        jFrame.pack();
        jFrame.setVisible(true);


    }

    public static void main(String[] args) {
        RedisFrontDarkLaf.setup();
        new ChartsPanel();
    }
}
