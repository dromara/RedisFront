package org.dromara.redisfront.widget.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.widget.MainComponent;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.action.DrawerAnimationAction;
import org.dromara.redisfront.widget.components.extend.DefaultLogoPanel;
import org.dromara.redisfront.widget.components.extend.DrawerMenuItemEvent;
import org.dromara.redisfront.widget.components.extend.ThemesChangePanel;
import raven.drawer.component.DrawerPanel;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.MenuEvent;
import raven.drawer.component.menu.SimpleMenuOption;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Getter
@Slf4j
public class MainLeftDrawerPanel extends SimpleDrawerBuilder {

    private final MainWidget owner;
    private final DrawerAnimationAction drawerAnimationAction;
    private final DrawerMenuItemEvent drawerMenuItemEvent;



    public DrawerPanel buildDrawerPanel() {
        return new DrawerPanel(this) {
            @Override
            public void updateUI() {
                super.updateUI();

            }
        };
    }

    public MainLeftDrawerPanel(MainWidget owner, MenuEvent menuEvent, DrawerAnimationAction drawerAnimationAction, DrawerMenuItemEvent drawerMenuItemEvent) {
        this.owner = owner;
        this.drawerAnimationAction = drawerAnimationAction;
        this.drawerMenuItemEvent = drawerMenuItemEvent;
        initializeUI();
    }

    private void initializeUI() {
        var simpleMenuOption = this.getSimpleMenuOption();
    }

    @Override
    public Component getFooter() {
        var footerPanel = new JPanel();
        footerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        footerPanel.setLayout(new MigLayout("al center", "[fill,fill]", "fill"));
        footerPanel.add(new ThemesChangePanel());
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
        headerPanel.add(DefaultLogoPanel.getInstance());
        return headerPanel;
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        return new SimpleHeaderData();
    }

    @Override
    public SimpleMenuOption getSimpleMenuOption() {
        return new SimpleMenuOption();
    }

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData();
    }

    @Override
    public Component getMenu() {
        JTree tree = new MainLeftConnectTree(owner);
        JScrollPane scrollPane = createScroll(tree);
        scrollPane.setBorder(new EmptyBorder(0, 10, 0, 10));
        return scrollPane;
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
