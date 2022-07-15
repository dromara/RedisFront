package com.redisfront.ui.component;

import cn.hutool.core.img.ImgUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * LoadingPanel
 *
 * @author Jin
 */
public class LoadingPanel extends JPanel {


    public LoadingPanel() {
        setLayout(new BorderLayout());

        BufferedImage bufferedImage = ImgUtil.read("/gif/loading.gif");

    }
}
