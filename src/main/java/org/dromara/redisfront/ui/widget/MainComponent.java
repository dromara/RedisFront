package org.dromara.redisfront.ui.widget;

import cn.hutool.core.util.ArrayUtil;
import com.formdev.flatlaf.FlatLaf;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.sentinel.api.sync.RedisSentinelCommands;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.event.OpenRedisConnectEvent;
import org.dromara.redisfront.ui.handler.ConnectHandler;
import org.dromara.redisfront.ui.handler.DrawerHandler;
import org.dromara.redisfront.ui.widget.content.MainContentComponent;
import org.dromara.redisfront.ui.widget.content.view.ContentTabView;
import org.dromara.redisfront.ui.widget.sidebar.MainSidebarComponent;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerAnimationAction;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;


public class MainComponent extends Background {

    public static final int DEFAULT_DRAWER_WIDTH = 250;

    private final MainWidget owner;
    private final RedisFrontContext context;
    private JPanel mainLeftPanel;
    private JPanel mainRightPane;

    public MainComponent(MainWidget owner) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
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
                            if (component instanceof MainContentComponent mainRightTabbedPanel) {
                                SyncLoadingDialog.newInstance(owner).showSyncLoadingDialog(() -> {
                                    fetchRedisMode(redisConnectContext);
                                    return new ContentTabView(owner, redisConnectContext);
                                }, (o, e) -> {
                                    if (e == null) {
                                        mainRightTabbedPanel.addTab(redisConnectContext.getTitle(), (ContentTabView) o);
                                    } else {
                                /*
                                  连接失败时，弹出密码输入框，输入密码后重试
                                if (e instanceof RedisException redisException) {
                                    var ex = redisException.getCause();
                                    if (Fn.equal(ex.getMessage(), "WRONGPASS invalid username-password pair or user is disabled.")) {
                                        var password = JOptionPane.showInputDialog(RedisFrontMain.frame, String.format(LocaleUtils.getMessageFromBundle("MainWindowForm.JOptionPane.showInputDialog.message"), redisConnectContext.getHost(), redisConnectContext.getPort()));
                                        redisConnectContext.setPassword(password);
                                        LettuceUtils.run(redisConnectContext, BaseRedisCommands::ping);
                                    }
                                } else {
                                    owner.displayException(e);
                                }
                                 */
                                        owner.displayException(e);
                                    }
                                });
                            } else {
                                SyncLoadingDialog.newInstance(owner).showSyncLoadingDialog(() -> {
                                    fetchRedisMode(redisConnectContext);
                                    MainContentComponent mainTabbedPanel = createMainTabbedPanel(drawerAnimationAction);
                                    ContentTabView contentTabView = new ContentTabView(owner, redisConnectContext);
                                    mainTabbedPanel.addTab(redisConnectContext.getTitle(), contentTabView);
                                    return mainTabbedPanel;
                                }, (ret, e) -> {
                                    if (e == null) {
                                        mainRightPane.remove(component);
                                        mainRightPane.add((MainContentComponent) ret, BorderLayout.CENTER);
                                        FlatLaf.updateUI();
                                    } else {
                                        owner.displayException(e);
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

        this.mainLeftPanel = new MainSidebarComponent(owner, connectHandler, drawerAnimationAction, (key, index) -> {
            System.out.println("drawerMenuItemEvent" + " key:" + key);
            System.out.println("drawerMenuItemEvent" + " index:" + Arrays.toString(index));
        }).buildPanel();
        parentPanel.add(mainLeftPanel, BorderLayout.WEST);
        this.add(parentPanel, BorderLayout.CENTER);
    }

    private static void fetchRedisMode(RedisConnectContext redisConnectContext) {
        RedisMode redisModeEnum = RedisBasicService.service.getRedisModeEnum(redisConnectContext);
        redisConnectContext.setRedisMode(redisModeEnum);
        if (RedisMode.SENTINEL == redisConnectContext.getRedisMode()) {
            var masterList = LettuceUtils.sentinelExec(redisConnectContext, RedisSentinelCommands::masters);
            var master = masterList.stream().findAny().orElseThrow();
            redisConnectContext.setHost(master.get("ip"));
            redisConnectContext.setPort(Integer.valueOf(master.get("port")));
            LettuceUtils.run(redisConnectContext, BaseRedisCommands::ping);
        }
    }

    private MainContentComponent createMainTabbedPanel(DrawerAnimationAction drawerAnimationAction) {
        MainContentComponent mainRightTabbedPanel = new MainContentComponent(drawerAnimationAction, owner);
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
