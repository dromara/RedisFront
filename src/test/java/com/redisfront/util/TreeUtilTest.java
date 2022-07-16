package com.redisfront.util;

import com.redisfront.commons.util.TreeUtil;
import com.redisfront.model.TreeNodeInfo;
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtilTest {

    @Test
    public void test1() {
        Set<String> rows = new HashSet<>();
        rows.add("A:0:1");
        rows.add("A:0");
        Set<TreeNodeInfo> treeNodeInfos = TreeUtil.convertTreeNodeInfoSet(TreeUtil.toStringTreeMap(rows, ":"), "");
        System.out.println();
    }

}
