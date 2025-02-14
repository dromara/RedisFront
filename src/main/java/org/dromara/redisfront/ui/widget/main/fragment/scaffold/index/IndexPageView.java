package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.panel.BorderNonePanel;
import org.dromara.redisfront.ui.components.panel.WrapperPanel;
import org.dromara.redisfront.ui.event.ClickKeyTreeNodeEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;

@Getter
@Slf4j
public class IndexPageView extends QSPageItem<RedisFrontWidget> {
    private final RedisConnectContext redisConnectContext;
    private final JSplitPane splitPane;
    private final RedisFrontWidget owner;
    private final RedisFrontContext context;
    private TreeNodeInfo selectTreeNode;

    public IndexPageView(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.redisConnectContext = redisConnectContext;
        this.splitPane = new JSplitPane();
        this.setupUI();

    }

    @Override
    public void onLoad() {
        var leftSearchFragment = new LeftSearchFragment(owner, redisConnectContext);
        this.splitPane.setDividerSize(0);
        this.splitPane.setLeftComponent(leftSearchFragment.getContentPanel());
        this.splitPane.setRightComponent(new BorderNonePanel());
        this.owner.getEventListener().bind(redisConnectContext.getId(), ClickKeyTreeNodeEvent.class, qsEvent -> {
            if (qsEvent instanceof ClickKeyTreeNodeEvent clickKeyTreeNodeEvent) {
                if (redisConnectContext.getId() != clickKeyTreeNodeEvent.getId()) {
                    return;
                }
                int dividerLocation = this.splitPane.getDividerLocation();
                Object message = clickKeyTreeNodeEvent.getMessage();
                if (message instanceof TreeNodeInfo treeNodeInfo) {
                    selectTreeNode = treeNodeInfo;
                    SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                        RightViewFragment rightViewFragment = new RightViewFragment(redisConnectContext, treeNodeInfo, owner);
                        rightViewFragment.fetchData();
                        return rightViewFragment;
                    }, (rightViewFragment, e) -> {
                        if (e == null) {
                            this.splitPane.setDividerSize(5);
                            this.splitPane.setDividerLocation(dividerLocation);
                            this.splitPane.setRightComponent(new WrapperPanel(rightViewFragment.contentPanel()));
                            rightViewFragment.loadData();
                        } else {
                            owner.displayException(e);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onChange() {
        super.onChange();
    }

    @Override
    public RedisFrontWidget getApp() {
        return owner;
    }

    @Override
    protected JComponent getContentPanel() {
        return splitPane;
    }

}
