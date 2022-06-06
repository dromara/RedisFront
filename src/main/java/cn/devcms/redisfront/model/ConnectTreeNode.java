package cn.devcms.redisfront.model;

import lombok.Data;

import javax.swing.tree.DefaultMutableTreeNode;

@Data
public class ConnectTreeNode extends DefaultMutableTreeNode {

    private String icon;
    private String name;
    private Boolean clusterMode;
    private String host = "127.0.0.1";
    private Integer port = 6378;
    private String password;

    public ConnectTreeNode() {
        if (clusterMode) {
            DatabaseTreeNode databaseTreeNode = new DatabaseTreeNode();
            databaseTreeNode.setName("db" + 0);
            databaseTreeNode.setIndex(0);
            children.add(databaseTreeNode);
        } else {
            for (int i = 0; i < 16; i++) {
                DatabaseTreeNode databaseTreeNode = new DatabaseTreeNode();
                databaseTreeNode.setName("db" + i);
                databaseTreeNode.setIndex(i);
                children.add(databaseTreeNode);
            }

        }
    }
}
