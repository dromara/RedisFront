package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.widget.main.MainWidget;

import javax.swing.*;
import java.awt.*;

public class MainFooterBar extends JPanel{

    private final MainWidget owner;

    public MainFooterBar(MainWidget owner) {
        this.owner = owner;
        Box horizontalBox = Box.createVerticalBox();
        horizontalBox.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
        horizontalBox.add(new JSeparator());
        var rightToolBar = new FlatToolBar();
        rightToolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        var version = new JLabel();
        version.setText(Const.APP_VERSION);
        RedisFrontContext context = (RedisFrontContext) owner.getContext();
        version.setToolTipText("Current Version " + context.version());
        version.setIcon(UI.REDIS_TEXT_80x16);
        rightToolBar.add(version);
        horizontalBox.add(rightToolBar);
        add(horizontalBox);
    }
}
