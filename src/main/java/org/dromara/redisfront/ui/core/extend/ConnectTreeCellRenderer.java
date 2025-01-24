package org.dromara.redisfront.ui.core.extend;

import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.redisfront.commons.constant.Icons;
import org.dromara.redisfront.ui.widget.left.tree.RedisConnectTreeNode;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class ConnectTreeCellRenderer extends DefaultXTreeCellRenderer {
    public ConnectTreeCellRenderer() {
        this.setTextNonSelectionColor(Color.WHITE);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (SystemInfo.isWindows) {
            this.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        }
        if (value instanceof RedisConnectTreeNode redisConnectTreeNode) {
            if (redisConnectTreeNode.getIsGroup()) {
                this.setIcon(Icons.FOLDER_ICON_14x14);
            } else {
                this.setIcon(Icons.LINK_ICON_14x14);
            }
        }
        return c;
    }
}