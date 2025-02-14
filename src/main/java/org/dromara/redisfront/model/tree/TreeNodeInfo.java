package org.dromara.redisfront.model.tree;

import cn.hutool.core.io.unit.DataSizeUtil;
import lombok.Getter;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;

/**
 * NodeInfo
 *
 * @author Jin
 */

@Getter
public class TreeNodeInfo extends DefaultMutableTreeNode implements Comparator<TreeNode> {

    private String title;
    private String key;
    private Integer memorySize;
    private String formattedMemory;
    private Boolean isLeafNode;

    public TreeNodeInfo() {
        super();
    }

    public TreeNodeInfo(String title, String key, Boolean isLeafNode) {
        this.title = title;
        this.key = key;
        this.isLeafNode = isLeafNode;
    }

    public Integer memorySize() {
        return RedisFrontUtils.isNull(memorySize) ? 0 : memorySize;
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

    @Override
    public int compare(TreeNode o1, TreeNode o2) {
        return ((TreeNodeInfo) o1).title.compareToIgnoreCase(((TreeNodeInfo) o2).title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNodeInfo that)) return false;
        return Objects.equals(key, that.key) &&
                Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, title);
    }

    public void setMemorySize(Integer memorySize) {
        this.memorySize = memorySize;
        this.formattedMemory = memorySize != null ? DataSizeUtil.format(memorySize) : "";
    }

    private void traverseChildren(TreeNode node, int[] result) {
        Enumeration<? extends TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            TreeNodeInfo child = (TreeNodeInfo) children.nextElement();
            if (child.getChildCount() > 0) {
                traverseChildren(child, result);
            } else {
                result[0] += child.memorySize();
                result[1]++;
            }
        }
    }

    @Override
    public String toString() {
        int childCount = getChildCount();
        int level = getLevel();

        if (childCount == 0) {
            return baseTitleWithMemory();
        }
        int[] stats = calculateChildStats();
        return buildTitleString(level, childCount, stats);
    }

    private String baseTitleWithMemory() {
        return title + (memorySize() == 0 ? "" : " [ " + formattedMemory + " ]  ");
    }

    private int[] calculateChildStats() {
        int[] result = new int[2];
        traverseChildren(this, result);
        return result;
    }

    private String buildTitleString(int level, int directChildren, int[] stats) {
        String countInfo = (level > 1 ? directChildren : stats[1]) + "";
        String memoryInfo = stats[0] == 0 ? "" : " [ " + DataSizeUtil.format(stats[0]) + " ]  ";
        return String.format("%s (%s) %s", title, countInfo, memoryInfo);
    }
}
