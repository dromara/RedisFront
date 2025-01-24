package org.dromara.redisfront.ui.widget;

import cn.hutool.core.util.ArrayUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.ui.support.NonePanel;
import org.dromara.redisfront.ui.support.extend.DrawerAnimationAction;
import org.dromara.redisfront.ui.support.extend.DrawerMenuItemEvent;
import org.dromara.redisfront.ui.widget.left.MainLeftComponent;
import org.dromara.redisfront.ui.widget.right.MainRightComponent;
import raven.drawer.component.menu.MenuEvent;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;


public class MainComponent extends Background {

    public static final int DEFAULT_DRAWER_WIDTH = 250;

    private final MainWidget owner;
    private JPanel mainDrawerPanel;
    private JPanel mainContentPane;
    private DrawerAnimationAction drawerAnimationAction;

    private final MenuEvent menuEvent = (_, _) -> {
        Component[] components = mainContentPane.getComponents();
        if (ArrayUtil.isNotEmpty(components)) {
            Optional<Component> first = Arrays.stream(components).findFirst();
            if (first.isPresent()) {
                if (first.get() instanceof MainRightComponent mainRightTabbedPanel) {
                    //todo add tab
                    System.out.println("JTabbedPane " + first.get());
                } else {
                    mainContentPane.removeAll();
                    MainRightComponent mainRightTabbedPanel = createMainTabbedPanel(drawerAnimationAction);
                    mainContentPane.add(mainRightTabbedPanel, BorderLayout.CENTER);
                    FlatLaf.updateUI();
                }
            }
        }
    };

    private MainRightComponent createMainTabbedPanel(DrawerAnimationAction drawerAnimationAction) {
        MainRightComponent mainRightTabbedPanel = new MainRightComponent(drawerAnimationAction, owner);
        mainRightTabbedPanel.setTabCloseProcess(count -> {
            if (count == 0) {
                if (!drawerAnimationAction.isDrawerOpen()) {
                    drawerAnimationAction.handleAction(null);
                }
                mainContentPane.removeAll();
                mainContentPane.add(NonePanel.getInstance(), BorderLayout.CENTER);
                FlatLaf.updateUI();
            }
        });
        return mainRightTabbedPanel;
    }

    private final BiConsumer<Double, Boolean> process = (fraction, drawerOpen) -> {
        int width = getDrawerWidth(fraction, drawerOpen);
        this.mainDrawerPanel.setPreferredSize(new Dimension(width, -1));
        this.mainDrawerPanel.updateUI();
    };

    private final DrawerMenuItemEvent drawerMenuItemEvent = (key, index) -> {
        System.out.println("drawerMenuItemEvent" + " key:" + key);
        System.out.println("drawerMenuItemEvent" + " index:" + Arrays.toString(index));
    };

    public MainComponent(MainWidget owner) {
        this.owner = owner;
        this.setLayout(new BorderLayout());
        this.initComponents();
    }

    private void initComponents() {
        this.drawerAnimationAction = new DrawerAnimationAction(owner, process);
        JPanel parentPanel = new JPanel(new BorderLayout());
        this.mainContentPane = new JPanel();
        this.mainContentPane.setLayout(new BorderLayout());
        this.mainContentPane.add(NonePanel.getInstance(), BorderLayout.CENTER);
        parentPanel.add(mainContentPane, BorderLayout.CENTER);
        this.mainDrawerPanel = new MainLeftComponent(owner, menuEvent, drawerAnimationAction, drawerMenuItemEvent).buildDrawerPanel();
        this.mainDrawerPanel.setMinimumSize(new Dimension(250, -1));
        this.mainDrawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        parentPanel.add(mainDrawerPanel, BorderLayout.WEST);
        this.add(parentPanel, BorderLayout.CENTER);
    }

    private static int getDrawerWidth(Double fraction, Boolean drawerOpen) {
        int width;
        if (drawerOpen) {
            width = (int) (DEFAULT_DRAWER_WIDTH - DEFAULT_DRAWER_WIDTH * fraction);
        } else {
            width = (int) (DEFAULT_DRAWER_WIDTH * fraction);
        }
        return width;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (mainDrawerPanel != null) {
            mainDrawerPanel.updateUI();
        }
    }

}
