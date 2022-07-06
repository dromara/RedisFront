package com.redisfront.util;

import com.redisfront.model.TreeNodeInfo;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * TreeUtil
 *
 * @author Jin
 */
public class TreeUtilTest {

    @Test
    public  void test1(){
        Set<String> rows = new HashSet<>();
        rows.add("1:2:3.4");
        rows.add("1:2.2");
        rows.add("8");
        Set<TreeNodeInfo> treeNodeInfos = TreeUtil.convertTreeNodeInfoSet(TreeUtil.toStringTreeMap(rows, ":"), "");
        System.out.println();
    }

}
