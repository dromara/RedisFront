package org.dromara.redisfront.ui.widget.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.quickswing.constant.QSOs;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.components.monitor.RedisMonitor;
import org.dromara.redisfront.ui.components.monitor.RedisUsageInfo;
import org.dromara.redisfront.ui.event.DrawerChangeEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.dromara.redisfront.ui.widget.main.about.MainAboutPanel;
import org.dromara.redisfront.ui.widget.main.fragment.MainTabView;
import org.dromara.redisfront.ui.widget.main.listener.MouseDraggedListener;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerAnimationAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Slf4j
public class MainComponent extends JPanel {
    private final RedisFrontWidget owner;
    private final RedisFrontContext redisFrontContext;
    private final DrawerAnimationAction action;
    private final Map<Integer, ScheduledExecutorService> executorServiceMap;
    private final JLabel cpu = new JLabel(Icons.CPU_ICON);
    private final JLabel memory = new JLabel(Icons.MEMORY_ICON);
    private final JLabel network = new JLabel(Icons.WIFI_ICON);
    private JTabbedPane topTabbedPane;
    private FlatToolBar toolBar;
    private Integer displayId;
    @Setter
    private Consumer<Integer> tabCloseEvent;
    private JLabel mode;


    public MainComponent(DrawerAnimationAction action, RedisFrontWidget owner) {
        this.owner = owner;
        this.action = action;
        this.redisFrontContext = (RedisFrontContext) owner.getContext();
        this.executorServiceMap = new ConcurrentHashMap<>();
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
        this.initBottomToolBar();
    }


