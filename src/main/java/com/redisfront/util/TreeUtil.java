package com.redisfront.util;

import com.redisfront.constant.NodeTypeEnum;
import com.redisfront.model.TreeNodeInfo;

import java.io.Serial;
import java.util.*;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtil {

    public static List<TreeNodeInfo> toTreeNodeInfoList(Set<String> rows, String delim) {
        StringTreeMap stringTreeMap = toStringTreeMap(rows,  delim);
        return convertTreeNodeInfo(stringTreeMap);
    }

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

    public static List<TreeNodeInfo> convertTreeNodeInfo(StringTreeMap m) {
        List<TreeNodeInfo> treeNodeInfos = new ArrayList<>();
        m.entrySet().stream().parallel().forEach(treeMapEntry -> {
            var treeNodeInfo = new TreeNodeInfo(treeMapEntry.getKey(), treeMapEntry.getKey());
            convertTreeNodeInfo(treeMapEntry.getValue()).stream().parallel().forEach(treeNodeInfo::add);
            treeNodeInfos.add(treeNodeInfo);
        });
        return treeNodeInfos;
    }

    static class StringTreeMap extends TreeMap<String, StringTreeMap> {
        @Serial
        private static final long serialVersionUID = 1L;
    }


}
