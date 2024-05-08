package org.dromara.redisfront.widget.main.ui;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import javax.swing.*;
import java.awt.*;

public class RedisFrontTabbedPane extends JTabbedPane {

    public RedisFrontTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }

    public RedisFrontTabbedPane() {
    }

    public RedisFrontTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
    }

    @Override
    public void updateUI() {
        setUI(new BoldTabbedPaneUI());
    }


    static class BoldTabbedPaneUI extends FlatTabbedPaneUI {



        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            if (isSelected) {
                font = font.deriveFont(Font.BOLD);
            }
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
        }
    }

}
