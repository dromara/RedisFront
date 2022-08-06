package com.redisfront.ui.form.fragment;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ProcessHandler;
import com.redisfront.commons.util.*;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.DbInfo;
import com.redisfront.model.ScanContext;
import com.redisfront.model.TreeNodeInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.ui.dialog.AddKeyDialog;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.ScanCursor;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * DataSearchForm
 *
 * @author Jin
 */
public class DataSearchForm {
    private static final Logger log = LoggerFactory.getLogger(DataSearchForm.class);
    private static final String SEPARATOR_FLAG = "/";
    private JPanel contentPanel;
    private JXTree keyTree;
    private JTextField searchTextField;
    private JComboBox<DbInfo> databaseComboBox;
    private JButton addBtn;
    private JButton loadMoreBtn;
    private JPanel treePanel;
    private JButton refreshBtn;
    private JPanel borderPanel;
    private JTextField currentField;
    private JTextField allField;
    private JPanel loadMorePanel;
    private volatile Map<Integer, ScanContext<String>> scanKeysContextMap;
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
        databaseComboBox.setSelectedIndex(0);
    }

    public void setNodeClickProcessHandler(ProcessHandler<TreeNodeInfo> nodeClickProcessHandler) {
        this.nodeClickProcessHandler = nodeClickProcessHandler;
    }


    public synchronized void loadTreeModelData(String key) {
        try {
            LoadingUtils.showDialog();
            scanBeforeProcess();
            var scanKeysContext = scanKeysContextMap.get(connectInfo.database());

            if (Fn.isNull(scanKeysContext.getLimit())) {
                Long limit = PrefUtils.getState().getLong(Const.KEY_KEY_MAX_LOAD_NUM, 10000L);
                scanKeysContext.setLimit(limit);
            }

            var lastSearchKey = scanKeysContext.getSearchKey();
            scanKeysContext.setSearchKey(key);

            if (Fn.isNull(scanKeysContext.getScanCursor())) {
                scanKeysContext.setScanCursor(ScanCursor.INITIAL);
            }

            if (!key.contains("*")) {
                var all = allField.getText();
                var scanInfo = all.split(SEPARATOR_FLAG);
                if (scanInfo.length > 1) {
                    if (Fn.equal(scanInfo[1], "null")) {
                        throw new RedisFrontException("当前数据库为空！", true);
                    } else {
                        scanKeysContext.setLimit(Long.valueOf(scanInfo[1]));
                    }
                } else {
                    scanKeysContext.setLimit(Long.valueOf(all));
                }

            }

            var keyScanCursor = RedisBasicService.service.scan(connectInfo, scanKeysContext.getScanCursor(), scanKeysContext.getScanArgs());
            scanKeysContext.setScanCursor(keyScanCursor);
            log.debug("本次扫描到：{}", keyScanCursor.getKeys().size());

            ArrayList<String> scanKeysList = new ArrayList<>(keyScanCursor.getKeys());

            //模糊匹配(模糊匹配在key数量小于 limit 的情况加全部查询出来)
            if (!loadMorePanel.isVisible() && Fn.equal("*", key)) {
                while (Fn.equal("*", key) && !keyScanCursor.isFinished()) {
                    keyScanCursor = RedisBasicService.service.scan(connectInfo, scanKeysContext.getScanCursor(), scanKeysContext.getScanArgs());
                    scanKeysContext.setScanCursor(keyScanCursor);
                    scanKeysList.addAll(keyScanCursor.getKeys());
                }
            }

            //数据扫描上限判断！
            if (Fn.equal(scanKeysContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(scanKeysContext.getKeyList())) {
                if (scanKeysContext.getKeyList().size() >= 300000) {
                    System.gc();
                    throw new RedisFrontException("数据加载上限，请使用正则模糊匹配查找！");
                }
                scanKeysContext.getKeyList().addAll(scanKeysList);
            } else {
                scanKeysContext.setKeyList(scanKeysList);
            }

            String delim = PrefUtils.getState().get(Const.KEY_KEY_SEPARATOR, ":");

            var treeModel = TreeUtils.toTreeModel(new HashSet<>(scanKeysContext.getKeyList()), delim);

            KeyScanCursor<String> finalKeyScanCursor = keyScanCursor;
            SwingUtilities.invokeLater(() -> {
                currentField.setText(String.valueOf(scanKeysContext.getKeyList().size()));
                loadMoreBtn.setEnabled(!finalKeyScanCursor.isFinished());
                var all = allField.getText();
                var scanInfo = all.split(SEPARATOR_FLAG);
                if (scanInfo.length > 1) {
                    var current = Long.valueOf(scanInfo[0]);
                    var allSize = Long.valueOf(scanInfo[1]);
                    //如果全部扫描完成！
                    if (current >= allSize) {
                        loadMoreBtn.setText("扫描完成");
                        loadMoreBtn.setEnabled(false);
                    } else {
                        allField.setText((current + scanKeysContext.getLimit()) + SEPARATOR_FLAG + allSize);
                        allField.setToolTipText("游标：" + current + ", 全部Key：" + allSize);
                    }

                } else {
                    allField.setToolTipText("游标：".concat(String.valueOf(scanKeysContext.getLimit())) + ", 全部Key：".concat(all));
                    allField.setText(scanKeysContext.getLimit() + SEPARATOR_FLAG + all);
                }
                if (loadMoreBtn.isEnabled()) {
                    loadMoreBtn.setText("扫描更多");
                    loadMoreBtn.requestFocus();
                } else {
                    loadMoreBtn.setText("扫描完成");
                    loadMoreBtn.setEnabled(false);
                }
                keyTree.setModel(treeModel);
            });
            scanAfterProcess();
            LoadingUtils.closeDialog();
        } catch (Exception e) {
            e.printStackTrace();
            scanAfterProcess();
            LoadingUtils.closeDialog();
            if (e instanceof RedisFrontException) {
                loadMoreBtn.setEnabled(false);
                AlertUtils.showInformationDialog(e.getMessage());
            } else {
                throw e;
            }
        }

    }

    public void scanKeysActionPerformed() {
        if (Fn.isEmpty(searchTextField.getText())) {
            FutureUtils.runAsync(() -> loadTreeModelData("*"), throwable -> {
                var cause = throwable.getCause();
                if (cause instanceof RedisCommandExecutionException) {
                    scanBeforeProcess();
                    addBtn.setEnabled(false);
                    AlertUtils.showInformationDialog("当前环境不支持该命令", cause);
                } else {
                    AlertUtils.showErrorDialog("ERROR", cause);
                }
            });
        } else {
            FutureUtils.runAsync(() -> loadTreeModelData(searchTextField.getText()), throwable -> {
                var cause = throwable.getCause();
                if (cause instanceof RedisCommandExecutionException) {
                    scanBeforeProcess();
                    AlertUtils.showInformationDialog("当前环境不支持该命令", cause);
                } else {
                    AlertUtils.showErrorDialog("ERROR", cause);
                }
            });
        }
    }

    public void scanBeforeProcess() {
        SwingUtilities.invokeLater(() -> {
            searchTextField.setEnabled(false);
            refreshBtn.setEnabled(false);
            searchBtn.setEnabled(false);
            keyTree.setEnabled(false);
            loadMoreBtn.setEnabled(false);
            databaseComboBox.setEnabled(false);
        });
    }

    public void scanAfterProcess() {
        SwingUtilities.invokeLater(() -> {
            searchTextField.setEnabled(true);
            refreshBtn.setEnabled(true);
            searchBtn.setEnabled(true);
            keyTree.setEnabled(true);
            if (Fn.notEqual(loadMoreBtn.getText(), "扫描完成")) {
                loadMoreBtn.requestFocus();
                loadMoreBtn.setEnabled(true);
            }
            databaseComboBox.setEnabled(Fn.notEqual(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER));
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

        treePanel = new JPanel();
        treePanel.setBorder(new EmptyBorder(3, 2, 2, 2));

        addBtn = new JButton();
        addBtn.setIcon(UI.PLUS_ICON);
        addBtn.addActionListener(e -> AddKeyDialog.showAddDialog(connectInfo, null, (key) -> AlertUtils.showInformationDialog("添加成功！")));

        refreshBtn = new JButton();
        refreshBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> searchTextField.setText(""));
            scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
            scanKeysAndInitScanInfo();
        });
        refreshBtn.setIcon(UI.REFRESH_ICON);
        databaseComboBox = new JComboBox<>();

        loadMoreBtn = new JButton("继续扫描");
        loadMoreBtn.setIcon(UI.LOAD_MORE_ICON);
        loadMoreBtn.addActionListener(e -> scanKeysActionPerformed());

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

        scanKeysContextMap = new ConcurrentHashMap<>();

        if (Fn.notEqual(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            var keySpace = RedisBasicService.service.getKeySpace(connectInfo);
            for (var dbInfo : dbList) {
                var value = (String) keySpace.get(dbInfo.dbName().toLowerCase());
                if (Fn.isNotEmpty(value)) {
                    String[] s = value.split(",");
                    String[] sub = s[0].split("=");
                    dbInfo.setDbSize(Long.valueOf(sub[1]));
                }
                scanKeysContextMap.put(dbInfo.dbIndex(), new ScanContext<>());
                databaseComboBox.addItem(dbInfo);
            }
        } else {
            var dbInfo = dbList.get(0);
            scanKeysContextMap.put(dbInfo.dbIndex(), new ScanContext<>());
            var dbSize = RedisBasicService.service.dbSize(connectInfo);
            dbInfo.setDbSize(dbSize);
            databaseComboBox.addItem(dbInfo);
            databaseComboBox.setEnabled(false);
        }

        databaseComboBox.addActionListener(e -> {
            var db = (DbInfo) databaseComboBox.getSelectedItem();
            assert db != null;
            connectInfo.setDatabase(db.dbIndex());
            scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
            var limit = PrefUtils.getState().getLong(Const.KEY_KEY_MAX_LOAD_NUM, 10000L);
            var flag = !Fn.isNull(db.dbSize()) && (db.dbSize() > limit);
            allField.setText(String.valueOf(db.dbSize()));
            loadMorePanel.setVisible(flag);
            scanKeysActionPerformed();
        });

        searchTextField = new JTextField();
        searchTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入搜索词...");
        searchBtn = new JButton(new FlatSearchIcon());
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (Fn.equal(e.getKeyCode(), KeyEvent.VK_ENTER)) {
                    scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
                    scanKeysAndInitScanInfo();
                }
            }
        });
        searchBtn.addActionListener(actionEvent -> {
            scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
            scanKeysAndInitScanInfo();
        });
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) textField -> {
            scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
            searchTextField.setText("");
            scanKeysAndInitScanInfo();
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
                    FutureUtils.runAsync(() -> nodeClickProcessHandler.processHandler(treeNodeInfo));
                }
            }
        });

        var popupMenu = new JPopupMenu() {
            {
                var addMenuItem = new JMenuItem("添加");
                addMenuItem.addActionListener((e) -> {
                    var selectNode = keyTree.getLastSelectedPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        AddKeyDialog.showAddDialog(connectInfo, treeNodeInfo.key(), (key) -> AlertUtils.showInformationDialog("添加成功！"));
                    }
                });
                add(addMenuItem);
                var delMenuItem = new JMenuItem("删除");
                delMenuItem.addActionListener((e) -> deleteActionPerformed());
                add(delMenuItem);
                var refMenuItem = new JMenuItem("刷新");
                refMenuItem.addActionListener(e -> AlertUtils.showInformationDialog("待实现"));
                add(refMenuItem);
                var memMenuItem = new JMenuItem("内存分析");
                memMenuItem.addActionListener(e -> AlertUtils.showInformationDialog("待实现"));
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

        currentField = new JTextField();
        var currentLabel = new JLabel();
        currentLabel.setText("结果");
        currentLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        currentLabel.setSize(5, -1);
        currentField.setBorder(new EmptyBorder(0, 5, 0, 0));
        currentField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, currentLabel);

        allField = new JTextField();
        var allLabel = new JLabel();
        allLabel.setText("游标");
        allLabel.setOpaque(true);
        allLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        allLabel.setSize(5, -1);
        allField.setBorder(new EmptyBorder(0, 5, 0, 0));
        allField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, allLabel);

    }

    private void scanKeysAndInitScanInfo() {
        var all = allField.getText();
        var scanInfo = all.split(SEPARATOR_FLAG);
        if (scanInfo.length > 1) {
            allField.setText("0" + SEPARATOR_FLAG + scanInfo[1]);
            allField.setToolTipText("已扫描：" + 0 + ",全部：".concat(scanInfo[1]));
        }
        scanKeysActionPerformed();
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
        contentPanel.setMinimumSize(new Dimension(300, -1));
        contentPanel.setPreferredSize(new Dimension(300, 681));
        borderPanel.setLayout(new BorderLayout(0, 0));
        borderPanel.setMinimumSize(new Dimension(350, 199));
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
        scrollPane1.setMinimumSize(new Dimension(200, 18));
        treePanel.add(scrollPane1, BorderLayout.CENTER);
        keyTree.setMaximumSize(new Dimension(-1, -1));
        keyTree.setMinimumSize(new Dimension(200, 0));
        scrollPane1.setViewportView(keyTree);
        loadMorePanel = new JPanel();
        loadMorePanel.setLayout(new GridLayoutManager(2, 11, new Insets(0, 0, 0, 0), -1, -1));
        loadMorePanel.setEnabled(true);
        treePanel.add(loadMorePanel, BorderLayout.SOUTH);
        loadMorePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        currentField.setEditable(false);
        currentField.setEnabled(false);
        currentField.setVisible(true);
        loadMorePanel.add(currentField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
        loadMoreBtn.setMargin(new Insets(0, 0, 0, 3));
        loadMoreBtn.setText("扫描更多");
        loadMorePanel.add(loadMoreBtn, new GridConstraints(1, 0, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        allField.setEditable(false);
        allField.setEnabled(false);
        allField.setVisible(true);
        loadMorePanel.add(allField, new GridConstraints(0, 2, 1, 9, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(135, -1), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
