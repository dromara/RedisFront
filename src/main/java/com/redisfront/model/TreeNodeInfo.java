package com.redisfront.model;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;

/**
 * NodeInfo
 *
 * @author Jin
 */

public class TreeNodeInfo extends DefaultMutableTreeNode implements Comparator<TreeNode> {

    private String title;
    private String key;

    public TreeNodeInfo() {
        super();
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
    public int compare(TreeNode o1, TreeNode o2) {
        return ((TreeNodeInfo) o1).title.compareToIgnoreCase(((TreeNodeInfo) o2).title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNodeInfo that = (TreeNodeInfo) o;

        return title.equals(that.title);
    }

    @Override
    public String toString() {
        if (getChildCount() > 0) {
            return title + " (" + getChildCount() + ") ";
        } else {
            return title;
        }
    }
}
