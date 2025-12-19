package org.dromara.redisfront.ui.widget.main;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.util.SystemInfo;
import io.lettuce.core.cluster.models.partitions.Partitions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.dromara.quickswing.constant.QSOs;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.jsch.JschManager;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.extend.BoldTitleTabbedPaneUI;
import org.dromara.redisfront.ui.components.monitor.RedisMonitor;
import org.dromara.redisfront.ui.components.monitor.RedisUsageInfo;
import org.dromara.redisfront.ui.event.DrawerChangeEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.dromara.redisfront.ui.widget.main.about.MainAboutPanel;
import org.dromara.redisfront.ui.widget.main.fragment.MainTabView;
import org.dromara.redisfront.ui.widget.main.listener.MouseDraggedListener;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerAnimationAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.TabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
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
import java.util.stream.Collectors;

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
    private RedisConnectContext currentRedisConnectContext;
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
            toolBar.setMargin(new Insets(2, 8, 0, 0));
        }
        var closeDrawerBtn = new JButton(Icons.DRAWER_SHOW_OR_CLOSE_ICON);
        closeDrawerBtn.addActionListener(action);
        toolBar.add(closeDrawerBtn);
        action.setBeforeProcess(ignore -> closeDrawerBtn.setVisible(false));
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
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, false);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TABS_POPUP_POLICY, FlatClientProperties.TABBED_PANE_POLICY_NEVER);
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_INSETS, new Insets(4, 4, 4, 4));
        //Redis Tab 关闭事件
        topTabbedPane.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK, (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
            Component component = tabbedPane.getComponentAt(tabIndex);
            if (component instanceof MainTabView mainTabView) {
                //清理资源
                mainTabView.clearUp();
                //关闭线程池
                RedisConnectContext redisConnectContext = mainTabView.getRedisConnectContext();
                ScheduledExecutorService executorService = executorServiceMap.remove(redisConnectContext.getId());
                if (executorService != null) {
                    executorService.shutdownNow();
                }
                //关闭连接池
                RedisConnectionPoolManager.cleanupContextPool(redisConnectContext);
                //关闭ssh会话
                if (RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
                    JschManager.MANAGER.closeSession(redisConnectContext);
                }
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

        topTabbedPane.addChangeListener(ignore -> {
            if (topTabbedPane.getSelectedIndex() == -1) {
                return;
            }
            if (topTabbedPane.getSelectedComponent() instanceof MainTabView mainTabView) {
                RedisConnectContext redisConnectContext = mainTabView.getRedisConnectContext();
                RedisFrontUtils.runEDT(() -> {
                    mode.setText(owner.$tr(redisConnectContext.getRedisMode().modeName));
                    mode.setToolTipText(redisConnectContext.getHost() + " | " + owner.$tr(redisConnectContext.getRedisMode().modeName));
                });
                executorServiceMap.computeIfAbsent(redisConnectContext.getId(), ignore1 -> {
                    RedisMonitor monitor = new RedisMonitor(owner, redisConnectContext);
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.scheduleAtFixedRate(() -> {
                        try {
                            RedisUsageInfo usage = monitor.getUsageInfo();
                            log.debug("[Redis {} - {} ] 使用：{}\n", redisConnectContext.getTitle(), redisConnectContext.getHost(), usage);
                            if (currentRedisConnectContext.getId() == redisConnectContext.getId()) {
                                SwingUtilities.invokeLater(() -> {
                                    memory.setText(usage.getMemory());
                                    memory.setToolTipText(redisConnectContext.getHost() + " | Memory Usage => " + usage.getMemory());
                                    cpu.setText(usage.getCpu());
                                    cpu.setToolTipText(redisConnectContext.getHost() + " | Cpu Usage => " + usage.getCpu());
                                    String networkRate = String.format("%.2fKB/s｜%.2fKB/s", usage.getNetwork().inputRate() / 1024, usage.getNetwork().outputRate() / 1024);
                                    network.setText(networkRate);
                                    network.setToolTipText(redisConnectContext.getHost() + " | NetWork Usage => " + usage.getNetwork());
                                });
                            }
                        } catch (Exception e) {
                            log.error("获取Redis使用信息失败", e);
                        }

                    }, 1, 3, TimeUnit.SECONDS);
                    return scheduler;
                });
                this.currentRedisConnectContext = redisConnectContext;
            }

        });
        this.add(topTabbedPane, BorderLayout.CENTER);
    }

    private void initBottomToolBar() {
        Box verticalBox = Box.createVerticalBox();
        verticalBox.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        verticalBox.add(new JSeparator());
        var rightToolBar = new FlatToolBar();
        rightToolBar.setLayout(new BorderLayout());
        rightToolBar.setMargin(new Insets(0, 3, 0, 3));

        mode = new JLabel(Icons.MODE_ICON);
        mode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mode.setText(owner.$tr("MainComponent.Text"));
        mode.setToolTipText(owner.$tr("MainComponent.TipText"));

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
        int tabIndex = topTabbedPane.getTabCount();
        topTabbedPane.addTab(title, Icons.REDIS_ICON_14x14, mainTabView);

        JPanel tabComponent = createCustomTabComponent(title, topTabbedPane, tabIndex);
        topTabbedPane.setTabComponentAt(tabIndex, tabComponent);
        topTabbedPane.setSelectedComponent(mainTabView);

    }

    private JPanel createCustomTabComponent(String title, JTabbedPane tabbedPane, int index) {
        final boolean[] isHovered = {false};

        JPanel panel = getJPanel(tabbedPane, isHovered);

        JLabel iconLabel = new JLabel(Icons.REDIS_ICON_14x14);
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(12f));
        panel.add(titleLabel, BorderLayout.CENTER);

        JLabel closeLabel = new JLabel("×") {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(16, 16);
            }
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(16, 16);
            }
        };
        closeLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
        closeLabel.setForeground(new Color(160, 160, 160));
        closeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        closeLabel.setVerticalAlignment(SwingConstants.CENTER);
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (tabbedPane.getSelectedIndex() == index) {
            closeLabel.setText("×");
            closeLabel.setEnabled(true);
        } else {
            closeLabel.setText("");
            closeLabel.setEnabled(false);
        }
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                int currentIndex = tabbedPane.indexOfTabComponent(panel);
                if (currentIndex >= 0 && tabbedPane.getSelectedIndex() != currentIndex) {
                    isHovered[0] = true;
                    panel.repaint();
                }
                closeLabel.setForeground(new Color(255, 100, 100));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeLabel.setForeground(new Color(160, 160, 160));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                @SuppressWarnings("unchecked")
                BiConsumer<JTabbedPane, Integer> callback =
                    (BiConsumer<JTabbedPane, Integer>) tabbedPane.getClientProperty(
                        FlatClientProperties.TABBED_PANE_TAB_CLOSE_CALLBACK
                    );
                if (callback != null) {
                    int currentIndex = tabbedPane.indexOfTabComponent(panel);
                    if (currentIndex >= 0) {
                        callback.accept(tabbedPane, currentIndex);
                    }
                }
            }
        });
        panel.add(closeLabel, BorderLayout.EAST);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                int currentIndex = tabbedPane.indexOfTabComponent(panel);
                if (currentIndex >= 0 && tabbedPane.getSelectedIndex() != currentIndex) {
                    isHovered[0] = true;
                    closeLabel.setText("×");
                    closeLabel.setEnabled(true);
                    panel.repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                Point mousePos = e.getPoint();
                if (!panel.contains(mousePos)) {
                    int currentIndex = tabbedPane.indexOfTabComponent(panel);
                    isHovered[0] = false;
                    if (currentIndex >= 0 && tabbedPane.getSelectedIndex() != currentIndex) {
                        closeLabel.setText("");
                        closeLabel.setEnabled(false);
                    }
                    panel.repaint();
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                int currentIndex = tabbedPane.indexOfTabComponent(panel);
                if (currentIndex >= 0) {
                    tabbedPane.setSelectedIndex(currentIndex);
                }
            }
        });

        tabbedPane.addChangeListener(e -> {
            int currentIndex = tabbedPane.indexOfTabComponent(panel);
            if (currentIndex >= 0) {
                labelChangeEvent(tabbedPane, currentIndex, titleLabel);
                if (tabbedPane.getSelectedIndex() == currentIndex) {
                    closeLabel.setText("×");
                    closeLabel.setEnabled(true);
                } else {
                    closeLabel.setText("");
                    closeLabel.setEnabled(false);
                }
            }
            panel.repaint();
        });

        labelChangeEvent(tabbedPane, index, titleLabel);

        return panel;
    }

    private void labelChangeEvent(JTabbedPane tabbedPane, int index, JLabel titleLabel) {
        if (tabbedPane.getSelectedIndex() == index) {
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 12f));
            titleLabel.setForeground(new Color(220, 50, 50));
        } else {
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
            titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        }
    }

    private @NotNull JPanel getJPanel(JTabbedPane tabbedPane, boolean[] isHovered) {
        final JPanel[] panelRef = new JPanel[1];
        
        JPanel panel = new JPanel(new BorderLayout(5, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int currentIndex = tabbedPane.indexOfTabComponent(panelRef[0]);
                if (currentIndex >= 0 && tabbedPane.getSelectedIndex() == currentIndex) {
                    g2d.setColor(new Color(170, 169, 169, 63));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                } else if (isHovered[0]) {
                    g2d.setColor(new Color(128, 128, 128, 40));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2d.setColor(new Color(128, 128, 128, 150));
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                }
                g2d.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 5));
        panelRef[0] = panel;
        return panel;
    }
}
