package org.dromara.redisfront.ui.widget.main.fragment;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.RedisFrontEventListener;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.components.info.RedisInfoPanel;
import org.dromara.redisfront.ui.dialog.SettingDialog;
import org.dromara.redisfront.ui.event.DrawerChangeEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.PageScaffold;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.IndexPageView;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.pubsub.PubSubPageView;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.report.ReportPageView;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.terminal.TerminalPageView;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class MainTabView extends JTabbedPane {
    private final RedisFrontWidget owner;
    private final RedisFrontContext context;
    private final RedisFrontEventListener eventListener;
    private final RedisConnectContext redisConnectContext;

    public MainTabView(RedisFrontWidget owner, RedisConnectContext redisConnectContext) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.eventListener = owner.getEventListener();
        this.redisConnectContext = redisConnectContext;
        initializeUI();
        //tab 切换事件
        this.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            if (tabbedPane.getSelectedComponent() instanceof PageScaffold pageScaffold) {
                pageScaffold.onChange();
            }
        });
        PageScaffold pageScaffold = new PageScaffold(new IndexPageView(redisConnectContext, owner));
        //主窗口
        this.addTab(owner.$tr("MainTabView.pageScaffold.home"), Icons.CONTENT_TAB_DATA_ICON, pageScaffold);
        //命令窗口
        pageScaffold = new PageScaffold(new TerminalPageView(redisConnectContext, owner));
        this.addTab(owner.$tr("MainTabView.pageScaffold.cmd"), Icons.CONTENT_TAB_COMMAND_ICON, pageScaffold);
        //订阅窗口
        pageScaffold = new PageScaffold(new PubSubPageView(redisConnectContext, owner));
        this.addTab(owner.$tr("MainTabView.pageScaffold.sub"), Icons.MQ_ICON, pageScaffold);
        //数据窗口
        pageScaffold = new PageScaffold(new ReportPageView(redisConnectContext, owner));
        this.addTab(owner.$tr("MainTabView.pageScaffold.chart"), Icons.CONTENT_TAB_INFO_ICON, pageScaffold);

        this.eventListener.bind(redisConnectContext.getId(), DrawerChangeEvent.class, qsEvent -> {
            if (qsEvent instanceof DrawerChangeEvent drawerChangeEvent) {
                Object message = drawerChangeEvent.getMessage();
                if (message instanceof Insets insets) {
                    RedisFrontUtils.runEDT(() -> {
                        putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, insets);
                        FlatLaf.updateUI();
                    });
                }
            }
        });
    }

    private void initializeUI() {
        String fontSize = owner.$tr("MainTabView.pageScaffold.fontSize");
        Font font = getFont();
        setFont(font.deriveFont(Float.parseFloat(fontSize)));
        this.setTabPlacement(JTabbedPane.LEFT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 70);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        FlatToolBar settingToolBar = new FlatToolBar();
        settingToolBar.setLayout(new MigLayout(new LC().align("center", "bottom")));
        JButton button = new JButton(Icons.REDIS_INFO_ICON_24x24);
        button.setToolTipText("Redis Info");
        button.addActionListener(_ -> {
            JDialog infoDialog = new JDialog(owner, redisConnectContext.getHost());
            infoDialog.setContentPane(new RedisInfoPanel(redisConnectContext));
            infoDialog.setSize(800, 600);
            infoDialog.setLocationRelativeTo(null);
            infoDialog.setVisible(true);
        });
        settingToolBar.add(button);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, settingToolBar);
    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        super.setUI(new BoldTitleTabbedPaneUI());
    }
}
