package org.dromara.redisfront.widget.main;

import com.formdev.flatlaf.FlatClientProperties;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.widget.main.action.DrawerAction;
import org.dromara.redisfront.widget.main.panel.DrawerPanel;
import org.dromara.redisfront.widget.main.panel.MainTabbedPanel;

import javax.swing.*;
import java.awt.*;


public class MainComponent extends Background {

    private final MainWidget owner;
    private raven.drawer.component.DrawerPanel drawerPanel;
    public MainComponent(MainWidget owner) {
        this.owner = owner;
        this.setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        drawerPanel = new raven.drawer.component.DrawerPanel(new DrawerPanel());
        drawerPanel.setMinimumSize(new Dimension(250, -1));
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        mainPanel.add(drawerPanel, BorderLayout.WEST);
        JButton button = new JButton();
        button.addActionListener(new DrawerAction(owner, drawerPanel));
        mainPanel.add(MainTabbedPanel.newInstance(button), BorderLayout.CENTER);
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
