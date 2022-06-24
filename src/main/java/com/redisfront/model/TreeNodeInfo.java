package com.redisfront.model;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Locale;

/**
 * NodeInfo
 *
 * @author Jin
 */

public class TreeNodeInfo extends DefaultMutableTreeNode implements Comparable<TreeNodeInfo> {

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

        return title.equals(that.title);
    }

    @Override
    public String toString() {
        return title;
    }


    @Override
    public int compareTo(TreeNodeInfo treeNodeInfo) {
        return treeNodeInfo.title.compareTo(title);
    }
}
