package com.redisfront.model;

import cn.hutool.core.io.unit.DataSizeUtil;
import com.redisfront.commons.func.Fn;

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
    private Integer memorySize;

    public Integer memorySize() {
        return Fn.isNull(memorySize) ? 0 : memorySize;
    }

    public TreeNodeInfo setMemorySize(Integer memorySize) {
        this.memorySize = memorySize;
        return this;
    }

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

        if (getChildCount() > 0 && getLevel() > 1) {
            var memory = getAllChildMemory(this);
            return title + " (" + getChildCount() + ") " + (memory == 0 ? "" : " [ " + DataSizeUtil.format(memory) + " ]  ");
        } else if (getChildCount() > 0 && getLevel() == 1) {
            var count = getAllChildCount(this);
            var memory = getAllChildMemory(this);
            return title + " (" + count + ") " + (memory == 0 ? "" : " [ " + DataSizeUtil.format(memory) + " ]  ");
        } else {
            return title + (memorySize() == 0 ? "" : " [ " + DataSizeUtil.format(memorySize()) + " ]  ");
        }
    }

    public int getAllChildMemory(TreeNode treeNode) {
        int tmp = 0;
        if (treeNode.getChildCount() > 0) {
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                var subTreeNode = (TreeNodeInfo) treeNode.getChildAt(i);
                if (subTreeNode.getChildCount() > 0) {
                    tmp += getAllChildMemory(subTreeNode);
                } else {
                    tmp += subTreeNode.memorySize();
                }
            }
        }
        return tmp;
    }

    public int getAllChildCount(TreeNode treeNode) {
        int tmp = 0;
        if (treeNode.getChildCount() > 0) {
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                var subTreeNode = treeNode.getChildAt(i);
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
