package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import cn.hutool.core.exceptions.ExceptionUtil;
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
import org.dromara.redisfront.ui.event.KeyDeleteSuccessEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;

@Getter
@Slf4j
public class IndexPageView extends QSPageItem<RedisFrontWidget> {
    private final RedisConnectContext redisConnectContext;
    private final JSplitPane splitPane;
    private final RedisFrontWidget owner;
    private final RedisFrontContext context;
    private final LeftSearchFragment leftSearchFragment;
    private TreeNodeInfo selectTreeNode;
    private RightViewFragment rightView;

    public boolean confirmDiscardChanges() { return rightView == null || rightView.confirmDiscardChanges(); }
    public void disposeSearch() { leftSearchFragment.disposeSearch(); }

    public IndexPageView(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.redisConnectContext = redisConnectContext;
        this.splitPane = new JSplitPane();
        this.leftSearchFragment = new LeftSearchFragment(owner, redisConnectContext);
        leftSearchFragment.setDatabaseChangeGuard(() -> {
            if (!confirmDiscardChanges()) return false;
            rightView = null;selectTreeNode = null;splitPane.setRightComponent(new BorderNonePanel());return true;
        });
        this.setupUI();

    }

    @Override
    public void onLoad() {
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
                    if (!confirmDiscardChanges()) { leftSearchFragment.restoreKeySelection(selectTreeNode);return; }
                    RightViewFragment candidate = new RightViewFragment(redisConnectContext, treeNodeInfo, owner);
                    SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                        candidate.fetchDataActionPerformed();
                        return candidate;
                    }, (rightViewFragment, e) -> {
                        if (e == null) {
                            rightView = rightViewFragment;
                            selectTreeNode = treeNodeInfo;
                            this.splitPane.setDividerSize(5);
                            this.splitPane.setDividerLocation(dividerLocation);
                            this.splitPane.setRightComponent(new WrapperPanel(rightViewFragment.contentPanel()));
                            rightViewFragment.refreshUI();
                        } else {
                            leftSearchFragment.restoreKeySelection(selectTreeNode);
                            owner.displayException(ExceptionUtil.unwrap(e));
                        }
                    });
                }
            }
        });

        this.owner.getEventListener().bind(redisConnectContext.getId(), KeyDeleteSuccessEvent.class, qsEvent -> {
            if (qsEvent instanceof KeyDeleteSuccessEvent KeyDeleteSuccessEvent) {
                if (redisConnectContext.getId() != KeyDeleteSuccessEvent.getId()) {
                    return;
                }
                this.splitPane.setDividerSize(0);
                rightView = null;selectTreeNode = null;
                this.splitPane.setRightComponent(new BorderNonePanel());
                leftSearchFragment.getRefreshBtn().doClick();
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
