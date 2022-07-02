package com.redisfront.ui.component;

import com.formdev.flatlaf.extras.components.FlatTextPane;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
import com.redisfront.ui.form.DataSearchForm;
import com.redisfront.ui.form.DataViewForm;
import com.redisfront.util.MsgUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * MainSplitComponent
 *
 * @author Jin
 */
public class DataSplitPanel extends JSplitPane {
    private static final Logger log = LoggerFactory.getLogger(TerminalComponent.class);
    private final ConnectInfo connectInfo;

    public static DataSplitPanel newInstance(ConnectInfo connectInfo) {
        return new DataSplitPanel(connectInfo);
    }

    public DataSplitPanel(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        this.init();
    }

    public void init() {
        var searchForm = DataSearchForm
                .newInstance()
                .setConnectInfo(connectInfo)
                .setNodeClickCallback((System.out::println));
        var viewForm = DataViewForm
                .newInstance();
        setLeftComponent(searchForm.getContentPanel());
        searchForm.init();
        setRightComponent(new FlatTextPane());
    }


    public void ping() {
        try {
            if (!RedisService.service.ping(connectInfo)) {
                MsgUtil.showErrorDialog("redis 服务器无响应", null);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            MsgUtil.showErrorDialog("Redis Server Connect Failed", e);
        }
    }

}
