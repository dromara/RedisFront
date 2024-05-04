package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.action.DrawerAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainTabbedPanel extends JPanel {

    private final MainWidget owner;
    private final DrawerAction action;
    private JTabbedPane tabbedPane;
    private FlatToolBar toolBar;
    private Boolean drawerState = false;
    public MainTabbedPanel(DrawerAction action, MainWidget owner) {
        this.owner = owner;
        this.action = action;
        this.setLayout(new BorderLayout());
        this.initComponentListener();
        this.initComponents();
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

    private void initComponents() {
        this.initMainTabbedUI();
        this.initTopBar();
        this.initMainTabbedItem();
        this.initMainToolbar();
    }



    private void initTopBar() {

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
                if(owner.isFullScreen()) {
                    if(drawerState) {
                        toolBar.setMargin(new Insets(2, 15, 0, 0));
                    }else {
                        toolBar.setMargin(new Insets(2, 5, 0, 0));
                    }
                }else{
                    if(drawerState) {
                        toolBar.setMargin(new Insets(2, 70, 0, 0));
                    }else {
                        toolBar.setMargin(new Insets(2, 15, 0, 0));
                    }
                }
                tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS,new Insets(10,22,10,22));
            } else {
                toolBar.setMargin(new Insets(2, 6, 0, 0));
                tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS,new Insets(10,10,10,10));
            }
            closeDrawerBtn.setVisible(true);
        });

        JPanel topBarPanel = new JPanel(new BorderLayout());
        if(SystemInfo.isMacOS) {
            topBarPanel.setPreferredSize(new Dimension(-1, 39));
            topBarPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
        }else {
            topBarPanel.setPreferredSize(new Dimension(-1, 33));
        }

        topBarPanel.add(toolBar, BorderLayout.WEST);
        JLabel host = new JLabel(UI.REDIS_ICON_14x14);
        host.setText("阿里云REDIS (127.0.0.1) - 集群模式");
        host.setVerticalAlignment(SwingConstants.CENTER);
        host.setHorizontalAlignment(SwingConstants.CENTER);
        topBarPanel.add(host, BorderLayout.CENTER);
        topBarPanel.add(new JSeparator(), BorderLayout.SOUTH);
        this.add(topBarPanel, BorderLayout.NORTH);
    }


    private void initMainToolbar() {
        Box horizontalBox = Box.createVerticalBox();
        horizontalBox.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        horizontalBox.add(new JSeparator());
        var rightToolBar = new FlatToolBar();
        rightToolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        var info = new JLabel();
        info.setText( Const.APP_VERSION);
        info.setToolTipText("Version ".concat(Const.APP_VERSION));
        info.setIcon(UI.REDIS_TEXT_80x16);
        rightToolBar.add(info);
        horizontalBox.add(rightToolBar);
        this.add(horizontalBox, BorderLayout.SOUTH);
    }

    private void initMainTabbedUI() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_FILL);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_CONTENT_SEPARATOR, true);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ROTATION,FlatClientProperties.TABBED_PANE_TAB_ROTATION_NONE);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE,FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT,70);
        Background background = new Background();
        background.setSize(new Dimension(500,600));
        background.add(tabbedPane,BorderLayout.CENTER);
        this.add(tabbedPane, BorderLayout.CENTER);


        {
            var leftToolBar = new FlatToolBar();
            leftToolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
            var cupInfo = new JLabel("0.5%", UI.CONTENT_TAB_CPU_ICON, SwingConstants.CENTER);
            leftToolBar.add(cupInfo);
//            tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, new JLabel(UI.REDIS_ICON_45x45));

            var rightToolBar = new FlatToolBar();
            rightToolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
            var memoryInfo = new JLabel("120MB", UI.CONTENT_TAB_MEMORY_ICON, SwingConstants.CENTER);
            rightToolBar.add(memoryInfo);

//            contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);
        }


    }

    private void initMainTabbedItem() {
        //主窗口
        tabbedPane.addTab("主页", UI.CONTENT_TAB_DATA_ICON, new JPanel());
        //命令窗口
        tabbedPane.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, new JPanel());
        tabbedPane.addTab("订阅", UI.MQ_ICON, new JPanel());
        //数据窗口
        tabbedPane.addTab("数据", UI.CONTENT_TAB_INFO_ICON, new JPanel());
    }


}
