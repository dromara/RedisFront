package org.dromara.redisfront.ui.widget.content.view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.quickswing.QSApplication;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventListener;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.event.DrawerChangeEvent;
import org.dromara.redisfront.ui.widget.content.view.scaffold.PageScaffold;
import org.dromara.redisfront.ui.widget.content.view.scaffold.index.IndexPageView;
import org.dromara.redisfront.ui.widget.content.view.scaffold.pubsub.PubSubPageView;
import org.dromara.redisfront.ui.widget.content.ext.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.widget.MainWidget;
import org.dromara.redisfront.ui.widget.content.view.scaffold.terminal.TerminalPageView;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ContentTabView extends JTabbedPane {

    private final MainWidget owner;
    private final RedisFrontContext context;
    private final RedisConnectContext redisConnectContext;

    public ContentTabView(MainWidget owner, RedisConnectContext redisConnectContext) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.redisConnectContext = redisConnectContext;
        initializeUI();
        //tab 切换事件
        this.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            if(tabbedPane.getSelectedComponent() instanceof PageScaffold pageScaffold){
                pageScaffold.onChange();
            }
        });
        PageScaffold pageScaffold = new PageScaffold(new IndexPageView(redisConnectContext,owner));
        //主窗口
        this.addTab("主页", Icons.CONTENT_TAB_DATA_ICON, pageScaffold);
        //命令窗口
        pageScaffold = new PageScaffold(new TerminalPageView(redisConnectContext,owner));
        this.addTab("命令", Icons.CONTENT_TAB_COMMAND_ICON, pageScaffold);
        pageScaffold = new PageScaffold(new PubSubPageView(redisConnectContext,owner));
        this.addTab("订阅", Icons.MQ_ICON, pageScaffold);
        //数据窗口
        this.addTab("数据", Icons.CONTENT_TAB_INFO_ICON, new JPanel());
        context.getEventBus().subscribe(new QSEventListener<QSApplication>() {
            @Override
            protected void onEvent(QSEvent qsEvent) {
                if(qsEvent instanceof DrawerChangeEvent drawerChangeEvent){
                    Object message = drawerChangeEvent.getMessage();
                    if(message instanceof Insets insets){
                        SwingUtilities.invokeLater(()->{
                            putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, insets);
                            FlatLaf.updateUI();
                        });
                    }
                }
            }
        });

    }

    private void initializeUI() {
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
    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        super.setUI(new BoldTitleTabbedPaneUI());
    }
}
