package cn.devcms.redisfront.core.tree;

import lombok.Data;

import javax.swing.tree.DefaultMutableTreeNode;


@Data
public class DatabaseTreeNode extends DefaultMutableTreeNode {

    private String name;
    private String icon;
    private Integer index;
    private Long length;



}
