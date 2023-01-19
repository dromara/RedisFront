package com.redisfront.ui.component;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.form.MainNoneForm;
import com.redisfront.ui.form.fragment.DataSearchForm;
import com.redisfront.ui.form.fragment.DataViewForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * MainSplitComponent
 *
 * @author Jin
 */
public class DataSplitPanel extends JSplitPane {
    private static final Logger log = LoggerFactory.getLogger(DataSplitPanel.class);
    private final ConnectInfo connectInfo;

    public static DataSplitPanel newInstance(ConnectInfo connectInfo) {
        return new DataSplitPanel(connectInfo);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    public DataSplitPanel(ConnectInfo connectInfo) {

        this.connectInfo = connectInfo;

        var dataSearchForm = DataSearchForm.newInstance(connectInfo);

        this.setLeftComponent(dataSearchForm.getContentPanel());
        this.setRightComponent(commonNonePanel);

        //节点点击事件
        dataSearchForm.setNodeClickProcessHandler((treeNodeInfo) -> {
            var dataViewForm = DataViewForm.newInstance(connectInfo);

            dataViewForm.setRefreshBeforeHandler(dataSearchForm::scanBeforeProcess);

            dataViewForm.setRefreshAfterHandler(dataSearchForm::scanAfterProcess);

            dataViewForm.setDeleteActionHandler(() -> {
                dataSearchForm.deleteActionPerformed();
                setRightComponent(commonNonePanel);
            });

            dataViewForm.setCloseActionHandler(() -> setRightComponent(commonNonePanel));
            var startTime = System.currentTimeMillis();
            //加载数据并展示
            dataViewForm.dataChangeActionPerformed(treeNodeInfo.key(),
                    () -> SwingUtilities.invokeLater(() -> setRightComponent(commonLoadingPanel)),
                    () -> SwingUtilities.invokeLater(() -> setRightComponent(dataViewForm.contentPanel())));

            log.info("加载key用时：{}/ms", (System.currentTimeMillis() - startTime) / 1000);
        });
    }

    private static final JPanel commonLoadingPanel = new JPanel() {
        {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 5, 0, 0));
            add(new JPanel() {
                @Override
                public void updateUI() {
                    super.updateUI();
                    var flatLineBorder = new FlatLineBorder(new Insets(0, 2, 0, 2), UIManager.getColor("Component.borderColor"));
                    setBorder(flatLineBorder);
                    setLayout(new BorderLayout());
                    add(LoadingPanel.newInstance(), BorderLayout.CENTER);
                }
            }, BorderLayout.CENTER);
        }
    };

    private static final JPanel commonNonePanel = new JPanel() {
        {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(0, 5, 0, 0));
            add(new JPanel() {
                @Override
                public void updateUI() {
                    super.updateUI();
                    var flatLineBorder = new FlatLineBorder(new Insets(0, 2, 0, 0), UIManager.getColor("Component.borderColor"));
                    setBorder(flatLineBorder);
                    setLayout(new BorderLayout());
                    add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER);
                }
            }, BorderLayout.CENTER);
        }
    };


    public void ping() {
        RedisBasicService.service.ping(connectInfo);
    }

}
