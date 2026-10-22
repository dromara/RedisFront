package org.dromara.redisfront.model.tree;

import cn.hutool.core.io.unit.DataSizeUtil;
import lombok.Getter;
import org.dromara.quickswing.tree.QSTreeNode;
import org.dromara.redisfront.model.MemoryUsageResult;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * NodeInfo
 *
 * @author Jin
 */

@Getter
public class TreeNodeInfo extends DefaultMutableTreeNode implements Comparator<TreeNode>, QSTreeNode<String> {

    private String title;
    private String key;
    private MemoryUsageResult memoryUsage = MemoryUsageResult.state(MemoryUsageResult.Status.UNANALYZED);
    private transient MemoryStats cachedMemoryStats;
    private Boolean isLeafNode;

    public TreeNodeInfo() {
        super();
    }

    public TreeNodeInfo(String title, String key, Boolean isLeafNode) {
        this.title = title;
        this.key = key;
        this.isLeafNode = isLeafNode;
    }

    public long memorySize() { return memoryUsage.bytes() == null ? 0 : memoryUsage.bytes(); }
    public Long getMemorySize() { return memoryUsage.bytes(); }

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
                Objects.equals(title, that.title) &&
                Objects.equals(isLeafNode, that.isLeafNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, title);
    }

    public void setMemorySize(long memorySize) { setMemoryUsage(MemoryUsageResult.measured(memorySize)); }

    public void setMemoryUsage(MemoryUsageResult result) {
        memoryUsage = Objects.requireNonNull(result);
        invalidateMemoryStats();
    }

    private void invalidateMemoryStats() {
        for (TreeNodeInfo node = this; node != null; node = node.getParent() instanceof TreeNodeInfo parent ? parent : null) {
            node.cachedMemoryStats = null;
        }
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        super.insert(child, index);
        invalidateMemoryStats();
    }

    @Override
    public void remove(int index) {
        super.remove(index);
        invalidateMemoryStats();
    }

    @Override
    public void removeAllChildren() {
        super.removeAllChildren();
        invalidateMemoryStats();
    }

    public record MemoryStats(long bytes, int keys, int measured, int missing, int failed, int unsupported,
                              int cancelled, int pending, long timestamp) {
        private MemoryStats plus(MemoryStats other) {
            return new MemoryStats(Math.addExact(bytes, other.bytes), keys + other.keys, measured + other.measured,
                    missing + other.missing, failed + other.failed, unsupported + other.unsupported,
                    cancelled + other.cancelled, pending + other.pending, Math.max(timestamp, other.timestamp));
        }
        public int unfinished() { return keys - measured - missing - failed - unsupported; }
    }

    public MemoryStats memoryStats() {
        if (cachedMemoryStats != null) return cachedMemoryStats;
        var state = memoryUsage.status();
        boolean keyNode = Boolean.TRUE.equals(isLeafNode);
        var stats = new MemoryStats(keyNode ? memorySize() : 0, keyNode ? 1 : 0,
                keyNode && state == MemoryUsageResult.Status.COMPLETE ? 1 : 0,
                keyNode && state == MemoryUsageResult.Status.MISSING ? 1 : 0,
                keyNode && state == MemoryUsageResult.Status.FAILED ? 1 : 0,
                keyNode && state == MemoryUsageResult.Status.UNSUPPORTED ? 1 : 0,
                keyNode && state == MemoryUsageResult.Status.CANCELLED ? 1 : 0,
                keyNode && state == MemoryUsageResult.Status.ANALYZING ? 1 : 0, keyNode ? memoryUsage.timestamp() : 0);
        for (int i = 0; i < getChildCount(); i++) stats = stats.plus(((TreeNodeInfo) getChildAt(i)).memoryStats());
        return cachedMemoryStats = stats;
    }

    @Override
    public String toString() {
        if (getChildCount() == 0 && Boolean.TRUE.equals(isLeafNode)) {
            return switch (memoryUsage.status()) {
                case UNANALYZED -> title;
                case COMPLETE -> title + " [ " + formatMemorySize(memorySize()) + " ]";
                case ANALYZING -> title + " [ … ]";
                default -> title + " [ " + ResourceBundle.getBundle("org.dromara.redisfront.RedisFront")
                        .getString("Memory.state." + memoryUsage.status()) + " ]";
            };
        }
        var stats = memoryStats();
        String size = stats.measured() > 0 ? " [ " + formatMemorySize(stats.bytes())
                + (stats.measured() < stats.keys() ? " · " + stats.measured() + "/" + stats.keys() : "") + " ]"
                : stats.pending() > 0 ? " [ … ]" : stats.missing() + stats.failed() + stats.unsupported() + stats.cancelled() > 0
                ? " [ — · 0/" + stats.keys() + " ]" : "";
        return title + " (" + stats.keys() + ")" + size;
    }

    public static String formatMemorySize(long bytes) { return bytes == 0 ? "0 B" : DataSizeUtil.format(bytes); }

    @Override
    public String id() {
        return key;
    }
}
