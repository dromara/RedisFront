package org.dromara.redisfront.ui.widget.main.left;

import org.dromara.redisfront.commons.constant.Res;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MainLeftLogo extends JPanel {
    private static final MainLeftLogo INSTANCE = new MainLeftLogo();

    public static MainLeftLogo getInstance() {
        return INSTANCE;
    }

    public MainLeftLogo() {
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setMinimumSize(new Dimension(196, 58));
        this.setMaximumSize(new Dimension(196, 58));
        this.add(BorderLayout.WEST, new JLabel(Res.REDIS_ICON_45x45));
        this.add(BorderLayout.CENTER, new JLabel(Res.LOGO_TEXT_ICON));
        JLabel subTitleLabel = new JLabel("Cross-Platform Redis GUI Client");
        subTitleLabel.setVerticalAlignment(JLabel.CENTER);
        subTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(BorderLayout.SOUTH, subTitleLabel);
        this.setBorder(BorderFactory.createEmptyBorder(3, 5, 2, 5));
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D roundRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10);
        g2d.setColor(getBackground());
        g2d.fill(roundRect);
    }
}
