package org.dromara.redisfront.widget.ui;

import org.dromara.redisfront.commons.constant.Res;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CombLogoPanel extends JPanel {
    private static final CombLogoPanel INSTANCE = new CombLogoPanel();
    public static CombLogoPanel getInstance() {
        return INSTANCE;
    }

    public CombLogoPanel() {
        this.setOpaque(false);
        this.setLayout(new FlowLayout());
        this.setMinimumSize(new Dimension(220,55));
        this.add(new JLabel(Res.REDIS_ICON_45x45));
        this.add(new JLabel(Res.LOGO_TEXT_ICON));
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
