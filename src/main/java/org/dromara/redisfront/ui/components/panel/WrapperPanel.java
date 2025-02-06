package org.dromara.redisfront.ui.components.panel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WrapperPanel extends JPanel {

    public WrapperPanel(JComponent component) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.setBackground(UIManager.getColor("Component.background"));
        setBorder(new EmptyBorder(8, 8, 7, 8));
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }
}
