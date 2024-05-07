package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.action.DrawerAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainHeaderBar extends JPanel {
    private final MainWidget owner;
    private final DrawerAction action;
    private final MainTabbedPanel tabbedPane;
    private FlatToolBar toolBar;
    private Boolean drawerState = false;

    public MainHeaderBar(MainWidget owner, DrawerAction action, MainTabbedPanel tabbedPane) {
        this.owner = owner;
        this.action = action;
        this.tabbedPane = tabbedPane;

        setLayout(new BorderLayout());

        toolBar = new FlatToolBar();

        if (SystemInfo.isMacOS) {
            toolBar.setMargin(new Insets(2, 5, 0, 0));
        } else {
            toolBar.setMargin(new Insets(2, 6, 0, 0));
        }

        var closeDrawerBtn = new JButton(UI.DRAWER_SHOW_OR_CLOSE_ICON);
        closeDrawerBtn.addActionListener(action);
        toolBar.add(closeDrawerBtn);
        action.setBeforeProcess(state -> closeDrawerBtn.setVisible(false));
        action.setAfterProcess(state -> {
            drawerState = state;
            if (SystemInfo.isMacOS) {
                if (owner.isFullScreen()) {
                    if (drawerState) {
                        toolBar.setMargin(new Insets(2, 15, 0, 0));
                    } else {
                        toolBar.setMargin(new Insets(2, 5, 0, 0));
                    }
                } else {
                    if (drawerState) {
                        toolBar.setMargin(new Insets(2, 73, 0, 0));
                        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 22, 10, 22));
                    } else {
                        toolBar.setMargin(new Insets(2, 6, 0, 0));
                        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 10, 10, 10));
                    }
                }

            } else {
                toolBar.setMargin(new Insets(2, 6, 0, 0));
                tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 10, 10, 10));
            }
            closeDrawerBtn.setVisible(true);
        });

        if (SystemInfo.isMacOS) {
            this.setPreferredSize(new Dimension(-1, 39));
            this.setBorder(new EmptyBorder(3, 0, 0, 0));
        } else {
            this.setPreferredSize(new Dimension(-1, 33));
        }

        this.add(toolBar, BorderLayout.WEST);
        JLabel host = new JLabel(UI.REDIS_ICON_14x14);
        host.setText("阿里云REDIS (127.0.0.1) - 集群模式");
        host.setVerticalAlignment(SwingConstants.CENTER);
        host.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(host, BorderLayout.CENTER);
    }


    private void initComponentListener() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
//                if(owner.isFullScreen()) {
//                    if(drawerState) {
//                        toolBar.setMargin(new Insets(2, 15, 0, 0));
//                    }else {
//                        toolBar.setMargin(new Insets(2, 5, 0, 0));
//                    }
//                }else{
//                    if(drawerState) {
//                        toolBar.setMargin(new Insets(2, 73, 0, 0));
//                    }else {
//
//                        toolBar.setMargin(new Insets(2, 10, 0, 0));
//                    }
//
//                }
            }
        });
    }
}
