package com.redisfront.ui.form.fragment;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.redisfront.commons.Handler.ProcessHandler;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.ExecutorUtil;
import com.redisfront.commons.util.LettuceUtil;
import com.redisfront.commons.util.TreeUtil;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.TreeNodeInfo;
import com.redisfront.ui.dialog.AddKeyDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * DataSearchForm
 *
 * @author Jin
 */
public class DataSearchForm {
    private JPanel contentPanel;
    private JTree keyTree;
    private JTextField searchTextField;
    private JComboBox<Integer> databaseComboBox;
    private JButton addBtn;
    private JPanel treePanel;
    private JButton refreshBtn;
    private JPanel borderPanel;

    private ProcessHandler<TreeNodeInfo> nodeClickProcessHandler;

    private final ConnectInfo connectInfo;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public static DataSearchForm newInstance(ConnectInfo connectInfo) {
        return new DataSearchForm(connectInfo);
    }

    public DataSearchForm(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        $$$setupUI$$$();
        if (connectInfo.redisModeEnum() == Enum.RedisMode.CLUSTER)
            databaseComboBox.setEnabled(false);
        loadTreeModelData("*");
    }

    public void setNodeClickProcessHandler(ProcessHandler<TreeNodeInfo> nodeClickProcessHandler) {
        this.nodeClickProcessHandler = nodeClickProcessHandler;
    }

    public void loadTreeModelData(String key) {
        ExecutorUtil.runAsync(() -> {
            if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
                LettuceUtil.clusterRun(connectInfo, redisCommands -> {
                    var list = redisCommands.keys(key);
                    var treeModel = TreeUtil.toTreeModel(new HashSet<>(list), ":");
                    SwingUtilities.invokeLater(() -> keyTree.setModel(treeModel));
                });
            } else {
                LettuceUtil.run(connectInfo, redisCommands -> {
                    var list = redisCommands.keys(key);
                    var treeModel = TreeUtil.toTreeModel(new HashSet<>(list), ":");
                    SwingUtilities.invokeLater(() -> keyTree.setModel(treeModel));
                });
            }
        });
    }

    public void searchActionPerformed() {
        if (Fn.isEmpty(searchTextField.getText())) {
            loadTreeModelData("*");
        } else {
            loadTreeModelData(searchTextField.getText());
        }
    }

    public void deleteActionPerformed() {
        DefaultTreeModel treeModel = (DefaultTreeModel) keyTree.getModel();
        var selectNode = keyTree.getLastSelectedPathComponent();
        if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
            if (treeNodeInfo.getParent() != null) {
                treeModel.removeNodeFromParent(treeNodeInfo);
            }
        }
    }


    private void createUIComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(0, 5, 0, 5));
        borderPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                var flatLineBorder = new FlatLineBorder(new Insets(0, 0, 0, 2), UIManager.getColor("Component.borderColor"));
                setBorder(flatLineBorder);
            }
        };
        borderPanel.setLayout(new BorderLayout());

        addBtn = new JButton();
        addBtn.addActionListener(e -> AddKeyDialog.showAddDialog(connectInfo, System.out::println));

        refreshBtn = new JButton();
        refreshBtn.addActionListener(e -> searchActionPerformed());
        refreshBtn.setIcon(UI.REFRESH_ICON);
        databaseComboBox = new JComboBox<>();

        var dbList = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
        for (var db : dbList) {
            databaseComboBox.addItem(db);
        }

        databaseComboBox.addActionListener(e -> {
            var db = (Integer) databaseComboBox.getSelectedItem();
            this.connectInfo.setDatabase(db);
            searchActionPerformed();
        });

        treePanel = new JPanel();
        treePanel.setBorder(new EmptyBorder(3, 2, 2, 2));

        searchTextField = new JTextField();
        searchTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入关键字...");
        var searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.addActionListener(actionEvent -> searchActionPerformed());
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) textField -> searchActionPerformed());

        keyTree = new JTree();
        keyTree.setRootVisible(false);
        keyTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        keyTree.addTreeSelectionListener(e -> {
            var selectNode = keyTree.getLastSelectedPathComponent();
            if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                if (treeNodeInfo.getChildCount() == 0) {
                    ExecutorUtil.runAsync(() -> nodeClickProcessHandler.processHandler(treeNodeInfo));
                }
            }
        });

        var popupMenu = new JPopupMenu() {
            {
                var addMenuItem = new JMenuItem("添加");
                add(addMenuItem);
                var delMenuItem = new JMenuItem("删除");
                delMenuItem.addActionListener((e) -> deleteActionPerformed());
                add(delMenuItem);
                var refMenuItem = new JMenuItem("刷新");
                add(refMenuItem);
                var memMenuItem = new JMenuItem("内存分析");
                add(memMenuItem);
            }
        };

        keyTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (MouseEvent.BUTTON3 == e.getButton() && keyTree.getSelectionCount() > 0) {
                    popupMenu.show(keyTree, e.getX(), e.getY());
                    popupMenu.setVisible(true);
                    popupMenu.pack();
                }
            }
        });

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
        borderPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(borderPanel, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        borderPanel.add(panel1, BorderLayout.NORTH);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        panel2.add(databaseComboBox, BorderLayout.WEST);
        addBtn.setHorizontalAlignment(0);
        addBtn.setHorizontalTextPosition(11);
        addBtn.setText("新增");
        panel2.add(addBtn, BorderLayout.CENTER);
        refreshBtn.setText("");
        panel2.add(refreshBtn, BorderLayout.EAST);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel1.add(panel3, BorderLayout.SOUTH);
        panel3.add(searchTextField, BorderLayout.CENTER);
        treePanel.setLayout(new BorderLayout(0, 0));
        borderPanel.add(treePanel, BorderLayout.CENTER);
        treePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 5, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        treePanel.add(scrollPane1, BorderLayout.CENTER);
        scrollPane1.setViewportView(keyTree);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
