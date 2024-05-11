package org.dromara.redisfront.widget;

import cn.hutool.core.util.ArrayUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.widget.components.action.DrawerAction;
import org.dromara.redisfront.widget.components.MainLeftDrawerPanel;
import org.dromara.redisfront.widget.components.MainRightTabbedPanel;
import org.dromara.redisfront.widget.ui.DefaultNonePanel;
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
    private DrawerAction drawerAction;

    private final MenuEvent menuEvent = (source, index) -> {
        Component[] components = mainContentPane.getComponents();
        if (ArrayUtil.isNotEmpty(components)) {
            Optional<Component> first = Arrays.stream(components).findFirst();
            if (first.isPresent()) {
                if (first.get() instanceof MainRightTabbedPanel mainRightTabbedPanel) {
                    //todo add tab
                    System.out.println("JTabbedPane " + first.get());
                } else {
                    mainContentPane.removeAll();
                    MainRightTabbedPanel mainRightTabbedPanel = createMainTabbedPanel(drawerAction);
                    mainContentPane.add(mainRightTabbedPanel, BorderLayout.CENTER);
                    FlatLaf.updateUI();
                }
            }
        }
    };

    private MainRightTabbedPanel createMainTabbedPanel(DrawerAction drawerAction) {
        MainRightTabbedPanel mainRightTabbedPanel = new MainRightTabbedPanel(drawerAction, owner);
        mainRightTabbedPanel.setTabCloseProcess(count -> {
            if (count == 0) {
                if (!drawerAction.isDrawerOpen()) {
                    drawerAction.handleAction(null);
                }
                mainContentPane.removeAll();
                mainContentPane.add(DefaultNonePanel.getInstance(), BorderLayout.CENTER);
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

    public MainComponent(MainWidget owner) {
        this.owner = owner;
        this.setLayout(new BorderLayout());
        this.initComponents();
    }

    private void initComponents() {
        this.drawerAction = new DrawerAction(owner, process);
        JPanel parentPanel = new JPanel(new BorderLayout());

        this.mainContentPane = new JPanel();
        this.mainContentPane.setLayout(new BorderLayout());
        this.mainContentPane.add(DefaultNonePanel.getInstance(), BorderLayout.CENTER);
        parentPanel.add(mainContentPane, BorderLayout.CENTER);

        this.mainDrawerPanel = new MainLeftDrawerPanel(owner, menuEvent, drawerAction).buildDrawerPanel();
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
