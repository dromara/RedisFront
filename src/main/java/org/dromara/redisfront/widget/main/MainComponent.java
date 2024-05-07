package org.dromara.redisfront.widget.main;

import com.formdev.flatlaf.FlatClientProperties;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.widget.main.action.DrawerAction;
import org.dromara.redisfront.widget.main.panel.MainDrawerPanel;
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
        this.setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        drawerPanel = new DrawerPanel(new MainDrawerPanel(owner));
        drawerPanel.setMinimumSize(new Dimension(250, -1));
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        mainPanel.add(drawerPanel, BorderLayout.WEST);
        MainTabbedPanel mainTabbedPanel = new MainTabbedPanel(new DrawerAction(owner, drawerPanel),owner);
        mainPanel.add(mainTabbedPanel, BorderLayout.CENTER);
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
