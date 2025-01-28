package org.dromara.redisfront.ui.widget.content.view.scaffold.index;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.widget.MainWidget;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class IndexPageView extends QSPageItem<MainWidget> {
    @Getter
    private final ConnectContext connectContext;

    private final JSplitPane splitPane ;
    @Getter
    private final MainWidget owner;

    public IndexPageView(ConnectContext connectContext, MainWidget owner) {
        this.connectContext = connectContext;
        this.owner = owner;
        this.splitPane = new JSplitPane();
        this.setLayout(new BorderLayout());
        IndexSearchFragment indexSearchFragment = new IndexSearchFragment(owner, connectContext);
        this.splitPane.setLeftComponent(indexSearchFragment.getContentPanel());
        this.splitPane.setRightComponent(NonePanel.getInstance());

        //节点点击事件
        indexSearchFragment.setNodeClickProcessHandler((treeNodeInfo) -> {
            var dataViewForm = IndexViewFragment.newInstance(connectContext);

            dataViewForm.setRefreshBeforeHandler(indexSearchFragment::scanBeforeProcess);

            dataViewForm.setRefreshAfterHandler(indexSearchFragment::scanAfterProcess);

            dataViewForm.setDeleteActionHandler(() -> {
                indexSearchFragment.deleteActionPerformed();
                splitPane.setRightComponent(NonePanel.getInstance());
            });

            dataViewForm.setCloseActionHandler(() -> splitPane.setRightComponent(NonePanel.getInstance()));
            var startTime = System.currentTimeMillis();
            //加载数据并展示
            dataViewForm.dataChangeActionPerformed(treeNodeInfo.key(),
                    () -> SwingUtilities.invokeLater(() -> splitPane.setRightComponent(NonePanel.getInstance())),
                    () -> SwingUtilities.invokeLater(() -> splitPane.setRightComponent(dataViewForm.contentPanel())));

            log.info("加载key用时：{}/ms", (System.currentTimeMillis() - startTime) / 1000);
        });
        this.setupUI();
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
