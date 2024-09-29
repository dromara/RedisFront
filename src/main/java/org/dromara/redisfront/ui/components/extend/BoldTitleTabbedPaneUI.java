package org.dromara.redisfront.ui.components.extend;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

import java.awt.*;

public class BoldTitleTabbedPaneUI extends FlatTabbedPaneUI {

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            if (isSelected) {
                font = font.deriveFont(Font.BOLD);
            }
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
        }
    }