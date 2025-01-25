package org.dromara.redisfront.commons.utils;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.tree.QSTreeNode;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class SwingTreeUtils {
    public static List<Object> saveExpandedPaths(JXTree tree) {
        List<Object> savedPaths = new ArrayList<>();
        TreePath rootPath = new TreePath(tree.getModel().getRoot());
        Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(rootPath);
        if (expandedPaths != null) {
            while (expandedPaths.hasMoreElements()) {
                TreePath path = expandedPaths.nextElement();
                if (path.getParentPath() != null) {
                    QSTreeNode<?> pathComponent = (QSTreeNode<?>) path.getLastPathComponent();
                    savedPaths.add(pathComponent.id());
                }
            }
        } else {
            log.warn("No expanded nodes.");
        }
        return savedPaths;
    }

    public static void restoreExpandedPaths(JTree tree, TreeModel model, List<Object> savedPaths) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
        for (Object id : savedPaths) {
            for (int i = 0; i < root.getChildCount(); i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
                TreePath treePath = new TreePath(node.getPath());
                QSTreeNode<?> data = (QSTreeNode<?>) treePath.getLastPathComponent();
                if (ObjectUtil.equals(data.id(), id)) {
                    tree.expandPath(treePath);
                    break;
                }
            }
        }
    }
}
