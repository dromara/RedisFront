package org.dromara.redisfront.widget.main.panel;

import cn.hutool.core.util.ArrayUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.ui.form.MainNoneForm;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.action.DrawerAction;
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
import java.util.Optional;

@Getter
public class MainDrawerBuilder extends SimpleDrawerBuilder {

    private final MainWidget owner;
    private final JPanel mainContentPane;

    @Setter
    private DrawerAction drawerAction;

    public MainDrawerBuilder(MainWidget owner, JPanel mainContentPane) {
        super();
        this.owner = owner;
        this.mainContentPane = mainContentPane;
    }

    @Override
    public Component getFooter() {
        JPanel footerPanel = new JPanel();
        footerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        footerPanel.setLayout(new MigLayout("al center", "[fill,fill]", "fill"));
        footerPanel.add(new ThemesChange());
        return footerPanel;
    }


    @Override
    public Component getHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        headerPanel.setLayout(new BorderLayout());
        if (SystemInfo.isMacOS) {
            headerPanel.setBorder(new EmptyBorder(35, 15, 5, 15));
        } else {
            headerPanel.setBorder(new EmptyBorder(15, 15, 5, 15));
        }
        headerPanel.add(Logo.getInstance());
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
                new Item("阿里云服务器", "folder.svg")
                        .subMenu(new Item("2.127.231.79", "link.svg"))
                        .subMenu(new Item("47.22.5.98", "link.svg"))
                        .subMenu(new Item("5.48.77.19", "link.svg")),
                new Item("127.0.0.1", "link.svg"),
                new Item("阿里云主机", "link.svg"),
        };

        SimpleMenuOption simpleMenuOption = getMenuOption();
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

        simpleMenuOption.setMenus(items)
                .setBaseIconPath("icons")
                .setIconScale(0.08f)
        ;
        return simpleMenuOption;
    }

    private SimpleMenuOption getMenuOption() {
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
        simpleMenuOption.addMenuEvent((source, index) -> {
            Component[] components = mainContentPane.getComponents();
            if (ArrayUtil.isNotEmpty(components)) {
                Optional<Component> first = Arrays.stream(components).findFirst();
                if (first.isPresent()) {
                    if (first.get() instanceof MainTabbedPanel) {
                        //todo add tab
                        System.out.println("JTabbedPane " + first.get());
                    } else {
                        mainContentPane.removeAll();
                        mainContentPane.add(getMainTabbedPanel(), BorderLayout.CENTER);
                        FlatLaf.updateUI();
                    }
                }
            }

        });
        return simpleMenuOption;
    }

    private @NotNull MainTabbedPanel getMainTabbedPanel() {
        MainTabbedPanel mainTabbedPanel = new MainTabbedPanel(drawerAction, owner);
        mainTabbedPanel.setTabCloseProcess(count -> {
            System.out.println(Thread.currentThread().getName());
            if (count == 0) {
                if(!drawerAction.isDrawerOpen()){
                    drawerAction.handleAction(null);
                }
                mainContentPane.removeAll();
                mainContentPane.add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER);
                FlatLaf.updateUI();
            }
        });
        return mainTabbedPanel;
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
