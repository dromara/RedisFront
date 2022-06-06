package cn.devcms.redisfront.model;

import cn.devcms.redisfront.constant.ItemTreeTypeEnum;
import cn.devcms.redisfront.constant.ItemValueTypeEnum;
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
