package org.dromara.redisfront.widget.main.panel.drawer;

import org.dromara.redisfront.commons.constant.UI;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Logo extends JPanel {
    private static final Logo INSTANCE = new Logo();
    public static Logo getInstance() {
        return INSTANCE;
    }

    public Logo() {
        this.setOpaque(false);
        this.setLayout(new FlowLayout());
        this.setMinimumSize(new Dimension(220,55));
        this.add(new JLabel(UI.REDIS_ICON_45x45));
        JLabel logoFont = new JLabel(UI.LOGO_TEXT_ICON);
        this.add(logoFont);
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
