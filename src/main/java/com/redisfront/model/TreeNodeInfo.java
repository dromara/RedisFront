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
        var l = getLevel();
        if (getChildCount() > 0 && getLevel() > 1) {
            return title + " (" + getChildCount() + ") ";
        } else if (getChildCount() > 0 && getLevel() == 1) {
            var count = getAllChildCount(this);
            return title + " (" + count + ") ";
        } else {
            return title;
        }
    }

    public int getAllChildCount(TreeNode treeNode) {
        int tmp = 0;
        if (treeNode.getChildCount() > 0) {
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                TreeNode subTreeNode = treeNode.getChildAt(i);
                if (subTreeNode.getChildCount() > 0) {
                    tmp += getAllChildCount(subTreeNode);
                } else {
                    tmp += 1;
                }
            }
        } else {
            tmp += 1;
        }
        return tmp;
    }
}
