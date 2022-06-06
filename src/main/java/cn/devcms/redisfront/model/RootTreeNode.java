package cn.devcms.redisfront.model;

import lombok.Data;

import javax.swing.tree.DefaultMutableTreeNode;

@Data
public class RootTreeNode extends DefaultMutableTreeNode {

    private String icon;
    private String name;
    private Integer sortNum;

}
