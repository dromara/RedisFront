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
import org.dromara.redisfront.model.LogInfo;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.components.info.RedisInfoView;
import org.dromara.redisfront.ui.event.CommandExecuteEvent;
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
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = true)
public class MainTabView extends JTabbedPane {
    private final RedisFrontWidget owner;
    private final RedisFrontContext context;
    private final RedisFrontEventListener eventListener;
    private final RedisConnectContext redisConnectContext;
    private final RedisInfoView redisInfoView;
    private PageScaffold lastPageScaffold;
    private final IndexPageView indexPage;
    private final TerminalPageView terminalPage;
    private JDialog infoDialog;
    private boolean disposed;
    private final org.dromara.redisfront.ui.components.diagnostics.RedisDiagnosticsPanel diagnostics;

    public boolean confirmDiscardChanges() { return indexPage.confirmDiscardChanges(); }

    public void clearUp(){
        if (disposed) return;
        disposed = true;
        indexPage.disposeSearch();
        for (int i = 0; i < getTabCount(); i++) {
            Component tabComponentAt = getComponentAt(i);
            if (tabComponentAt instanceof PageScaffold pageScaffold) {
                pageScaffold.onClose();
            }
        }
        redisInfoView.stop();
        terminalPage.disposeTerminal();
        diagnostics.close();
        if (infoDialog != null) infoDialog.dispose();
    }

    public MainTabView(RedisFrontWidget owner, RedisConnectContext redisConnectContext) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.eventListener = owner.getEventListener();
        this.redisConnectContext = redisConnectContext;
        this.redisInfoView = new RedisInfoView(owner, redisConnectContext);
        this.initializeUI();
        //tab 切换事件
        this.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            if (lastPageScaffold != null && tabbedPane.getSelectedComponent() != lastPageScaffold) {
                lastPageScaffold.onClose();lastPageScaffold = null;
            }
            if (tabbedPane.getSelectedComponent() instanceof PageScaffold pageScaffold) {
                pageScaffold.onChange();
                lastPageScaffold = pageScaffold;
            }
        });
        indexPage = new IndexPageView(redisConnectContext, owner);
        terminalPage = new TerminalPageView(redisConnectContext, owner);
        PageScaffold pageScaffold = new PageScaffold(indexPage);
        //主窗口
        this.addTab(owner.$tr("MainTabView.pageScaffold.home"), Icons.CONTENT_TAB_DATA_ICON, pageScaffold);
        //命令窗口
        pageScaffold = new PageScaffold(terminalPage);
        this.addTab(owner.$tr("MainTabView.pageScaffold.cmd"), Icons.CONTENT_TAB_COMMAND_ICON, pageScaffold);
        //订阅窗口
        pageScaffold = new PageScaffold(new PubSubPageView(redisConnectContext, owner));
        this.addTab(owner.$tr("MainTabView.pageScaffold.sub"), Icons.MQ_ICON, pageScaffold);
        //数据窗口
        pageScaffold = new PageScaffold(new ReportPageView(redisConnectContext, owner));
        this.addTab(owner.$tr("MainTabView.pageScaffold.chart"), Icons.CONTENT_TAB_INFO_ICON, pageScaffold);
        diagnostics = new org.dromara.redisfront.ui.components.diagnostics.RedisDiagnosticsPanel(redisConnectContext, owner);
        this.addTab(owner.$tr("Diagnostics.title"), Icons.CONTENT_TAB_INFO_ICON, diagnostics);

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

        this.eventListener.bind(redisConnectContext.getId(), CommandExecuteEvent.class, qsEvent -> {
            if (qsEvent instanceof CommandExecuteEvent commandExecuteEvent) {
                if (!matchEventId(redisConnectContext.getId(), commandExecuteEvent.getId())) {
                    return;
                }
                Object message = commandExecuteEvent.getMessage();
                if (message instanceof LogInfo command) {
                    redisInfoView.appendLog(command);
                }
            }
        });
    }

    public static boolean matchEventId(int connectId, Integer eventId) {
        return Objects.equals(connectId, eventId);
    }

    private void initializeUI() {
        final String fontSize = owner.$tr("MainTabView.pageScaffold.fontSize");
        final Font font = getFont();
        this.setFont(font.deriveFont(Float.parseFloat(fontSize)));
        this.setTabPlacement(JTabbedPane.LEFT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_HEIGHT, 70);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.TOP);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE, FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE_COMPACT);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        var settingToolBar = new FlatToolBar();
        settingToolBar.setLayout(new MigLayout(new LC().align("center", "bottom")));
        var infoButton = new JButton(Icons.REDIS_INFO_ICON_24x24);
        infoButton.setToolTipText(owner.$tr("MainTabView.moreInfo.tooltip.text"));
        infoButton.addActionListener(ignore -> showInfoDialog());
        settingToolBar.add(infoButton);
        this.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, settingToolBar);
    }

    private void showInfoDialog() {
        if (infoDialog == null) infoDialog = new JDialog(owner, redisConnectContext.getHost());
        redisInfoView.refreshInfo();
        infoDialog.setContentPane(redisInfoView);
        infoDialog.setSize(800, 600);
        infoDialog.setLocationRelativeTo(null);
        infoDialog.setVisible(true);
    }

    @Override
    public void setUI(TabbedPaneUI ui) {
        super.setUI(new BoldTitleTabbedPaneUI());
    }
}
