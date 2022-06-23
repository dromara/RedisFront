package com.redisfront.util;

import com.redisfront.model.TreeNodeInfo;

import java.util.HashSet;
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
        Set<TreeNodeInfo> treeNodeInfos = TreeUtil.convertTreeNodeInfoList(TreeUtil.toStringTreeMap(rows, ":"), "");
        System.out.println();
    }

}
