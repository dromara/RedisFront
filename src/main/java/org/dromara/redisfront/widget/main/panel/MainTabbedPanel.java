package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.ui.component.DataSplitPanel;
import org.dromara.redisfront.ui.component.RedisTerminal;
import org.dromara.redisfront.ui.form.fragment.DataChartsForm;
import org.dromara.redisfront.ui.form.fragment.PubSubForm;
import org.dromara.redisfront.widget.main.MainWidget;
import org.dromara.redisfront.widget.main.action.DrawerAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainTabbedPanel extends JTabbedPane {

    private final MainWidget owner;


    public MainTabbedPanel(MainWidget owner) {
        this.owner = owner;
        this.initComponents();
    }


    private void initComponents() {
        this.initMainTabbedUI();
        this.initMainTabbedItem();
    }

    private void initMainTabbedUI() {
        //tabbedPane init
        this.setTabPlacement(JTabbedPane.LEFT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 70);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);

        //tab 切换事件
        this.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            System.out.println(tabbedPane.getSelectedIndex());
        });


        //配置按钮
        FlatToolBar settingToolBar = new FlatToolBar();
        settingToolBar.setLayout(new MigLayout(new LC().bottomToTop()));
        settingToolBar.add(new JButton(UI.SETTING_ICON_40x40));
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, settingToolBar);
    }

    private void initMainTabbedItem() {
        //主窗口
        this.addTab("主页", UI.CONTENT_TAB_DATA_ICON, new JPanel());
        //命令窗口
        this.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, new JPanel());
        this.addTab("订阅", UI.MQ_ICON, new JPanel());
        //数据窗口
        this.addTab("数据", UI.CONTENT_TAB_INFO_ICON, new JPanel());
    }


}
