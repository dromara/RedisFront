package com.redisfront.ui.form.fragment;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.redisfront.commons.Handler.ProcessHandler;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.ExecutorUtil;
import com.redisfront.commons.util.LoadingUtil;
import com.redisfront.commons.util.TreeUtil;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.DbInfo;
import com.redisfront.model.TreeNodeInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.dialog.AddRedisKeyDialog;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

/**
 * DataSearchForm
 *
 * @author Jin
 */
public class DataSearchForm {
    private JPanel contentPanel;
    private JXTree keyTree;
    private JTextField searchTextField;
    private JComboBox<DbInfo> databaseComboBox;
    private JButton addBtn;
    private final JButton loadMoreBtn;
    private JPanel treePanel;
    private JButton refreshBtn;
    private JPanel borderPanel;

    private volatile Map<Integer, ScanKeysContext> scanKeysContextMap;

    private ProcessHandler<TreeNodeInfo> nodeClickProcessHandler;

    private final ConnectInfo connectInfo;
    private JButton searchBtn;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public static DataSearchForm newInstance(ConnectInfo connectInfo) {
        return new DataSearchForm(connectInfo);
    }

    public DataSearchForm(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        $$$setupUI$$$();
        loadMoreBtn = new JButton("加载更多");
        loadMoreBtn.addActionListener(e -> searchActionPerformed());
        treePanel.add(loadMoreBtn, BorderLayout.SOUTH);
        if (connectInfo.redisModeEnum() == Enum.RedisMode.CLUSTER) {
            databaseComboBox.setEnabled(false);
        }
        ExecutorUtil.runAsync(() -> loadTreeModelData("*"));
    }

    public void setNodeClickProcessHandler(ProcessHandler<TreeNodeInfo> nodeClickProcessHandler) {
        this.nodeClickProcessHandler = nodeClickProcessHandler;
    }

    private static class ScanKeysContext {
        private ScanCursor scanCursor;
        private Long limit;
        private String searchKey;
        private List<String> keys;

        public ScanCursor getScanCursor() {
            return scanCursor;
        }

        public void setScanCursor(ScanCursor scanCursor) {
            this.scanCursor = scanCursor;
        }

        public Long getLimit() {
            return limit;
        }

        public void setLimit(Long limit) {
            this.limit = limit;
        }

        public String getSearchKey() {
            return searchKey;
        }

        public void setSearchKey(String searchKey) {
            this.searchKey = searchKey;
        }

        public ScanArgs getScanArgs() {
            return ScanArgs.Builder.matches(getSearchKey()).limit(getLimit());
        }


        public List<String> getKeyList() {
            return keys;
        }

        public void setKeyList(List<String> keys) {
            this.keys = keys;
        }
    }

