package org.dromara.redisfront.ui.widget.left.tree;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.dromara.redisfront.model.entity.ConnectGroupEntity;

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
public class RedisConnectTreeNode extends DefaultMutableTreeNode implements Comparator<TreeNode> {

    private Boolean isGroup;
    private ConnectGroupEntity group;
    private ConnectDetailEntity detail;

    public RedisConnectTreeNode(ConnectGroupEntity group) {
        this.isGroup = true;
        this.group = group;
    }

    public RedisConnectTreeNode(ConnectDetailEntity detail) {
        this.isGroup = false;
        this.detail = detail;
    }

    public Integer id() {
        if (isGroup) {
            return group.getGroupId();
        }
        return detail.getGroupId();
    }

    @Override
    public String toString() {
        if (isGroup) {
            return group.getGroupName();
        }
        return detail.getName();
    }

    @Override
    public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof RedisConnectTreeNode o1Node && o2 instanceof RedisConnectTreeNode o2Node) {
            return o1Node.id().compareTo(o2Node.id());
        }
        return 0;
    }
}
