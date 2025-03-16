package org.dromara.redisfront.ui.widget;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatLaf;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.jsch.JschManager;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.event.OpenRedisConnectEvent;
import org.dromara.redisfront.ui.handler.ConnectHandler;
import org.dromara.redisfront.ui.handler.DrawerHandler;
import org.dromara.redisfront.ui.widget.main.MainComponent;
import org.dromara.redisfront.ui.widget.main.fragment.MainTabView;
import org.dromara.redisfront.ui.widget.sidebar.SidebarComponent;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerAnimationAction;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;


public class RedisFrontComponent extends Background {

    public static final int DEFAULT_DRAWER_WIDTH = 250;

    private final RedisFrontWidget owner;
    private JPanel mainLeftPanel;
    private JPanel mainRightPane;

    public RedisFrontComponent(RedisFrontWidget owner) {
        this.owner = owner;
        this.setLayout(new BorderLayout());
        this.initComponents();
    }

    private void initComponents() {
        var parentPanel = new JPanel(new BorderLayout());
        this.mainRightPane = new JPanel();
        this.mainRightPane.setLayout(new BorderLayout());
        this.mainRightPane.add(NonePanel.getInstance(), BorderLayout.CENTER);
        parentPanel.add(mainRightPane, BorderLayout.CENTER);

        DrawerHandler drawerHandler = (fraction, drawerOpen) -> {
            int width = getDrawerWidth(fraction, drawerOpen);
            this.mainLeftPanel.setPreferredSize(new Dimension(width, -1));
            this.mainLeftPanel.updateUI();
        };

        var drawerAnimationAction = new DrawerAnimationAction(owner, drawerHandler);

        ConnectHandler connectHandler = redisConnectContext -> {
            Component[] components = mainRightPane.getComponents();
            if (ArrayUtil.isNotEmpty(components)) {
                Arrays.stream(components)
                        .findFirst()
                        .ifPresent(component -> {
                            if (RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
                                JschManager.MANAGER.openSession(redisConnectContext);
                            }
                            if (component instanceof MainComponent mainRightTabbedPanel) {
                                SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                                    fetchRedisMode(redisConnectContext);
                                    return new MainTabView(owner, redisConnectContext);
                                }, (o, e) -> {
                                    if (e == null) {
                                        mainRightTabbedPanel.addTab(redisConnectContext.getTitle(), o);
                                    } else {
                                        owner.displayException(e);
                                        if (RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
                                            JschManager.MANAGER.closeSession(redisConnectContext);
                                        }
                                    }
                                });
                            } else {
                                SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                                    fetchRedisMode(redisConnectContext);
                                    MainComponent mainTabbedPanel = createMainTabbedPanel(drawerAnimationAction);
                                    mainTabbedPanel.addTab(redisConnectContext.getTitle(), new MainTabView(owner, redisConnectContext));
                                    return mainTabbedPanel;
                                }, (mainComponent, e) -> {
                                    if (e == null) {
                                        mainRightPane.remove(component);
                                        mainRightPane.add(mainComponent, BorderLayout.CENTER);
                                        FlatLaf.updateUI();
                                    } else {
                                        owner.displayException(e);
                                        if (RedisFrontUtils.equal(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
                                            JschManager.MANAGER.closeSession(redisConnectContext);
                                        }
                                    }
                                });
                            }
                        });
            }
        };
        owner.getEventListener().bind(-1, OpenRedisConnectEvent.class, qsEvent -> {
            if (qsEvent instanceof OpenRedisConnectEvent openRedisConnectEvent) {
                Object message = openRedisConnectEvent.getMessage();
                connectHandler.accept((RedisConnectContext) message);
            }
        });
        SidebarComponent sidebarComponent = new SidebarComponent(owner, connectHandler, drawerAnimationAction);
        this.mainLeftPanel = sidebarComponent.buildPanel();
        parentPanel.add(mainLeftPanel, BorderLayout.WEST);
        this.add(parentPanel, BorderLayout.CENTER);
    }

    private void fetchRedisMode(RedisConnectContext redisConnectContext) {
        RedisMode redisModeEnum = RedisBasicService.service.getRedisModeEnum(redisConnectContext);
        redisConnectContext.setRedisMode(redisModeEnum);
        if (RedisMode.SENTINEL == redisConnectContext.getRedisMode()) {
            var masterList = LettuceUtils.sentinelExec(redisConnectContext, RedisSentinelCommands::masters);
            var master = masterList.stream().findAny().orElseThrow();
            String ip = master.get("ip");
            if (StrUtil.equals(ip, redisConnectContext.getHost())) {
                redisConnectContext.setHost(ip);
            } else {
                if (RedisFrontUtils.notEqual(redisConnectContext.getConnectTypeMode(), ConnectType.SSH)) {
                    RedisFrontUtils.runEDTSync(() -> {
                        String cstIp = JOptionPane.showInputDialog(owner, String.format(owner.$tr("MainWindowForm.Sentinel.showIpInputDialog.message"), ip, redisConnectContext.getHost()), ip);
                        redisConnectContext.setHost(cstIp);
                    });
                } else {
                    redisConnectContext.setHost(ip);
                }
            }
            redisConnectContext.setPort(Integer.valueOf(master.get("port")));
        }
        if (RedisMode.CLUSTER == redisConnectContext.getRedisMode()) {
            JschManager.MANAGER.openClusterSession(redisConnectContext);
        }
        if (RedisMode.SENTINEL == redisConnectContext.getRedisMode()) {
            JschManager.MANAGER.openSession(redisConnectContext);
            try {
                LettuceUtils.exec(redisConnectContext, BaseRedisCommands::ping);
            } catch (Exception e) {
                if (e instanceof RedisFrontException) {
                    RedisFrontUtils.runEDTSync(() -> {
                        if (RedisFrontUtils.equal(e.getMessage(), "WRONGPASS invalid username-password pair or user is disabled.")) {
                            var password = JOptionPane.showInputDialog(owner, String.format(owner.$tr("MainWindowForm.Sentinel.showPasswordInputDialog.message"), redisConnectContext.getHost(), redisConnectContext.getPort()));
                            redisConnectContext.setPassword(password);
                        }
                    });
                } else {
                    throw e;
                }
            }
        }
    }

    private MainComponent createMainTabbedPanel(DrawerAnimationAction drawerAnimationAction) {
        MainComponent mainRightTabbedPanel = new MainComponent(drawerAnimationAction, owner);
        mainRightTabbedPanel.setTabCloseEvent(count -> {
            if (count == 0) {
                if (!drawerAnimationAction.isDrawerOpen()) {
                    drawerAnimationAction.handleAction(null);
                }
                mainRightPane.removeAll();
                mainRightPane.add(NonePanel.getInstance(), BorderLayout.CENTER);
                FlatLaf.updateUI();
            }
        });
        return mainRightTabbedPanel;
    }

    private int getDrawerWidth(Double fraction, Boolean drawerOpen) {
        int width;
        if (drawerOpen) {
            width = (int) (DEFAULT_DRAWER_WIDTH - DEFAULT_DRAWER_WIDTH * fraction);
        } else {
            width = (int) (DEFAULT_DRAWER_WIDTH * fraction);
        }
        return width;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (mainLeftPanel != null) {
            mainLeftPanel.updateUI();
        }
    }

}
