package org.dromara.redisfront.ui.widget.content.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.widget.content.view.scaffold.PageScaffold;
import org.dromara.redisfront.ui.widget.content.view.scaffold.index.IndexPageView;
import org.dromara.redisfront.ui.widget.content.view.scaffold.pubsub.PubSubPageView;
import org.dromara.redisfront.ui.widget.content.view.scaffold.terminal.RedisFrontTerminal;
import org.dromara.redisfront.ui.widget.content.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.widget.MainWidget;
import org.dromara.redisfront.ui.widget.content.view.scaffold.terminal.TerminalPageView;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentTabView extends JTabbedPane {

    private final MainWidget owner;
    private final ConnectContext connectContext;

    public ContentTabView(MainWidget owner, ConnectContext connectContext) {
        this.owner = owner;
        this.connectContext = connectContext;
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
            if(tabbedPane.getSelectedComponent() instanceof PageScaffold pageScaffold){
                pageScaffold.onChange();
            }
        });
        PageScaffold pageScaffold = new PageScaffold(new IndexPageView(connectContext,owner));
        //主窗口
        this.addTab("主页", Icons.CONTENT_TAB_DATA_ICON, pageScaffold);
        //命令窗口
        pageScaffold = new PageScaffold(new TerminalPageView(connectContext,owner));
        this.addTab("命令", Icons.CONTENT_TAB_COMMAND_ICON, pageScaffold);
        pageScaffold = new PageScaffold(new PubSubPageView(connectContext,owner));
        this.addTab("订阅", Icons.MQ_ICON, pageScaffold);
        //数据窗口
        this.addTab("数据", Icons.CONTENT_TAB_INFO_ICON, new JPanel());

    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        super.setUI(new BoldTitleTabbedPaneUI());
    }
}
