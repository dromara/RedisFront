package com.redisfront.ui.component;

import cn.hutool.core.io.resource.ResourceUtil;

import javax.swing.*;
import java.awt.*;

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
        var icon = new JLabel(new ImageIcon(ResourceUtil.getResource("gif/21.gif")));
        add(icon, BorderLayout.CENTER);
    }
}
