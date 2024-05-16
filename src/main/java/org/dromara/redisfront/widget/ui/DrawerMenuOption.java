package org.dromara.redisfront.widget.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import lombok.Getter;
import lombok.Setter;
import raven.drawer.component.menu.SimpleMenuOption;

import javax.swing.*;
import java.awt.*;

@Getter
@Setter
public class DrawerMenuOption extends SimpleMenuOption {
    private DrawerMenuItemEvent drawerMenuItemEvent;

    @Override
    public Icon buildMenuIcon(String path, float scale) {
        FlatSVGIcon icon = new FlatSVGIcon(path, scale);
        FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
        colorFilter.add(Color.decode("#969696"), Color.decode("#FAFAFA"), Color.decode("#969696"));
        icon.setColorFilter(colorFilter);
        return icon;
    }
}
