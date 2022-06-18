package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.ui.form._DashboardForm;
import cn.devcms.redisfront.ui.form._DatabaseForm;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import redis.clients.jedis.*;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS;

public class DatabaseTabbedComponent extends JPanel {

    public DatabaseTabbedComponent(ConnectInfo connectInfo) {
        setLayout(new BorderLayout());
        var contentPanel = new JTabbedPane();
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.CENTER);
        contentPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, false);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_TRAILING);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        var leftToolBar = new FlatToolBar();
        var leftToolBarLayout = new FlowLayout();
        leftToolBarLayout.setAlignment(FlowLayout.CENTER);
        leftToolBar.setLayout(leftToolBarLayout);

        JedisClientConfig jedisClientConfig = DefaultJedisClientConfig
                .builder()
                .database(connectInfo.database())
                .user(connectInfo.user())
                .password(connectInfo.password())
                .build();
        Jedis jedis = new Jedis(connectInfo.host(), connectInfo.port(), jedisClientConfig);
        String string = jedis.clusterNodes();
        String clientInfo = jedis.info();
        String clusterInfo = jedis.clusterInfo();
        System.out.println(clientInfo);
        java.util.List<String> list = jedis.aclCat();
        JedisCluster jedisCluster = new JedisCluster(Set.of(new HostAndPort(connectInfo.host(), connectInfo.port())), connectInfo.user(), connectInfo.password());
        for (int i = 0; i <= 2000; i++) {
            jedisCluster.set("AAAAAAAA" + i, "ssdfFDSF4DS56123");
        }
        String s = jedis.get("AAAAAAAA");
        jedis.del("AAAAAAAA");
        //host info
        var hostInfo = new FlatLabel();
        hostInfo.setText(connectInfo.host() + ":" + connectInfo.port());
        hostInfo.setIcon(new FlatSVGIcon("icons/host.svg"));
        leftToolBar.add(hostInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);

        //host info
        var rightToolBar = new FlatToolBar();
        var rightToolBarLayout = new FlowLayout();
        rightToolBarLayout.setAlignment(FlowLayout.CENTER);
        rightToolBar.setLayout(rightToolBarLayout);

        //keysInfo
        var keysInfo = new FlatLabel();
        keysInfo.setText("60001");
        keysInfo.setIcon(new FlatSVGIcon("icons/key.svg"));
        rightToolBar.add(keysInfo);

        //cupInfo
        var cupInfo = new FlatLabel();
        cupInfo.setText("100%");
        cupInfo.setIcon(new FlatSVGIcon("icons/CPU.svg"));
        rightToolBar.add(cupInfo);

        //memoryInfo
        var memoryInfo = new FlatLabel();
        memoryInfo.setText("825.26K");
        memoryInfo.setIcon(new FlatSVGIcon("icons/memory.svg"));
        rightToolBar.add(memoryInfo);
        contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, rightToolBar);

        contentPanel.addTab("数据", new FlatSVGIcon("icons/db_key2.svg"), new _DashboardForm().getContentPanel());
        contentPanel.addTab("命令", new FlatSVGIcon("icons/db_cli2.svg"), new TerminalComponent());
        contentPanel.addTab("信息", new FlatSVGIcon("icons/db_report2.svg"), new _DatabaseForm().getContentPanel());

        contentPanel.addChangeListener(e -> {
            var tabbedPane = (JTabbedPane) e.getSource();
            var component = tabbedPane.getSelectedComponent();
            if (component instanceof TerminalComponent terminalComponent) {

            }
        });
        add(contentPanel, BorderLayout.CENTER);
    }


}
