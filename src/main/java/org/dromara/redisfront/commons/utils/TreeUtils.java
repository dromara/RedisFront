package org.dromara.redisfront.commons.utils;

import lombok.Getter;
import org.dromara.redisfront.model.tree.TreeNodeInfo;

import javax.swing.tree.DefaultTreeModel;
import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtils {

    private TreeUtils() {
    }

    public static DefaultTreeModel toTreeModel(Set<String> rows, String delim) {
        var rootNode = new TreeNodeInfo();
        var stringTreeMap = toStringTreeMap(rows, delim);
        var treeNodeInfos = convertTreeNodeInfoSet(stringTreeMap, "");
        treeNodeInfos.forEach(rootNode::add);
        return new DefaultTreeModel(rootNode);
    }

    public static StringTreeMap toStringTreeMap(Set<String> rows, String delim) {
        StringTreeMap root = new StringTreeMap();
        for (String row : rows) {
            String[] cells = splitKey(row, delim);
            StringTreeMap node = root;
            // 处理末尾分隔符
            if (row.endsWith(delim)) {
                cells[cells.length - 1] += delim;
            }
            for (int i = 0; i < cells.length; i++) {
                String cell = cells[i];
                boolean isLeaf = (i == cells.length - 1);
                StringTreeMap child = node.get(cell);
                if (child == null) {
                    child = new StringTreeMap();
                    if (isLeaf) {
                        child.markLeafNode();
                    }
                    node.put(cell, child);
                }
                node = child;
            }
        }
        return root;
    }

    private static String[] splitKey(String row, String delim) {
        List<String> parts = new ArrayList<>();
        int delimLen = delim.length();
        int pos;

        while ((pos = row.indexOf(delim)) != -1) {
            parts.add(row.substring(0, pos));
            row = row.substring(pos + delimLen);
        }
        parts.add(row);
        return parts.toArray(new String[0]);
    }

    /**
     * 递归转换TreeNode集合
     *
     * @param stringTreeMap StringTreeMap
     * @param parentKey parentKey
     * @return Set<TreeNodeInfo>
     */
    public static Set<TreeNodeInfo> convertTreeNodeInfoSet(StringTreeMap stringTreeMap, String parentKey) {
        return stringTreeMap.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey().replace("->!N!", "");
                    StringTreeMap value = entry.getValue();
                    StringBuilder sb = new StringBuilder(parentKey.length() + key.length() + 1);
                    if (!RedisFrontUtils.isEmpty(parentKey)) {
                        sb.append(parentKey).append(':');
                    }
                    sb.append(key);
                    String fullKeyName = sb.toString();
                    TreeNodeInfo node = new TreeNodeInfo(key, fullKeyName, value.isLeafNode);
                    convertTreeNodeInfoSet(value, fullKeyName).forEach(node::add);
                    return node;
                })
                .sorted(Comparator.comparing(TreeNodeInfo::key))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    @Getter
    public static class StringTreeMap extends LinkedHashMap<String, StringTreeMap> {
        @Serial
        private static final long serialVersionUID = 1L;

        private transient boolean isLeafNode = false;

        public void markLeafNode() {
            this.isLeafNode = true;
        }
    }


}
