package org.dromara.redisfront.ui.widget.content.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.components.RedisTerminal;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.widget.MainWidget;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentTabPanel extends JTabbedPane {

    private final MainWidget owner;
    private final ConnectContext context;

    public ContentTabPanel(MainWidget owner, ConnectContext context) {
        this.owner = owner;
        this.context = context;
        this.setTabPlacement(JTabbedPane.LEFT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 70);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        FlatToolBar settingToolBar = new FlatToolBar();
        settingToolBar.setLayout(new MigLayout(new LC().align("center", "bottom")));
        settingToolBar.add(new JButton(Icons.SETTING_ICON_40x40));
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, settingToolBar);
        //tab 切换事件
        this.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            System.out.println(tabbedPane.getSelectedIndex());
        });
        //主窗口
        this.addTab("主页", Icons.CONTENT_TAB_DATA_ICON, new JPanel());
        ConnectContext connectContext = new ConnectContext();
        connectContext.setHost("127.0.0.1");
        connectContext.setPort(3306);
        connectContext.setDatabase(2);
        //命令窗口
        this.addTab("命令", Icons.CONTENT_TAB_COMMAND_ICON, new RedisTerminal(connectContext));
        this.addTab("订阅", Icons.MQ_ICON, new JPanel());
        //数据窗口
        this.addTab("数据", Icons.CONTENT_TAB_INFO_ICON, new JPanel());

    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        super.setUI(new BoldTitleTabbedPaneUI());
    }
}
