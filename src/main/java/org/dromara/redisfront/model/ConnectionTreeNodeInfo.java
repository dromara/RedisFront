package org.dromara.redisfront.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;

/**
 * NodeInfo
 *
 * @author Jin
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
public class ConnectionTreeNodeInfo extends DefaultMutableTreeNode implements Comparator<TreeNode> {

    private String title;
    private String host;
    private Boolean isFolder;

    @Override
    public String toString() {
        return title;
    }

    @Override
    public int compare(TreeNode o1, TreeNode o2) {
        return 0;
    }
}
