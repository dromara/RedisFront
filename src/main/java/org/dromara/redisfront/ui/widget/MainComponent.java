package org.dromara.redisfront.ui.widget;

import cn.hutool.core.util.ArrayUtil;
import com.formdev.flatlaf.FlatLaf;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventListener;
import org.dromara.quickswing.ui.swing.Background;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.widget.sidebar.drawer.DrawerAnimationAction;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.event.OpenRedisConnectEvent;
import org.dromara.redisfront.ui.handler.ConnectHandler;
import org.dromara.redisfront.ui.handler.DrawerHandler;
import org.dromara.redisfront.ui.widget.content.MainContentComponent;
import org.dromara.redisfront.ui.widget.content.view.ContentTabView;
import org.dromara.redisfront.ui.widget.sidebar.MainSidebarComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;


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

        ConnectHandler connectHandler = context -> {
            Component[] components = mainRightPane.getComponents();
            if (ArrayUtil.isNotEmpty(components)) {
                Optional<Component> first = Arrays.stream(components).findFirst();
                if (first.isPresent()) {
                    if (first.get() instanceof MainContentComponent mainRightTabbedPanel) {
                        //todo add tab
                        System.out.println("JTabbedPane " + first.get());
                        SyncLoadingDialog.newInstance(owner).showSyncLoadingDialog(() -> {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return null;
                        }, (o, e) -> {
                            mainRightTabbedPanel.addTab(context.getTitle(), new ContentTabView(owner, context));
                        });
                    } else {
                        SyncLoadingDialog.newInstance(owner).showSyncLoadingDialog(() -> {
                            return null;
                        }, (o, e) -> {
                            if (e == null) {
                                mainRightPane.removeAll();
                                MainContentComponent mainTabbedPanel = createMainTabbedPanel(drawerAnimationAction);
                                mainTabbedPanel.addTab(context.getTitle(), new ContentTabView(owner, context));
                                mainRightPane.add(mainTabbedPanel, BorderLayout.CENTER);
                                FlatLaf.updateUI();
                            }
                        });
                    }
                }
            }
        };

        context.getEventBus().subscribe(new QSEventListener<>() {
            @Override
            protected void onEvent(QSEvent qsEvent) {
                if (qsEvent instanceof OpenRedisConnectEvent openRedisConnectEvent) {
                    Object message = openRedisConnectEvent.getMessage();
                    connectHandler.accept((ConnectContext) message);
                }
            }
        });

        this.mainLeftPanel = new MainSidebarComponent(owner, connectHandler, drawerAnimationAction, (key, index) -> {
            System.out.println("drawerMenuItemEvent" + " key:" + key);
            System.out.println("drawerMenuItemEvent" + " index:" + Arrays.toString(index));
        }).buildPanel();
        parentPanel.add(mainLeftPanel, BorderLayout.WEST);
        this.add(parentPanel, BorderLayout.CENTER);
    }

    private MainContentComponent createMainTabbedPanel(DrawerAnimationAction drawerAnimationAction) {
        MainContentComponent mainRightTabbedPanel = new MainContentComponent(drawerAnimationAction, owner);
        mainRightTabbedPanel.setTabCloseProcess(count -> {
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
