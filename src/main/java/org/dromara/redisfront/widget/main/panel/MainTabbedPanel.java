package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.util.SystemInfo;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.action.DrawerAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
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
            }
        });
    }

    private void initComponents() {
        this.initMainTabbedUI();
        this.initTopBar();
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
                        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 22, 10, 22));
                    } else {
                        toolBar.setMargin(new Insets(2, 6, 0, 0));
                        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 10, 10, 10));
                    }
                }

            } else {
                toolBar.setMargin(new Insets(2, 3, 0, 0));
                tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(10, 10, 10, 10));
            }
            closeDrawerBtn.setVisible(true);
        });

        JPanel topBarPanel = new JPanel(new BorderLayout());
        if (SystemInfo.isMacOS) {
            topBarPanel.setPreferredSize(new Dimension(-1, 39));
            topBarPanel.setBorder(new EmptyBorder(3, 0, 0, 0));
        } else {
            topBarPanel.setPreferredSize(new Dimension(-1, 33));
        }

        topBarPanel.add(toolBar, BorderLayout.WEST);
        JLabel title = new JLabel(UI.REDIS_ICON_14x14);
        title.setText("阿里云REDIS (127.0.0.1) - 集群模式");
        title.setVerticalAlignment(SwingConstants.CENTER);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        topBarPanel.add(title, BorderLayout.CENTER);
        topBarPanel.add(new JSeparator(), BorderLayout.SOUTH);
        this.add(topBarPanel, BorderLayout.NORTH);
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

    final FlatTabbedPaneUI flatTabbedPaneUI = new FlatTabbedPaneUI(){
        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            if(isSelected){
                font = font.deriveFont(Font.BOLD);
            }
            super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
        }
    };

    private void initMainTabbedUI() {
        //tabbedPane init
        tabbedPane = new JTabbedPane();
        tabbedPane.setUI(flatTabbedPaneUI);
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 70);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);

        FlatToolBar settingToolBar = new FlatToolBar();
        settingToolBar.setLayout(new MigLayout(new LC().bottomToTop()));
        settingToolBar.add(new JButton(UI.SETTING_ICON_40x40));
        tabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, settingToolBar);

        //tab 切换事件
        tabbedPane.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            System.out.println(tabbedPane.getSelectedIndex());
        });

        this.add(tabbedPane, BorderLayout.CENTER);
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
