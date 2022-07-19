package com.redisfront.ui.component;

import cn.hutool.core.io.resource.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.net.URL;

/**
 * LoadingPanel
 *
 * @author Jin
 */
public class LoadingPanel extends JPanel {


    public static LoadingPanel newInstance() {
        return new LoadingPanel();
    }

    private LoadingPanel() {
        setLayout(new BorderLayout());
        var iconImage = new ImageIcon(ResourceUtil.getResource("gif/21.gif"));
        var iconLabel = new JLabel(iconImage);
        add(iconLabel, BorderLayout.CENTER);
    }
}
