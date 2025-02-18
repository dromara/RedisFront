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
        rows.add("111: 111");
        rows.add("111: 111: 111");
        rows.add("111: 111: 000");
        rows.add("111: 111: BBB");
//        Set<TreeNodeInfo> treeNodeInfos = TreeUtils.convertTreeNodeInfoSet(TreeUtils.toStringTreeMap(rows, ":"), "",":");
        DefaultTreeModel treeModel = TreeUtils.toTreeModel(rows, ":");
        System.out.println();
    }

}
