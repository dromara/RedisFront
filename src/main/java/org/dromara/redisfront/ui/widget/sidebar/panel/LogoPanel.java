package org.dromara.redisfront.ui.widget.sidebar.panel;

import org.dromara.redisfront.commons.resources.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;

public class LogoPanel extends JPanel {
    private static final LogoPanel INSTANCE = new LogoPanel();

    public static LogoPanel getInstance() {
        return INSTANCE;
    }

    public LogoPanel() {
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.setMaximumSize(new Dimension(190, 60));
        this.add(BorderLayout.WEST, new JLabel(Icons.REDIS_ICON_45x45));
        this.add(BorderLayout.CENTER, new JLabel(Icons.LOGO_TEXT_ICON));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    System.gc();
                }
            }
        });
        JLabel subTitleLabel = new JLabel("Cross-Platform Redis GUI Client");
        subTitleLabel.setVerticalAlignment(JLabel.CENTER);
        subTitleLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(BorderLayout.SOUTH, subTitleLabel);
        this.setBorder(BorderFactory.createEmptyBorder(3, 10, 2, 10));
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RoundRectangle2D roundRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15);
        g2d.setColor(getBackground());
        g2d.fill(roundRect);
    }
}
