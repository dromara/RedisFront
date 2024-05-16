package org.dromara.redisfront.widget.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.commons.constant.Res;
import org.dromara.redisfront.widget.MainComponent;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.action.DrawerAction;
import org.dromara.redisfront.widget.ui.*;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jetbrains.annotations.NotNull;
import raven.drawer.component.DrawerPanel;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooter;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.MenuEvent;
import raven.drawer.component.menu.MenuValidation;
import raven.drawer.component.menu.SimpleMenuOption;
import raven.drawer.component.menu.SimpleMenuStyle;
import raven.drawer.component.menu.data.Item;
import raven.drawer.component.menu.data.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Getter
public class MainLeftDrawerPanel extends SimpleDrawerBuilder {

    MenuItem[] items = new MenuItem[]{
            new Item("阿里云服务器", "folder.svg")
                    .subMenu(new Item("2.127.231.79", "link.svg"))
                    .subMenu(new Item("47.22.5.98", "link.svg"))
                    .subMenu(new Item("5.48.77.19", "link.svg")),
            new Item("127.0.0.1", "link.svg"),
            new Item("阿里云主机", "link.svg"),
    };

    private final MainWidget owner;
    private final MenuEvent menuEvent;
    private final DrawerAction drawerAction;
    private final DrawerMenuItemEvent drawerMenuItemEvent;


    public DrawerPanel buildDrawerPanel() {
        return new DrawerPanel(this);
    }

    public MainLeftDrawerPanel(MainWidget owner, MenuEvent menuEvent, DrawerAction drawerAction, DrawerMenuItemEvent drawerMenuItemEvent) {
        this.owner = owner;
        this.menuEvent = menuEvent;
        this.drawerAction = drawerAction;
        this.drawerMenuItemEvent = drawerMenuItemEvent;
        initializeUI();
    }

    private void initializeUI() {
        var simpleMenuOption = this.getSimpleMenuOption();
        if (simpleMenuOption instanceof DrawerMenuOption drawerMenuOption) {
            drawerMenuOption.setDrawerMenuItemEvent(drawerMenuItemEvent);
        }
        simpleMenuOption.addMenuEvent(menuEvent);
        this.menu = new DrawerMenu(simpleMenuOption);
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new JMenuItem("导入"));
        popupMenu.add(new JMenuItem("导出"));
        menu.setComponentPopupMenu(popupMenu);
        this.menuScroll = createScroll(menu);
        this.footer = new SimpleFooter(getSimpleFooterData());
    }

    @Override
    public Component getFooter() {
        var footerPanel = new JPanel();
        footerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        footerPanel.setLayout(new MigLayout("al center", "[fill,fill]", "fill"));
        footerPanel.add(new ThemesChange());
        return footerPanel;
    }


    @Override
    public Component getHeader() {
        var headerPanel = new JPanel();
        headerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        headerPanel.setLayout(new BorderLayout());
        if (SystemInfo.isMacOS) {
            headerPanel.setBorder(new EmptyBorder(35, 15, 5, 15));
        } else {
            headerPanel.setBorder(new EmptyBorder(15, 15, 5, 15));
        }
        headerPanel.add(CombLogoPanel.getInstance());
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
    public Component getMenu() {
        JTree tree = createConnectTree();
        tree.setCellRenderer(new DefaultXTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                this.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
                if (!leaf) {
                    this.setIcon(Res.FOLDER_ICON_14x14);
                } else {
                    this.setIcon(Res.LINK_ICON_14x14);
                }
                return c;
            }

            {
                setTextNonSelectionColor(Color.WHITE);
            }
        });

        JScrollPane scrollPane = createScroll(tree);
        scrollPane.setBorder(new EmptyBorder(0, 10, 0, 10));
        return scrollPane;
    }

    private static @NotNull JTree createConnectTree() {
        JTree tree = new JTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setDragEnabled(true);
        tree.putClientProperty(FlatClientProperties.STYLE,
                "selectionArc:10;" +
                        "rowHeight:25;" +
                        "background:$RedisFront.main.background;" +
                        "foreground:#ffffff;" +
                        "[light]selectionBackground:darken(#FAFAFA,15%);" +
                        "[light]selectionForeground:darken($Label.foreground,50%);" +
                        "[dark]selectionBackground:darken($Label.foreground,50%);" +
                        "showCellFocusIndicator:false;"

        );
        return tree;
    }

    @Override
    public SimpleMenuOption getSimpleMenuOption() {
        var simpleMenuOption = new DrawerMenuOption().setMenus(items)
                .setBaseIconPath("icons")
                .setIconScale(0.08f);
        simpleMenuOption.setMenuStyle(new SimpleMenuStyle() {
            @Override
            public void styleMenuPanel(JPanel panel, int[] index) {
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
        return simpleMenuOption;
    }


    @Override
    public void build(DrawerPanel drawerPanel) {
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
    }

    @Override
    public int getDrawerWidth() {
        return MainComponent.DEFAULT_DRAWER_WIDTH;
    }
}
