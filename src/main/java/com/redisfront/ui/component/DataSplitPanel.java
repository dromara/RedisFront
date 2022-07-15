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

    public DataSplitPanel(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        var dataSearchForm = DataSearchForm.newInstance(connectInfo);

        var nonePanel = new JPanel() {
            {
                setLayout(new BorderLayout());
                setBorder(new EmptyBorder(0, 5, 0, 0));
                var subPanel = new JPanel() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        var flatLineBorder = new FlatLineBorder(new Insets(0, 2, 0, 0), UIManager.getColor("Component.borderColor"));
                        setBorder(flatLineBorder);
                    }

                    {
                        setLayout(new BorderLayout());
                        add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER);
                    }
                };
                add(subPanel, BorderLayout.CENTER);
            }
        };

        dataSearchForm.setNodeClickProcessHandler((treeNodeInfo) -> {
            var dataViewForm = DataViewForm.newInstance(connectInfo);
            dataViewForm.dataChangeActionPerformed(treeNodeInfo.key());
            dataViewForm.setDeleteActionHandler(() -> {
                dataSearchForm.deleteActionPerformed();
                setRightComponent(nonePanel);
            });
            setRightComponent(dataViewForm.contentPanel());
        });

        this.setLeftComponent(dataSearchForm.getContentPanel());
        this.setRightComponent(nonePanel);
    }


    public void ping() {
        RedisBasicService.service.ping(connectInfo);
    }

}
