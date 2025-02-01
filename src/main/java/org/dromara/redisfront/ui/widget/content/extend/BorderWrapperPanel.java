package org.dromara.redisfront.ui.widget.content.extend;

import com.formdev.flatlaf.ui.FlatLineBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BorderWrapperPanel extends JPanel {

    public BorderWrapperPanel(JComponent component) {
        JPanel contentPanel = new JPanel(){
            @Override
            public void updateUI() {
                super.updateUI();
                var flatLineBorder = new FlatLineBorder(new Insets(10, 10, 10, 10), UIManager.getColor("Component.borderColor"), 1, 5);
                setBorder(flatLineBorder);
                add(component, BorderLayout.CENTER);
            }
        };
        var flatLineBorder = new FlatLineBorder(new Insets(10, 10, 10, 10), UIManager.getColor("Component.borderColor"),1,5);
        contentPanel.setBorder(flatLineBorder);
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.setBackground(UIManager.getColor("Component.background"));
        setBorder(new EmptyBorder(8,8,8,8));
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }
}
