package org.dromara.redisfront.ui.components.panel;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.LayerUI;
import java.awt.*;

import static org.dromara.redisfront.commons.resources.Icons.REDIS_ICON_45x45;

public class BorderNonePanel extends JPanel {


    public BorderNonePanel() {
        setOpaque(false);
        JPanel bodyPanel = getContentPanel();
        bodyPanel.setOpaque(false);
        JPanel contentPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                add(bodyPanel, BorderLayout.CENTER);
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(bodyPanel, BorderLayout.CENTER);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel getContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        JLabel iconLabel = new JLabel(REDIS_ICON_45x45);

        JLayer<JLabel> jLayer = new JLayer<>(iconLabel, new LayerUI<>() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
                super.paint(g2d, c);
                g2d.dispose();
            }
        });
        contentPanel.add(jLayer, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        contentPanel.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        contentPanel.add(spacer2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        contentPanel.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        contentPanel.add(spacer4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        return contentPanel;
    }
}
