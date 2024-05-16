package org.dromara.redisfront.widget.ui;

import lombok.Setter;
import raven.drawer.component.menu.SimpleMenu;
import raven.drawer.component.menu.SimpleMenuOption;

import javax.swing.*;
import java.util.Arrays;

public class DrawerMenu extends SimpleMenu {

    public DrawerMenu(SimpleMenuOption simpleMenuOption) {
        super(simpleMenuOption);
    }

    @Override
    protected void applyMenuEvent(JButton button, int[] index) {
        this.setPopupMenu(button, index);
        super.applyMenuEvent(button, index);
    }

    @Setter
    public static class DrawerMenuItem extends JMenuItem {
        public DrawerMenuItem(String text, String key, int[] index, DrawerMenuItemEvent drawerMenuItemEvent) {
            super(text);
            this.addActionListener(e -> drawerMenuItemEvent.apply(key, Arrays.copyOf(index, index.length)));
        }
    }

    private void setPopupMenu(JButton menuItem, int[] index) {
        DrawerMenuOption simpleMenuOption = (DrawerMenuOption) getSimpleMenuOption();
        JPopupMenu popupMenu = new JPopupMenu();
        DrawerMenuItem open = new DrawerMenuItem("打开", "open", index, simpleMenuOption.getDrawerMenuItemEvent());
        popupMenu.add(open);
        DrawerMenuItem edit = new DrawerMenuItem("编辑", "edit", index, simpleMenuOption.getDrawerMenuItemEvent());
        popupMenu.add(edit);
        DrawerMenuItem delete = new DrawerMenuItem("删除", "delete", index, simpleMenuOption.getDrawerMenuItemEvent());
        popupMenu.add(delete);
        menuItem.setComponentPopupMenu(popupMenu);
    }
}
