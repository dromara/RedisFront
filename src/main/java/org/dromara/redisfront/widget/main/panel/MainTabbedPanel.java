package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.ui.component.RedisTerminal;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.action.DrawerAction;
import org.dromara.redisfront.widget.main.ui.RedisFrontTabbedPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainTabbedPanel extends JPanel {

    private final MainWidget owner;
    private final DrawerAction action;
    private JTabbedPane topTabbedPane;
    private JTabbedPane contentTabbedPane;
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
            }
        });
    }

    private void initComponents() {
        this.initTopBar();
        this.initMainTabbedUI();
        this.initBottomBar();
        this.initMainTabbedItem();

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
            if (SystemInfo.isMacOS) {
                if (owner.isFullScreen()) {
                    if (state) {
                        toolBar.setMargin(new Insets(2, 15, 0, 0));
                    } else {
                        toolBar.setMargin(new Insets(2, 5, 0, 0));
                    }
                } else {
                    if (state) {
                        toolBar.setMargin(new Insets(2, 73, 0, 0));
                        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 22, 10, 22));
                    } else {
                        toolBar.setMargin(new Insets(2, 6, 0, 0));
                        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 10, 10, 10));
                    }
                }

            } else {
                toolBar.setMargin(new Insets(2, 3, 0, 0));
                contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 10, 10, 10));
            }
            closeDrawerBtn.setVisible(true);
        });

        //tabbedPane init
        topTabbedPane = new RedisFrontTabbedPane();
        topTabbedPane.setTabPlacement(JTabbedPane.TOP);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_EQUAL);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);

        FlatToolBar settingToolBar = new FlatToolBar();
        if (SystemInfo.isMacOS) {
            settingToolBar.setPreferredSize(new Dimension(-1, 39));
            settingToolBar.setBorder(new EmptyBorder(3, 0, 0, 0));
        } else {
            settingToolBar.setPreferredSize(new Dimension(-1, 33));
        }
        settingToolBar.setLayout(new MigLayout(new LC().align("center","bottom")));
        settingToolBar.add(new JButton(UI.SETTING_ICON_40x40));
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT,toolBar);
        this.add(topTabbedPane, BorderLayout.NORTH);
    }


    private void initBottomBar() {
        Box horizontalBox = Box.createVerticalBox();
        horizontalBox.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        horizontalBox.add(new JSeparator());
        var rightToolBar = new FlatToolBar();
        rightToolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        var version = new JLabel();
        version.setText(Const.APP_VERSION);
        RedisFrontContext context = (RedisFrontContext) owner.getContext();
        version.setToolTipText("Current Version " + context.version());
        version.setIcon(UI.REDIS_TEXT_80x16);
        rightToolBar.add(version);
        horizontalBox.add(rightToolBar);
        this.add(horizontalBox, BorderLayout.SOUTH);
    }

    private void initMainTabbedUI() {
        //tabbedPane init
        contentTabbedPane = new RedisFrontTabbedPane();
        contentTabbedPane.setTabPlacement(JTabbedPane.LEFT);
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 70);
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);

        FlatToolBar settingToolBar = new FlatToolBar();
        settingToolBar.setLayout(new MigLayout(new LC().align("center","bottom")));
        settingToolBar.add(new JButton(UI.SETTING_ICON_40x40));
        contentTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, settingToolBar);

        //tab 切换事件
        contentTabbedPane.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            System.out.println(tabbedPane.getSelectedIndex());
        });
        topTabbedPane.addTab("阿里云主机",UI.REDIS_ICON_14x14, new JLabel("测试"));
        topTabbedPane.addTab("192.168.1.1",UI.REDIS_ICON_14x14, new JPanel());
        topTabbedPane.addTab("127.0.0.1",UI.REDIS_ICON_14x14,contentTabbedPane);
        this.add(topTabbedPane, BorderLayout.CENTER);
    }

    private void initMainTabbedItem() {
        //主窗口
        contentTabbedPane.addTab("主页", UI.CONTENT_TAB_DATA_ICON, new JPanel());
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setHost("127.0.0.1");
        connectInfo.setPort(3306);
        connectInfo.setDatabase(2);
        //命令窗口
        contentTabbedPane.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, new RedisTerminal(connectInfo));
        contentTabbedPane.addTab("订阅", UI.MQ_ICON, new JPanel());
        //数据窗口
        contentTabbedPane.addTab("数据", UI.CONTENT_TAB_INFO_ICON, new JPanel());
    }


}
