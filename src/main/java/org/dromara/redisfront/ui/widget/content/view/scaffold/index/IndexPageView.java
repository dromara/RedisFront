package org.dromara.redisfront.ui.widget.content.view.scaffold.index;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.thread.ThreadUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventListener;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.model.TreeNodeInfo;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.event.ClickKeyTreeNodeEvent;
import org.dromara.redisfront.ui.widget.MainWidget;
import org.dromara.redisfront.ui.widget.content.ext.BorderWrapperPanel;

import javax.swing.*;
import java.awt.*;

@Getter
@Slf4j
public class IndexPageView extends QSPageItem<MainWidget> {
    private final RedisConnectContext redisConnectContext;
    private final JSplitPane splitPane;
    private final MainWidget owner;
    private final RedisFrontContext context;
    private TreeNodeInfo selectTreeNode;

    public IndexPageView(RedisConnectContext redisConnectContext, MainWidget owner) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.redisConnectContext = redisConnectContext;
        this.splitPane = new JSplitPane();
        this.setupUI();

    }


    @Override
    public void onLoad() {
        var leftSearchFragment = new LeftSearchFragment(owner, redisConnectContext);
        this.splitPane.setLeftComponent(leftSearchFragment.getContentPanel());
        this.splitPane.setRightComponent(new BorderWrapperPanel(NonePanel.getInstance()));
        this.context.getEventBus().subscribe(new QSEventListener<>(owner) {
            @Override
            protected void onEvent(QSEvent qsEvent) {
                if (qsEvent instanceof ClickKeyTreeNodeEvent clickKeyTreeNodeEvent) {
                    Object message = clickKeyTreeNodeEvent.getMessage();
                    if (message instanceof TreeNodeInfo treeNodeInfo) {
                        selectTreeNode = treeNodeInfo;
                        leftSearchFragment.scanBeforeProcess();
                        SyncLoadingDialog.newInstance(owner).showSyncLoadingDialog(() -> {
                            RightViewFragment rightViewFragment = new RightViewFragment(redisConnectContext);
                            ThreadUtil.safeSleep(2000);
                            return rightViewFragment.contentPanel();
                        }, (o, e) -> {
                            leftSearchFragment.scanAfterProcess();
                            if (e == null) {
                                splitPane.setRightComponent(new BorderWrapperPanel((JComponent) o));
                            } else {
                                owner.displayException(e);
                            }
                        });


//                        dataViewForm.setRefreshBeforeHandler();
//
//                        dataViewForm.setRefreshAfterHandler(leftSearchFragment::scanAfterProcess);
//
//                        dataViewForm.setDeleteActionHandler(() -> {
//                            leftSearchFragment.deleteActionPerformed();
//                            splitPane.setRightComponent(NonePanel.getInstance());
//                        });

//                        dataViewForm.setCloseActionHandler(() -> splitPane.setRightComponent(NonePanel.getInstance()));
                        StopWatch stopWatch = StopWatch.create("loadData");
                        stopWatch.start();
                        //加载数据并展示
//                        dataViewForm.dataChangeActionPerformed(treeNodeInfo.key(),
//                                () -> SwingUtilities.invokeLater(() -> splitPane.setRightComponent(NonePanel.getInstance())),
//                                () -> SwingUtilities.invokeLater(() -> splitPane.setRightComponent(dataViewForm.contentPanel())));
                        stopWatch.stop();
                        log.info("加载key用时：{}/ms", stopWatch.getTotalTimeSeconds());
                    }
                }
            }
        });
    }

    @Override
    public void onChange() {
        super.onChange();
    }

    @Override
    public MainWidget getApp() {
        return owner;
    }

    @Override
    protected JComponent getContentPanel() {
        return splitPane;
    }

}
