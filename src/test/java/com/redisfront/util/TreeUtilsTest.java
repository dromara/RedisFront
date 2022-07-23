package com.redisfront.util;

import com.redisfront.commons.util.TreeUtils;
import com.redisfront.model.TreeNodeInfo;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtilsTest {

    @Test
    public void test1() {
        Set<String> rows = new HashSet<>();
        rows.add("A:0:1");
        rows.add("A:0");
        Set<TreeNodeInfo> treeNodeInfos = TreeUtils.convertTreeNodeInfoSet(TreeUtils.toStringTreeMap(rows, ":"), "");
        System.out.println();
    }

}
