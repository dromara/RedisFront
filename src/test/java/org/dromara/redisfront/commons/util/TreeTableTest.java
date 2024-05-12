package org.dromara.redisfront.commons.util;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Arrays;

public class TreeTableTest {


    static class Bean {
        String name;
        int age;

        public Bean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        // 创建树形数据  
        DefaultMutableTreeTableNode root = new DefaultMutableTreeTableNode();
        DefaultMutableTreeTableNode child1 = new DefaultMutableTreeTableNode(new Bean("小明爸爸", 40));
        root.add(child1);
        DefaultMutableTreeTableNode child2 = new DefaultMutableTreeTableNode(new Bean("小明", 10));
        root.add(child2);
        DefaultMutableTreeTableNode grandChild = new DefaultMutableTreeTableNode(new Bean("小刚", 33));
        child1.add(grandChild);

        Object[] columnIdentifiers = {"name", "age"};

        TreeTableModel model = new DefaultTreeTableModel(root, Arrays.stream(columnIdentifiers).toList()) {
            @Override
            public Object getValueAt(Object node, int column) {
                if (node instanceof DefaultMutableTreeTableNode treeNode) {
                    Object userObject = treeNode.getUserObject();
                    if (userObject instanceof Bean bean) {
                        return switch (column) {
                            case 0 -> bean.getName();
                            case 1 -> bean.getAge();
                            default -> throw new IllegalArgumentException("Invalid column index");
                        };
                    }
                }
                return null;
            }
        };

        JXTreeTable treeTable = new JXTreeTable(model);

        JFrame frame = new JFrame("测试");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(treeTable));
        frame.pack();
        frame.setVisible(true);
    }
}