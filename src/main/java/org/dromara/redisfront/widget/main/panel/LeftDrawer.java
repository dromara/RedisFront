package org.dromara.redisfront.widget.main.panel;

import cn.hutool.core.img.ImgUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import org.dromara.redisfront.commons.constant.UI;
import raven.drawer.component.DrawerPanel;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.*;
import raven.drawer.component.menu.data.Item;
import raven.drawer.component.menu.data.MenuItem;
import raven.swing.AvatarIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class LeftDrawer extends SimpleDrawerBuilder {

    private final JPanel headerPanel;

    public LeftDrawer() {
        this.headerPanel = new JPanel();
        this.headerPanel.setLayout(new BorderLayout());
        this.headerPanel.setBorder(new EmptyBorder(20, 15, 10, 15));
        this.headerPanel.add(Box.createVerticalGlue(), BorderLayout.NORTH); // 在上面添加额外的空间
        this.headerPanel.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
        this.headerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
    }

    @Override
    public Component getFooter() {
        return new ThemesChange();
    }

    @Override
    public Component getHeader() {
        AvatarIcon icon = new AvatarIcon(UI.REDIS_ICON, 55, 50, 80);
        icon.setBorder(5);
        Image image = ImgUtil.getImage(this.getClass().getResource("/images/RedisFront.png"));
        Image scaledInstance = image.getScaledInstance(155, 45, Image.SCALE_DEFAULT);
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BorderLayout());
        logoPanel.add(new JLabel(icon),BorderLayout.WEST);
        logoPanel.add(new JLabel(new ImageIcon(scaledInstance)),BorderLayout.CENTER);
        headerPanel.add(logoPanel, BorderLayout.CENTER);
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
                new Item.Label("MAIN"),
                new Item("Dashboard", "project.svg"),
        };

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
                    // Hide Calendar
                    if (index[0] == 3) {
                        return false;
                    }
                } else if (index.length == 3) {
                    //  Hide Read 4
                    if (index[0] == 1 && index[1] == 1 && index[2] == 4) {
                        return false;
                    }
                }
                return true;
            }
        });

        simpleMenuOption.setMenus(items)
                .setBaseIconPath("icons")
                .setIconScale(0.45f);
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