    private void initTopBar() {
        toolBar = new FlatToolBar();
        if (SystemInfo.isMacOS) {
            toolBar.setMargin(new Insets(2, 5, 0, 0));
        } else {
            toolBar.setMargin(new Insets(2, 6, 0, 0));
        }
        var closeDrawerBtn = new JButton(Icons.DRAWER_SHOW_OR_CLOSE_ICON);
        closeDrawerBtn.addActionListener(action);
        toolBar.add(closeDrawerBtn);
        action.setBeforeProcess(_ -> closeDrawerBtn.setVisible(false));
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
                        DrawerChangeEvent drawerChangeEvent = new DrawerChangeEvent(new Insets(10, 22, 10, 22));
                        redisFrontContext.getEventBus().publish(drawerChangeEvent);
                    } else {
                        toolBar.setMargin(new Insets(2, 6, 0, 0));
                        DrawerChangeEvent drawerChangeEvent = new DrawerChangeEvent(new Insets(10, 10, 10, 10));
                        redisFrontContext.getEventBus().publish(drawerChangeEvent);
                    }
                }

            } else {
                toolBar.setMargin(new Insets(2, 3, 0, 0));
            }
            closeDrawerBtn.setVisible(true);
            FlatLaf.updateUI();
        });

        //tabbedPane init
        topTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
            @Override
            public void setUI(TabbedPaneUI ui) {
                super.setUI(new BoldTitleTabbedPaneUI());
            }
        };

        if (SystemInfo.isLinux) {
            topTabbedPane.addMouseListener(new MouseDraggedListener(owner));
            topTabbedPane.addMouseMotionListener(new MouseDraggedListener(owner));
        }

        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_POLICY, FlatClientProperties.TABBED_PANE_POLICY_AS_NEEDED);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
        //Redis Tab 关闭事件
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
            Component component = tabbedPane.getComponentAt(tabIndex);
            if (component instanceof MainTabView mainTabView) {
                //关闭线程池
                RedisConnectContext redisConnectContext = mainTabView.getRedisConnectContext();
                ScheduledExecutorService executorService = executorServiceMap.remove(redisConnectContext.getId());
                if (executorService != null) {
                    executorService.shutdownNow();
                }
                //关闭ssh会话
//                if (RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
//                    JschManager.MANAGER.closeSession(redisConnectContext);
//                }
                //关闭移除消息监听器
                owner.getEventListener().unbind(redisConnectContext.getId());
            }
            tabbedPane.removeTabAt(tabIndex);
            tabCloseEvent.accept(tabbedPane.getTabCount());
        });

        FlatToolBar settingToolBar = new FlatToolBar();
        if (SystemInfo.isMacOS) {
            settingToolBar.setPreferredSize(new Dimension(-1, 39));
            settingToolBar.setBorder(new EmptyBorder(3, 0, 0, 0));
        } else {
            settingToolBar.setPreferredSize(new Dimension(-1, 33));
        }
        settingToolBar.setLayout(new MigLayout(new LC().align("center", "bottom")));
        settingToolBar.add(new JButton(Icons.SETTING_ICON_40x40));
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, toolBar);
        if (owner.getOS() == QSOs.WINDOWS) {
            topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_INSETS, new Insets(0, 0, 0, 130));
        }

        topTabbedPane.addChangeListener(_ -> {
            if (topTabbedPane.getSelectedIndex() == -1) {
                return;
            }
            if (topTabbedPane.getSelectedComponent() instanceof MainTabView mainTabView) {
                RedisConnectContext redisConnectContext = mainTabView.getRedisConnectContext();
                SwingUtilities.invokeLater(() -> mode.setText(owner.$tr(redisConnectContext.getRedisMode().modeName)));
                if (!executorServiceMap.containsKey(redisConnectContext.getId())) {
                    RedisMonitor monitor = new RedisMonitor(redisConnectContext);
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.scheduleAtFixedRate(() -> {
                        try {
                            RedisUsageInfo usage = monitor.getUsageInfo();
                            log.debug("[Redis {} - {} ] 使用：{}\n", redisConnectContext.getTitle(), redisConnectContext.getHost(), usage);
                            if (displayId == redisConnectContext.getId()) {
                                SwingUtilities.invokeLater(() -> {
                                    memory.setText(usage.getMemory());
                                    memory.setToolTipText("[ Redis " + redisConnectContext.getTitle() + "@" + redisConnectContext.getHost() + " ]\n内存已使用：" + usage.getMemory());
                                    cpu.setText(usage.getCpu());
                                    cpu.setToolTipText("[ Redis " + redisConnectContext.getTitle() + "@" + redisConnectContext.getHost() + " ]\nCPU使用率：" + usage.getCpu());
                                    String networkRate = String.format("%.2fKB/s｜%.2fKB/s", usage.getNetwork().inputRate() / 1024, usage.getNetwork().outputRate() / 1024);
                                    network.setText(networkRate);
                                    network.setToolTipText("[ Redis " + redisConnectContext.getTitle() + "@" + redisConnectContext.getHost() + " ]\n网络传输：" + usage.getNetwork());
                                });
                            }
                        } catch (Exception e) {
                            log.error("获取Redis使用信息失败", e);
                        }

                    }, 1, 3, TimeUnit.SECONDS);
                    executorServiceMap.put(redisConnectContext.getId(), scheduler);
                }
                displayId = redisConnectContext.getId();
            }

        });
        this.add(topTabbedPane, BorderLayout.CENTER);
    }

    JPanel aboutPanel;

    @Override
    public void updateUI() {
        if (aboutPanel != null) {
            aboutPanel.updateUI();
        }
        super.updateUI();
    }

    private void initBottomToolBar() {
        Box verticalBox = Box.createVerticalBox();
        verticalBox.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        verticalBox.add(new JSeparator());
        var rightToolBar = new FlatToolBar();
        rightToolBar.setLayout(new BorderLayout());
        rightToolBar.setMargin(new Insets(0, 3, 0, 3));

        mode = new JLabel(Icons.MODE_ICON);
        mode.setText("单机模式");
        rightToolBar.add(mode, BorderLayout.WEST);

        JPanel horizontalBox = new JPanel();
        horizontalBox.setLayout(new FlowLayout());
        rightToolBar.add(horizontalBox, BorderLayout.CENTER);

        cpu.setText("0.00%");
        horizontalBox.add(cpu);

        memory.setText("00MB");
        horizontalBox.add(memory);

        network.setText("0.00KB/s | 0.00KB/s");
        network.setFont(network.getFont().deriveFont(12f));
        horizontalBox.add(network);
        var version = new JLabel();
        {
            version.setText(redisFrontContext.version());
            version.setToolTipText(owner.$tr("Menu.Help.About.Title"));
            version.setIcon(Icons.REDIS_TEXT_80x16);
            version.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            version.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(owner, new Object[]{
                                    new MainAboutPanel(owner)
                            }, owner.$tr("Menu.Help.About.Title"),
                            JOptionPane.PLAIN_MESSAGE);
                }
            });
        }
        rightToolBar.add(version, BorderLayout.EAST);
        verticalBox.add(rightToolBar);


        this.add(verticalBox, BorderLayout.SOUTH);
    }

    public void addTab(String title, MainTabView mainTabView) {
        Optional<MainTabView> matchedPanel = Arrays
                .stream(topTabbedPane.getComponents())
                .map(e -> {
                    if (e instanceof MainTabView) {
                        return (MainTabView) e;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(e ->
                        e.getRedisConnectContext().getId() == mainTabView.getRedisConnectContext().getId()
                )
                .findFirst();
        if (matchedPanel.isPresent()) {
            topTabbedPane.setSelectedComponent(matchedPanel.get());
            return;
        }
        topTabbedPane.addTab(title, Icons.REDIS_ICON_14x14, mainTabView);
        topTabbedPane.setSelectedComponent(mainTabView);

    }
}
