package org.dromara.redisfront.commons.utils;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatLaf;
import com.surelogic.Utility;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.tree.QSTreeNode;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;


/**
 * 常用函数
 *
 * @author Jin
 */
@Slf4j
@Utility
public class RedisFrontUtils {


    public static boolean isNotEmpty(Collection<?> collection) {
        return CollectionUtil.isNotEmpty(collection);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapUtil.isNotEmpty(map);
    }

    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    public static boolean isNotEmpty(final Object array) {
        return ArrayUtil.isNotEmpty(array);
    }

    public static boolean isNotNull(Object obj) {
        return ObjectUtil.isNotNull(obj);
    }

    public static boolean isNull(Object obj) {
        return ObjectUtil.isNull(obj);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtil.isEmpty(collection);
    }

    public static <T> boolean isEmpty(T[] array) {
        return ArrayUtil.isEmpty(array);
    }

    public static <T> T[] isEmpty(T[] array, T[] defaultValue) {
        return ArrayUtil.isEmpty(array) ? defaultValue : array;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtil.isEmpty(map);
    }

    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    public static boolean equal(Object obj1, Object obj2) {
        return ObjectUtil.equal(obj1, obj2);
    }

    public static boolean notEqual(Object obj1, Object obj2) {
        return ObjectUtil.notEqual(obj1, obj2);
    }

    public static boolean startWith(String str, String prefix) {
        return StrUtil.startWith(str, prefix);
    }

    public static boolean endsWith(String str, String suffix) {
        return StrUtil.endWith(str, suffix);
    }

    public static String toJson(Object obj) {
        return JSONUtil.parse(obj).toStringPretty();
    }


    public static int getByteSize(Object data) {
        if (data instanceof String) {
            return ((String) data).getBytes(StandardCharsets.UTF_8).length;
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(data);
            outputStream.flush();
            return byteArrayOutputStream.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getDataSize(String str) {
        if (isNotEmpty(str)) {
            return DataSizeUtil.format(str.getBytes().length);
        }
        return DataSizeUtil.format(0);
    }


    public static void revalidateAndRepaintAllFramesAndDialogs() {
        FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
    }

    public static void removeAllComponent(JComponent component) {
        for (Component c : component.getComponents()) {
            component.remove(c);
        }
        revalidateAndRepaintAllFramesAndDialogs();
    }

    public static java.util.List<Object> saveExpandedPaths(JXTree tree) {
        java.util.List<Object> savedPaths = new ArrayList<>();
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

    public static void runEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

}
