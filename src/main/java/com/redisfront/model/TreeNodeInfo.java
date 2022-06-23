package com.redisfront.model;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * NodeInfo
 *
 * @author Jin
 */

public class TreeNodeInfo extends DefaultMutableTreeNode {

    private String title;
    private String key;

    public TreeNodeInfo() {
    }

    public TreeNodeInfo(String title, String key) {
        this.title = title;
        this.key = key;
    }

    public String title() {
        return title;
    }

    public TreeNodeInfo setTitle(String title) {
        this.title = title;
        return this;
    }


    public String key() {
        return key;
    }

    public TreeNodeInfo setKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNodeInfo that = (TreeNodeInfo) o;

        return key.equals(that.key);
    }

    @Override
    public String toString() {
        return title;
    }
}
