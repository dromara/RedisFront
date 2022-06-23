package com.redisfront.util;

import com.redisfront.model.TreeNodeInfo;

import javax.swing.tree.DefaultTreeModel;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtil {

    public static DefaultTreeModel toTreeModel(Set<String> rows, String delim) {
        var rootNode = new TreeNodeInfo();
        var stringTreeMap = toStringTreeMap(rows, delim);
        var treeNodeInfos = convertTreeNodeInfoList(stringTreeMap, "");
        treeNodeInfos.forEach(rootNode::add);
        return new DefaultTreeModel(rootNode);
    }

    /**
     * 字符串集合转 StringTreeMap
     *
     * @param rows  字符串集合
     * @param delim 分隔符
     * @return StringTreeMap
     */
    public static StringTreeMap toStringTreeMap(Set<String> rows, String delim) {
        var root = new StringTreeMap();
        rows.stream().parallel().forEach(row -> {
            var n = root;
            var cells = row.split(delim);
            for (var cell : cells) {
                var child = n.get(cell);
                if (child == null) {
                    n.put(cell, child = new StringTreeMap());
                }
                n = child;
            }
        });
        return root;
    }


    /**
     * 递归转换TreeNode集合
     *
     * @param m StringTreeMap
     * @return Set<TreeNodeInfo>
     */
    public static Set<TreeNodeInfo> convertTreeNodeInfoList(StringTreeMap m, String parentKey) {
        Set<TreeNodeInfo> treeNodeInfos = new HashSet<>();
        m.entrySet().stream().parallel().forEach(treeMapEntry -> {
            var fullKey = (Fn.isEmpty(parentKey) ? "" : parentKey.concat(":")) + treeMapEntry.getKey();
            var treeNodeInfo = new TreeNodeInfo(treeMapEntry.getKey(), fullKey);
            convertTreeNodeInfoList(treeMapEntry.getValue(), fullKey).stream().parallel().forEach(treeNodeInfo::add);
            treeNodeInfos.add(treeNodeInfo);
        });
        return treeNodeInfos;
    }

    static class StringTreeMap extends TreeMap<String, StringTreeMap> {
        @Serial
        private static final long serialVersionUID = 1L;
    }


}