    public synchronized void loadTreeModelData(String key) {
        try {
            disableInputComponent();
            var scanKeysContext = scanKeysContextMap.get(connectInfo.database());

            if (Fn.isNull(scanKeysContext.limit)) {
                scanKeysContext.setLimit(10000L);
            }

            var lastSearchKey = scanKeysContext.getSearchKey();
            scanKeysContext.setSearchKey(key);

            if (Fn.isNull(scanKeysContext.getScanCursor())) {
                scanKeysContext.setScanCursor(ScanCursor.INITIAL);
            }

            var keyScanCursor = RedisBasicService.service.scan(connectInfo, scanKeysContext.getScanCursor(), scanKeysContext.getScanArgs());
            scanKeysContext.setScanCursor(keyScanCursor);

            if (Fn.equal(scanKeysContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(scanKeysContext.getKeyList())) {
                scanKeysContext.getKeyList().addAll(keyScanCursor.getKeys());
            } else {
                scanKeysContext.setKeyList(keyScanCursor.getKeys());
            }

            var treeModel = TreeUtil.toTreeModel(new HashSet<>(scanKeysContext.keys), ":");

            SwingUtilities.invokeLater(() -> {
                loadMoreBtn.setEnabled(!keyScanCursor.isFinished());
                keyTree.setModel(treeModel);
            });
        } finally {
            enableInputComponent();
        }

    }

    public void searchActionPerformed() {
        if (Fn.isEmpty(searchTextField.getText())) {
            ExecutorUtil.runAsync(() -> loadTreeModelData("*"));
        } else {
            ExecutorUtil.runAsync(() -> loadTreeModelData(searchTextField.getText()));
        }
    }

    private void disableInputComponent() {
        SwingUtilities.invokeLater(() -> {
            LoadingUtil.showDialog();
            searchTextField.setEnabled(false);
            refreshBtn.setEnabled(false);
            searchBtn.setEnabled(false);
            keyTree.setEnabled(false);
            loadMoreBtn.setEnabled(false);
            loadMoreBtn.setText("数据加载中...");
            databaseComboBox.setEnabled(false);
        });
    }

    private void enableInputComponent() {
        SwingUtilities.invokeLater(() -> {
            LoadingUtil.closeDialog();
            searchTextField.setEnabled(true);
            refreshBtn.setEnabled(true);
            searchBtn.setEnabled(true);
            keyTree.setEnabled(true);
            loadMoreBtn.setEnabled(true);
            loadMoreBtn.setText("加载更多>>");
            loadMoreBtn.requestFocus();
            databaseComboBox.setEnabled(true);
        });
    }

    public void deleteActionPerformed() {
        DefaultTreeModel treeModel = (DefaultTreeModel) keyTree.getModel();
        var selectNode = keyTree.getLastSelectedPathComponent();
        if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
            if (treeNodeInfo.getParent() != null) {
                RedisBasicService.service.del(connectInfo, treeNodeInfo.key());
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
        addBtn.setIcon(UI.PLUS_ICON);
        addBtn.addActionListener(e -> AddRedisKeyDialog.showAddDialog(connectInfo, System.out::println));

        refreshBtn = new JButton();
        refreshBtn.addActionListener(e -> {
            scanKeysContextMap.put(connectInfo.database(), new ScanKeysContext());
            searchActionPerformed();
        });
        refreshBtn.setIcon(UI.REFRESH_ICON);
        databaseComboBox = new JComboBox<>();

        var dbList = new ArrayList<DbInfo>() {
            {
                add(new DbInfo("DB0", 0));
                add(new DbInfo("DB1", 1));
                add(new DbInfo("DB2", 2));
                add(new DbInfo("DB3", 3));
                add(new DbInfo("DB4", 4));
                add(new DbInfo("DB5", 5));
                add(new DbInfo("DB6", 6));
                add(new DbInfo("DB7", 7));
                add(new DbInfo("DB8", 8));
                add(new DbInfo("DB9", 9));
                add(new DbInfo("DB10", 10));
                add(new DbInfo("DB11", 11));
                add(new DbInfo("DB12", 12));
                add(new DbInfo("DB13", 13));
                add(new DbInfo("DB14", 14));
                add(new DbInfo("DB15", 15));
            }
        };

        scanKeysContextMap = new LinkedHashMap<>();

        if (Fn.notEqual(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            var keySpace = RedisBasicService.service.getKeySpace(connectInfo);
            for (var dbInfo : dbList) {
                var value = (String) keySpace.get(dbInfo.dbName().toLowerCase());
                if (Fn.isNotEmpty(value)) {
                    String[] s = value.split(",");
                    String[] sub = s[0].split("=");
                    dbInfo.setDbSize(sub[1]);
                }
                scanKeysContextMap.put(dbInfo.dbIndex(), new ScanKeysContext());
                databaseComboBox.addItem(dbInfo);
            }
        } else {
            var dbInfo = dbList.get(0);
            scanKeysContextMap.put(dbInfo.dbIndex(), new ScanKeysContext());
            var dbSize = RedisBasicService.service.dbSize(connectInfo);
            dbInfo.setDbSize(dbSize.toString());
            databaseComboBox.addItem(dbInfo);
        }

        databaseComboBox.addActionListener(e -> {
            var db = (DbInfo) databaseComboBox.getSelectedItem();
            assert db != null;
            this.connectInfo.setDatabase(db.dbIndex());
            searchActionPerformed();
        });

        treePanel = new JPanel();
        treePanel.setBorder(new EmptyBorder(3, 2, 2, 2));

        searchTextField = new JTextField();
        searchTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入关键字...");
        searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.addActionListener(actionEvent -> {
            scanKeysContextMap.put(connectInfo.database(), new ScanKeysContext());
            searchActionPerformed();
        });
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) textField -> {
            SwingUtilities.invokeLater(() -> searchTextField.setText(""));
            scanKeysContextMap.put(connectInfo.database(), new ScanKeysContext());
            searchActionPerformed();
        });

        keyTree = new JXTree();
        keyTree.setRootVisible(false);
        keyTree.setModel(null);
        keyTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        keyTree.setCellRenderer(new DefaultXTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            }


        });
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
