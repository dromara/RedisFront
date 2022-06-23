package com.redisfront.util;

import com.redisfront.model.TreeNodeInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtilTest {

    public static void main(String[] args) {
        Set<String> rows = new HashSet<>();
        rows.add("1:2:3.4");
        rows.add("1:2.2");
        rows.add("8");
        List<TreeNodeInfo> treeNodeInfos = TreeUtil.convertTreeNodeInfo(TreeUtil.toStringTreeMap(rows, ":"));
        System.out.println();
    }

}
