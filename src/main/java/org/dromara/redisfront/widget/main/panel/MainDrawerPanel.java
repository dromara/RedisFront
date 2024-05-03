package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.panel.drawer.Logo;
import org.dromara.redisfront.widget.main.panel.drawer.ThemesChange;
import org.jetbrains.annotations.NotNull;
import raven.drawer.component.DrawerPanel;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.MenuValidation;
import raven.drawer.component.menu.SimpleMenuOption;
import raven.drawer.component.menu.SimpleMenuStyle;
import raven.drawer.component.menu.data.Item;
import raven.drawer.component.menu.data.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

@Getter
public class MainDrawerPanel extends SimpleDrawerBuilder {

    private final MainWidget owner;

    public MainDrawerPanel(MainWidget owner) {
        this.owner = owner;
    }

    @Override
    public Component getFooter() {
        return new ThemesChange();
    }


    @Override
    public Component getHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        if(SystemInfo.isMacOS) {
            headerPanel.setBorder(new EmptyBorder(35, 15, 5, 15));
        }else {
            headerPanel.setBorder(new EmptyBorder(15, 15, 5, 15));
        }
        headerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        headerPanel.add(Logo.getInstance(),BorderLayout.CENTER);
        return headerPanel;
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        return new SimpleHeaderData();
    }

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData();
    }

    @Override
    public SimpleMenuOption getSimpleMenuOption() {

        MenuItem[] items = new MenuItem[]{
                new Item("127.0.0.1", "project.svg"),
                new Item("127.0.0.1", "project.svg"),
                new Item("127.0.0.1", "project.svg"),
                new Item("127.0.0.1", "project.svg"),
                new Item("127.0.0.1", "project.svg"),
        };

        SimpleMenuOption simpleMenuOption = getMenuOption();
        simpleMenuOption.setMenuStyle(new SimpleMenuStyle() {
            @Override
            public void styleMenuPanel(JPanel panel, int[] index) {
                // style submenu panel here
                panel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
            }

            @Override
            public void styleMenuItem(JButton menu, int[] index) {
                menu.putClientProperty(FlatClientProperties.STYLE, "[light]foreground:#f8fafc;" +
                        "[dark]foreground:@foreground");
            }

            @Override
            public void styleMenu(JComponent component) {
                component.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
            }

            @Override
            public void styleLabel(JLabel label) {
                label.putClientProperty(FlatClientProperties.STYLE, "[light]foreground:darken(#FAFAFA,15%);" +
                        "[dark]foreground:darken($Label.foreground,30%)");
            }
        });


        simpleMenuOption.setMenuValidation(new MenuValidation() {
            @Override
            public boolean menuValidation(int[] index) {
                if (index.length == 1) {
                    return index[0] != 3;
                } else if (index.length == 3) {
                    return index[0] != 1 || index[1] != 1 || index[2] != 4;
                }
                return true;
            }
        });

        simpleMenuOption.setMenus(items)
                .setBaseIconPath("icons")
                .setIconScale(0.45f);
        return simpleMenuOption;
    }

    private static @NotNull SimpleMenuOption getMenuOption() {
        SimpleMenuOption simpleMenuOption = new SimpleMenuOption() {
            @Override
            public Icon buildMenuIcon(String path, float scale) {
                FlatSVGIcon icon = new FlatSVGIcon(path, scale);
                FlatSVGIcon.ColorFilter colorFilter = new FlatSVGIcon.ColorFilter();
                colorFilter.add(Color.decode("#969696"), Color.decode("#FAFAFA"), Color.decode("#969696"));
                icon.setColorFilter(colorFilter);
                return icon;
            }
        };
        simpleMenuOption.addMenuEvent((action, index) -> System.out.println("Drawer menu selected " + Arrays.toString(index)));
        return simpleMenuOption;
    }

    @Override
    public void build(DrawerPanel drawerPanel) {
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");

    }

    @Override
    public int getDrawerWidth() {
        return 250;
    }
}
