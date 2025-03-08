package org.dromara.redisfront.ui.widget.main.listener;

import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseDraggedListener implements MouseListener, MouseMotionListener {
    private int mouseX, mouseY;
    private final RedisFrontWidget owner;

    public MouseDraggedListener(RedisFrontWidget owner) {
        this.owner = owner;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        owner.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // 计算新窗口位置
        int newX = owner.getX() + (e.getX() - mouseX);
        int newY = owner.getY() + (e.getY() - mouseY);
        this.owner.setLocation(newX, newY);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }


    @Override
    public void mouseReleased(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0) {
            owner.setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0) {
            owner.setCursor(Cursor.getDefaultCursor());
        }
    }


    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
