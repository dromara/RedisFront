package org.dromara.redisfront.model;

import cn.hutool.db.Entity;
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
public class RedisConnectTreeItem extends DefaultMutableTreeNode implements Comparator<TreeNode> {

    private Boolean isGroup;
    private Entity origin;

    public Integer id() {
        if (isGroup) {
            return origin.getInt("group_id");
        }
        return origin.getInt("id");
    }

    @Override
    public String toString() {
        if (isGroup) {
            return origin.getStr("group_name");
        }
        return origin.getStr("name");
    }

    @Override
    public int compare(TreeNode o1, TreeNode o2) {
        if (o1 instanceof RedisConnectTreeItem o1Node && o2 instanceof RedisConnectTreeItem o2Node) {
            return o1Node.id().compareTo(o2Node.id());
        }
        return 0;
    }
}
