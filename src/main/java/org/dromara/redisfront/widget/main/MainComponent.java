package org.dromara.redisfront.widget.main;

import com.formdev.flatlaf.FlatClientProperties;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.ui.form.MainNoneForm;
import org.dromara.redisfront.widget.main.action.DrawerAction;
import org.dromara.redisfront.widget.main.panel.MainDrawerBuilder;
import org.jetbrains.annotations.NotNull;
import raven.drawer.component.DrawerPanel;

import javax.swing.*;
import java.awt.*;


public class MainComponent extends Background {

    private final MainWidget owner;
    private JPanel drawerPanel;
    private JPanel mainContentPane;

    public MainComponent(MainWidget owner) {
        this.owner = owner;
        this.setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JPanel parentPanel = new JPanel(new BorderLayout());
        this.add(parentPanel, BorderLayout.CENTER);

        mainContentPane = new JPanel();
        mainContentPane.setLayout(new BorderLayout());
        mainContentPane.add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER);
        parentPanel.add(mainContentPane, BorderLayout.CENTER);

        drawerPanel = new DrawerPanel(getMainDrawerBuilder());
        drawerPanel.setMinimumSize(new Dimension(250, -1));
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        parentPanel.add(drawerPanel, BorderLayout.WEST);


    }

    private @NotNull MainDrawerBuilder getMainDrawerBuilder() {
        MainDrawerBuilder mainDrawerBuilder = new MainDrawerBuilder(owner, mainContentPane);
        DrawerAction drawerAction = new DrawerAction(owner);
        drawerAction.setProcess((fraction, drawerOpen) -> {
            int width;
            if (drawerOpen) {
                width = (int) (250 - 250 * fraction);
            } else {
                width = (int) (250 * fraction);
            }
            drawerPanel.setPreferredSize(new Dimension(width, -1));
            drawerPanel.updateUI();
        });
        mainDrawerBuilder.setDrawerAction(drawerAction);
        return mainDrawerBuilder;
    }


    @Override
    public void updateUI() {
        super.updateUI();
        if (drawerPanel != null) {
            drawerPanel.updateUI();
            drawerPanel.revalidate();
        }
    }

}
