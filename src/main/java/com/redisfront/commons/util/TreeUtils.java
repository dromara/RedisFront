package com.redisfront.commons.util;

import cn.hutool.core.util.StrUtil;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.TreeNodeInfo;

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

    public static synchronized DefaultTreeModel toTreeModel(Set<String> rows, String delim) {
        var rootNode = new TreeNodeInfo();
        var stringTreeMap = toStringTreeMap(rows, delim);
        var treeNodeInfos = convertTreeNodeInfoSet(stringTreeMap, "");
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
        for (var row : rows.stream().parallel().sorted().toList()) {

            var node = root;
            var cells = row.split(delim);

            var keyLength = (StrUtil.join("", (Object) cells).toCharArray().length + cells.length - 1);

            if (row.contains(delim) && row.toCharArray().length - keyLength >= 2) {
                var lastStr = StrUtil.sub(row, keyLength, row.toCharArray().length - 1);
                ArrayList<String> tmpList = new ArrayList<>(Arrays.asList(cells));
                tmpList.add(lastStr);
                cells = tmpList.toArray(new String[]{});
            } else if (Fn.endsWith(row, delim)) {
                cells[cells.length - 1] = cells[cells.length - 1].concat(delim);
            }

            for (int i = 0; i < cells.length; i++) {
                String cell = cells[i];
                var child = node.get(cell);
                if (child == null) {
                    if (i == cells.length - 1) {
                        cell += "->!N!";
                    }
                    node.put(cell, child = new StringTreeMap());
                }
                node = child;
            }
        }
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
            String key = treeMapEntry.getKey().replace("->!N!", "");
            //完整的KeyName
            var fullKeyName = (Fn.isEmpty(parentKey) ? "" : parentKey.concat(":")).concat(key);
            var treeNodeInfo = new TreeNodeInfo(key, fullKeyName);
            //递归查找下级
            convertTreeNodeInfoSet(treeMapEntry.getValue(), fullKeyName)
                    .forEach(treeNodeInfo::add);
            return treeNodeInfo;
        }).sorted(Comparator.comparing(TreeNodeInfo::key)).collect(Collectors.toCollection(LinkedHashSet::new));
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
