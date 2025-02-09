package org.dromara.redisfront.ui.widget.sidebar;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.ui.widget.RedisFrontComponent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.dromara.redisfront.ui.widget.handler.ConnectHandler;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerAnimationAction;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerMenuItemEvent;
import org.dromara.redisfront.ui.widget.sidebar.panel.LogoPanel;
import org.dromara.redisfront.ui.widget.sidebar.panel.ThemesChangePanel;
import org.dromara.redisfront.ui.widget.sidebar.tree.RedisConnectTree;
import org.jdesktop.swingx.JXTree;
import raven.drawer.component.DrawerPanel;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.SimpleMenuOption;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Getter
@Slf4j
public class SidebarComponent extends SimpleDrawerBuilder {

    private final RedisFrontWidget owner;
    private final DrawerAnimationAction drawerAnimationAction;
    private final RedisConnectTree tree;


    public DrawerPanel buildPanel() {
        DrawerPanel drawerPanel = new DrawerPanel(this);
        drawerPanel.setMinimumSize(new Dimension(250, -1));
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        return drawerPanel;
    }

    public SidebarComponent(RedisFrontWidget owner, ConnectHandler connectHandler, DrawerAnimationAction drawerAnimationAction) {
        this.owner = owner;
        this.tree = new RedisConnectTree(owner, connectHandler);
        this.drawerAnimationAction = drawerAnimationAction;
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
        headerPanel.add(LogoPanel.getInstance());
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
        return RedisFrontComponent.DEFAULT_DRAWER_WIDTH;
    }
}
