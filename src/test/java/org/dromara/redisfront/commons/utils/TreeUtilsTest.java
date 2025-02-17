package org.dromara.redisfront.commons.utils;

import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultTreeModel;
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
        rows.add("A:0: 456");
        rows.add("A:0");
//        Set<TreeNodeInfo> treeNodeInfos = TreeUtils.convertTreeNodeInfoSet(TreeUtils.toStringTreeMap(rows, ":"), "",":");
        DefaultTreeModel treeModel = TreeUtils.toTreeModel(rows, ":");
        System.out.println();
    }

}
