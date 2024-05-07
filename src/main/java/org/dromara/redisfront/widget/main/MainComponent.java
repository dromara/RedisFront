package org.dromara.redisfront.widget.main;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.bouncycastle.math.raw.Mod;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.widget.main.action.DrawerAction;
import org.dromara.redisfront.widget.main.panel.MainDrawerPanel;
import org.dromara.redisfront.widget.main.panel.MainFooterBar;
import org.dromara.redisfront.widget.main.panel.MainHeaderBar;
import org.dromara.redisfront.widget.main.panel.MainTabbedPanel;
import raven.drawer.component.DrawerPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class MainComponent extends Background {

    private final MainWidget owner;
    private DrawerPanel drawerPanel;
    public MainComponent(MainWidget owner) {
        this.owner = owner;
        this.setLayout(new MigLayout(new LC().noCache()));
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        drawerPanel = new DrawerPanel(new MainDrawerPanel(owner));
        drawerPanel.setMinimumSize(new Dimension(250, -1));
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        mainPanel.add(drawerPanel, BorderLayout.WEST);

        MainTabbedPanel mainTabbedPanel = new MainTabbedPanel(owner);
        mainPanel.add(mainTabbedPanel, BorderLayout.CENTER);
        MainHeaderBar mainHeaderBar = new MainHeaderBar(owner,new DrawerAction(owner, drawerPanel),mainTabbedPanel);
        mainPanel.add(mainHeaderBar, BorderLayout.NORTH);
        MainFooterBar mainFooterBar = new MainFooterBar(owner);
        mainPanel.add(mainFooterBar, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void updateUI(){
        super.updateUI();
        if(drawerPanel!=null) {
            drawerPanel.updateUI();
            drawerPanel.revalidate();
        }
    }

}
