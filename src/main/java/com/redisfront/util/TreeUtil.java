package com.redisfront.util;

import com.redisfront.model.TreeNodeInfo;

import javax.swing.tree.DefaultTreeModel;
import java.io.Serial;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtil {

    private TreeUtil() {
    }

    public static DefaultTreeModel toTreeModel(Set<String> rows, String delim) {
        var rootNode = new TreeNodeInfo();
        var stringTreeMap = toStringTreeMap(rows, delim);
        var treeNodeInfos = convertTreeNodeInfoSet(stringTreeMap, "");
        treeNodeInfos.stream().sorted(Comparator.reverseOrder()).forEach(rootNode::add);
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
     * @param stringTreeMap StringTreeMap
     * @return Set<TreeNodeInfo>
     */
    public static Set<TreeNodeInfo> convertTreeNodeInfoSet(StringTreeMap stringTreeMap, String parentKey) {
        return stringTreeMap.entrySet().stream().parallel().map(treeMapEntry -> {
            //完整的KeyName
            var fullKeyName = (FunUtil.isEmpty(parentKey) ? "" : parentKey.concat(":")).concat(treeMapEntry.getKey());
            var treeNodeInfo = new TreeNodeInfo(treeMapEntry.getKey(), fullKeyName);
            //递归查找下级
            convertTreeNodeInfoSet(treeMapEntry.getValue(), fullKeyName)
                    .stream()
                    .parallel()
                    .forEach(treeNodeInfo::add);
            return treeNodeInfo;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static class StringTreeMap extends TreeMap<String, StringTreeMap> {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public Comparator<? super String> comparator() {
            return Comparator.reverseOrder();
        }
    }


}
