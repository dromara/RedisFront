package com.redisfront.ui.component;

import com.redisfront.model.ConnectInfo;
import com.redisfront.model.TreeNodeInfo;
import com.redisfront.service.RedisService;
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

        var dataViewForm = DataViewForm.newInstance(connectInfo);
        this.setRightComponent(dataViewForm.contentPanel());

        var dataSearchForm = DataSearchForm.newInstance(dataViewForm::dataChange, connectInfo);
        this.setLeftComponent(dataSearchForm.getContentPanel());
    }

    public void ping() {
        RedisService.service.ping(connectInfo);
    }

}
