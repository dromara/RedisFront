package org.dromara.redisfront.ui.widget.common;

import com.formdev.flatlaf.util.SystemInfo;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.Getter;
import org.dromara.redisfront.commons.constant.Res;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author jin
 */
@SuppressWarnings("all")
public class DefaultNonePanel extends JPanel {
    @Getter
    private static final DefaultNonePanel instance = new DefaultNonePanel();

    public DefaultNonePanel() {
        this.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        this.add(new Spacer(), new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        this.add(new Spacer(), new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        this.add(new Spacer(), new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        this.add(new Spacer(), new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        this.initializeUI();
    }

    public void initializeUI() {

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.add(controlPanel, new GridConstraints(1, 2, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        JLabel logoLabel = new JLabel(Res.REDIS_ICON);
        this.add(logoLabel, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));


        JPanel newOptPanel = new JPanel();
        newOptPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.add(newOptPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JLabel newLabel = new JLabel("添加连接");
        newLabel.putClientProperty("FlatLaf.styleClass", " h3");
        newOptPanel.add(newLabel);
        if(SystemInfo.isMacOS) {
            JLabel newCmdLabel = new JLabel(Res.COMMAND_ICON_16x16);
            newCmdLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
            newOptPanel.add(newCmdLabel);
        }else {
            JLabel newCmdLabel = new JLabel("Ctrl");
            newCmdLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
            newCmdLabel.putClientProperty("FlatLaf.styleClass", " h3");
            newOptPanel.add(newCmdLabel);
        }

        JLabel newKeyLabel = new JLabel("A");
        newKeyLabel.putClientProperty("FlatLaf.styleClass", " h3");
        newOptPanel.add(newKeyLabel);

        controlPanel.add(new Spacer(), new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));


        JPanel openOptPanel = new JPanel();
        openOptPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.add(openOptPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JLabel openLabel = new JLabel("打开连接");
        openLabel.putClientProperty("FlatLaf.styleClass", "h3");
        openOptPanel.add(openLabel);
        if(SystemInfo.isMacOS) {
            JLabel openCmdLabel = new JLabel(Res.COMMAND_ICON_16x16);
            openCmdLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
            openOptPanel.add(openCmdLabel);
        }else {
            JLabel openCmdLabel = new JLabel("Ctrl");
            openCmdLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
            openCmdLabel.putClientProperty("FlatLaf.styleClass", " h3");
            openOptPanel.add(openCmdLabel);
        }

        JLabel openKeyLabel = new JLabel("S");
        openKeyLabel.putClientProperty("FlatLaf.styleClass", " h3");
        openOptPanel.add(openKeyLabel);

        controlPanel.add(new Spacer(), new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

    }

}
