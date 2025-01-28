package org.dromara.redisfront.ui.components.panel;

import com.formdev.flatlaf.ui.FlatLineBorder;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.form.MainNoneForm;
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
@Deprecated
public class DataSplitPanel extends JSplitPane {
    private static final Logger log = LoggerFactory.getLogger(DataSplitPanel.class);
    private final ConnectContext connectContext;

    public static DataSplitPanel newInstance(ConnectContext connectContext) {
        return new DataSplitPanel(connectContext);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    public DataSplitPanel(ConnectContext connectContext) {

        this.connectContext = connectContext;


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
                    add(MainNoneForm.getInstance(), BorderLayout.CENTER);
                }
            }, BorderLayout.CENTER);
        }
    };


    public void ping() {
        RedisBasicService.service.ping(connectContext);
    }

}
