package cn.devcms.redisfront.core.tree;

import cn.devcms.redisfront.core.constant.ItemTreeTypeEnum;
import cn.devcms.redisfront.core.constant.ItemValueTypeEnum;
import lombok.Data;

import javax.swing.tree.DefaultMutableTreeNode;

@Data
public class DatabaseItemTreeNode extends DefaultMutableTreeNode {

    private String name;
    private ItemTreeTypeEnum itemTreeTypeEnum;
    private ItemValueTypeEnum itemValueTypeEnum;
    private Long length;
    private String dataSize;

}
