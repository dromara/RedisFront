package com.redisfront.ui.form;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataSearchForm {
    private JPanel contentPanel;
    private JTree keyTree;
    private JComboBox comboBox1;
    private JTextField textField1;
    private JComboBox databaseComboBox;
    private JButton addBtn;
    private JPanel treePanel;
    private JButton refreshBtn;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public static DataSearchForm newInstance() {
        return new DataSearchForm();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPanel.setLayout(new BorderLayout(0, 0));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        contentPanel.add(panel1, BorderLayout.NORTH);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        comboBox1 = new JComboBox();
        panel2.add(comboBox1, BorderLayout.WEST);
        addBtn.setHorizontalAlignment(0);
        addBtn.setHorizontalTextPosition(11);
        addBtn.setText("新增");
        panel2.add(addBtn, BorderLayout.CENTER);
        refreshBtn.setText("");
        panel2.add(refreshBtn, BorderLayout.EAST);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel1.add(panel3, BorderLayout.SOUTH);
        textField1 = new JTextField();
        panel3.add(textField1, BorderLayout.CENTER);
        treePanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(treePanel, BorderLayout.CENTER);
        keyTree = new JTree();
        treePanel.add(keyTree, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

    private void createUIComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 0));
        addBtn = new JButton();
//        addBtn.setIcon(new FlatSVGIcon("icons/add.svg"));
        refreshBtn = new JButton();
        refreshBtn.setIcon(new FlatSVGIcon("icons/refresh.svg"));
        databaseComboBox = new JComboBox();
        treePanel = new JPanel();
        treePanel.setBorder(new EmptyBorder(3, 2, 2, 2));
        keyTree = new JTree();
        keyTree.setBorder(new EmptyBorder(5, 5, 5, 5));


    }
}