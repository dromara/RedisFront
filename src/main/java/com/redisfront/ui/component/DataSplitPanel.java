package com.redisfront.ui.component;

import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.form.MainNoneForm;
import com.redisfront.ui.form.fragment.DataSearchForm;
import com.redisfront.ui.form.fragment.DataViewForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

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

    public DataSplitPanel(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        var dataSearchForm = DataSearchForm.newInstance(connectInfo);

        dataSearchForm.setNodeClickProcessHandler((treeNodeInfo) -> {
            var dataViewForm = DataViewForm.newInstance(connectInfo);
            dataViewForm.dataChangeActionPerformed(treeNodeInfo.key());
            dataViewForm.setDeleteActionHandler(() -> {
                dataSearchForm.deleteActionPerformed();
                setRightComponent(MainNoneForm.getInstance().getContentPanel());
            });
            setRightComponent(dataViewForm.contentPanel());
        });

        this.setLeftComponent(dataSearchForm.getContentPanel());
        this.setRightComponent(MainNoneForm.getInstance().getContentPanel());
    }


    public void ping() {
        RedisBasicService.service.ping(connectInfo);
    }

}
