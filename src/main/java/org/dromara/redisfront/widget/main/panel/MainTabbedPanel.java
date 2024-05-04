package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.quickswing.constant.OS;
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
    private JTabbedPane contentPanel;

    public MainTabbedPanel(DrawerAction action, MainWidget owner) {
        this.owner = owner;
        this.action = action;
        this.setLayout(new BorderLayout());
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (owner.getOS() == OS.WINDOWS||owner.getOS() == OS.MAC_OS_X) {
                    if ((owner.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.NORMAL) {
                        System.out.println("窗口处于最大化状态");
                    } else {
                        System.out.println("窗口未处于最大化状态");
                    }
                }
            }
        });
        this.initComponents();

    }

    private void initComponents() {
        this.initMainTabbedUI();
        this.initTopBar();
        this.initMainTabbedItem();
        this.initMainToolbar();
    }

    private void initTopBar() {
        var toolBar = new FlatToolBar();
        var closeDrawerBtn = new JButton(UI.DRAWER_SHOW_OR_CLOSE_ICON);
        closeDrawerBtn.addActionListener(action);
        action.setBeforeProcess(state -> closeDrawerBtn.setVisible(false));
        action.setAfterProcess(state -> {
            if (SystemInfo.isMacOS && state && owner.getExtendedState() != Frame.MAXIMIZED_BOTH) {
                toolBar.setMargin(new Insets(0, 65, 0, 0));
            } else {
                toolBar.setMargin(new Insets(0, 0, 0, 0));
            }
            closeDrawerBtn.setVisible(true);
        });
        toolBar.add(closeDrawerBtn);

        JPanel topBarPanel = new JPanel(new BorderLayout());
        if(SystemInfo.isMacOS) {
            topBarPanel.setPreferredSize(new Dimension(-1, 39));
            topBarPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
        }else {
            topBarPanel.setPreferredSize(new Dimension(-1, 33));
        }
        topBarPanel.add(toolBar, BorderLayout.WEST);
        JLabel host = new JLabel("阿里云REDIS(127.0.0.1) - 集群模式");
        host.setVerticalAlignment(SwingConstants.CENTER);
        host.setHorizontalAlignment(SwingConstants.CENTER);
        topBarPanel.add(host, BorderLayout.CENTER);
        topBarPanel.add(new Spacer(), BorderLayout.EAST);
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
        info.setText("RedisFront " + Const.APP_VERSION);
        info.setToolTipText("Version ".concat(Const.APP_VERSION));
        info.setIcon(UI.REDIS_ICON_14x14);
        rightToolBar.add(info);
        horizontalBox.add(rightToolBar);
        this.add(horizontalBox, BorderLayout.SOUTH);
    }

    private void initMainTabbedUI() {
        contentPanel = new JTabbedPane();
        contentPanel.setTabPlacement(JTabbedPane.LEFT);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_FILL);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_CONTENT_SEPARATOR, true);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ROTATION,FlatClientProperties.TABBED_PANE_TAB_ROTATION_NONE);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE,FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);

        Background background = new Background();
        background.setSize(new Dimension(500,600));
        background.add(contentPanel,BorderLayout.CENTER);
        this.add(contentPanel, BorderLayout.CENTER);


        {
            var leftToolBar = new FlatToolBar();
            leftToolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
            var cupInfo = new JLabel("0.5%", UI.CONTENT_TAB_CPU_ICON, SwingConstants.CENTER);
            leftToolBar.add(cupInfo);
//            contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);

            var rightToolBar = new FlatToolBar();
            rightToolBar.setLayout(new FlowLayout(FlowLayout.CENTER));
            var memoryInfo = new JLabel("120MB", UI.CONTENT_TAB_MEMORY_ICON, SwingConstants.CENTER);
            rightToolBar.add(memoryInfo);

//            contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);
        }


    }

    private void initMainTabbedItem() {
        //主窗口
        contentPanel.addTab("主页", UI.CONTENT_TAB_DATA_ICON, new JPanel());
        //命令窗口
        contentPanel.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, new JPanel());
        contentPanel.addTab("订阅", UI.MQ_ICON, new JPanel());
        //数据窗口
        contentPanel.addTab("数据", UI.CONTENT_TAB_INFO_ICON, new JPanel());
    }


}
