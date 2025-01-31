package org.dromara.redisfront.ui.widget.content.view.scaffold.index;

import cn.hutool.core.date.StopWatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.widget.MainWidget;
import org.dromara.redisfront.ui.widget.content.extend.BorderWrapperPanel;

import javax.swing.*;

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
        LeftSearchFragment leftSearchFragment = new LeftSearchFragment(owner, connectContext);
        //节点点击事件
        leftSearchFragment.setNodeClickProcessHandler((treeNodeInfo) -> {
            var dataViewForm = RightViewFragment.newInstance(connectContext);

            dataViewForm.setRefreshBeforeHandler(leftSearchFragment::scanBeforeProcess);

            dataViewForm.setRefreshAfterHandler(leftSearchFragment::scanAfterProcess);

            dataViewForm.setDeleteActionHandler(() -> {
                leftSearchFragment.deleteActionPerformed();
                splitPane.setRightComponent(NonePanel.getInstance());
            });

            dataViewForm.setCloseActionHandler(() -> splitPane.setRightComponent(NonePanel.getInstance()));
            StopWatch stopWatch = StopWatch.create("loadData");
            stopWatch.start();
            //加载数据并展示
            dataViewForm.dataChangeActionPerformed(treeNodeInfo.key(),
                    () -> SwingUtilities.invokeLater(() -> splitPane.setRightComponent(NonePanel.getInstance())),
                    () -> SwingUtilities.invokeLater(() -> splitPane.setRightComponent(dataViewForm.contentPanel())));
            stopWatch.stop();
            log.info("加载key用时：{}/ms", stopWatch.getTotalTimeSeconds());
        });
        this.splitPane.setLeftComponent(leftSearchFragment.getContentPanel());
        this.splitPane.setRightComponent(new BorderWrapperPanel(NonePanel.getInstance()));
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
