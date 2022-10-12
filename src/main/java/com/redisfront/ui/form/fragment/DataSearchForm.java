package com.redisfront.ui.form.fragment;

import cn.hutool.core.util.NumberUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.redisfront.RedisFrontApplication;
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
import com.redisfront.service.*;
import com.redisfront.ui.dialog.AddKeyDialog;
import io.lettuce.core.*;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private JButton deleteAllBtn;
    private volatile Map<Integer, ScanContext<String>> scanKeysContextMap;
    private ProcessHandler<TreeNodeInfo> nodeClickProcessHandler;
    private final ConnectInfo connectInfo;
    private JButton searchBtn;

    private final ArrayList<DbInfo> dbList = new ArrayList<>() {
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

    static final ExecutorService executorService = Executors.newFixedThreadPool(5);

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
            LoadingUtils.showDialog(LocaleUtils.getMessageFromBundle("DataSearchForm.showDialog.message"));
            scanBeforeProcess();
            var scanKeysContext = scanKeysContextMap.get(connectInfo.database());

            if (Fn.isNull(scanKeysContext.getLimit())) {
                Long limit = PrefUtils.getState().getLong(Const.KEY_KEY_MAX_LOAD_NUM, 10000L);
                scanKeysContext.setLimit(limit);
            }

            var lastSearchKey = scanKeysContext.getSearchKey();
            scanKeysContext.setSearchKey(key);

            if (!key.contains("*")) {
                var all = allField.getText();
                var scanInfo = all.split(SEPARATOR_FLAG);
                if (scanInfo.length > 1) {
                    if (Fn.equal(scanInfo[1], "null")) {
                        throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataSearchForm.exception.databaseIsNull.message"), true);
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

            LoadingUtils.closeDialog();

            var scanKeysList = new ArrayList<>(keyScanCursor.getKeys());

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
                    throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataSearchForm.exception.loadUpperLimit.message"));
                }
                scanKeysContext.getKeyList().addAll(scanKeysList);
            } else {
                scanKeysContext.setKeyList(scanKeysList);
            }

            var delim = PrefUtils.getState().get(Const.KEY_KEY_SEPARATOR, ":");

            var treeModel = TreeUtils.toTreeModel(new HashSet<>(scanKeysContext.getKeyList()), delim);

            var finalKeyScanCursor = keyScanCursor;
            SwingUtilities.invokeLater(() -> {
                currentField.setText(String.valueOf(scanKeysContext.getKeyList().size()));
                loadMoreBtn.setEnabled(!finalKeyScanCursor.isFinished());
                var all = allField.getText();
                var scanInfo = all.split(SEPARATOR_FLAG);
                if (scanInfo.length > 1) {
                    var current = Long.parseLong(scanInfo[0]);
                    var allSize = NumberUtil.isNumber(scanInfo[1]) ? Long.parseLong(scanInfo[1]) : 0;
                    //如果全部扫描完成！
                    if (current >= allSize && scanKeysContext.getKeyList().size() == allSize) {
                        loadMoreBtn.setText(LocaleUtils.getMessageFromBundle("DataSearchForm.loadMoreBtn.complete.title"));
                        loadMoreBtn.setEnabled(false);
                        return;
                    } else {
                        allField.setText((current + scanKeysContext.getLimit()) + SEPARATOR_FLAG + allSize);
                        var title = LocaleUtils.getMessageFromBundle("DataSearchForm.allField.toolTipText.title");
                        allField.setToolTipText(String.format(title, current, allSize));
                    }

                } else {
                    var title = LocaleUtils.getMessageFromBundle("DataSearchForm.allField.toolTipText.title");
                    allField.setToolTipText(String.format(title, scanKeysContext.getLimit(), all));
                    allField.setText(scanKeysContext.getLimit() + SEPARATOR_FLAG + all);
                }
                if (loadMoreBtn.isEnabled()) {
                    loadMoreBtn.setText(LocaleUtils.getMessageFromBundle("DataSearchForm.loadMoreBtn.title"));
                    loadMoreBtn.requestFocus();
                } else {
                    loadMoreBtn.setText(LocaleUtils.getMessageFromBundle("DataSearchForm.loadMoreBtn.complete.title"));
                    loadMoreBtn.setEnabled(false);
                }
                keyTree.setModel(treeModel);
                keyTree.updateUI();
            });
            scanAfterProcess();
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
        FutureUtils.runAsync(() -> {

            if (Fn.isEmpty(searchTextField.getText())) {
                try {
                    loadTreeModelData("*");
                } catch (Exception throwable) {
                    var cause = throwable.getCause();
                    if (cause instanceof RedisCommandExecutionException) {
                        scanBeforeProcess();
                        addBtn.setEnabled(false);
                        AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("DataSearchForm.showInformationDialog.message"), cause);
                    } else {
                        AlertUtils.showErrorDialog("ERROR", cause);
                    }
                }
            } else {
                try {
                    loadTreeModelData(searchTextField.getText());
                } catch (Exception throwable) {
                    var cause = throwable.getCause();
                    if (cause instanceof RedisCommandExecutionException) {
                        scanBeforeProcess();
                        addBtn.setEnabled(false);
                        AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("DataSearchForm.showInformationDialog.message"), cause);
                    } else {
                        AlertUtils.showErrorDialog("ERROR", cause);
                    }
                }
            }
        });
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
            if (Fn.notEqual(loadMoreBtn.getText(), LocaleUtils.getMessageFromBundle("DataSearchForm.loadMoreBtn.complete.title"))) {
                loadMoreBtn.requestFocus();
                loadMoreBtn.setEnabled(true);
            }
            databaseComboBox.setEnabled(Fn.notEqual(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER));
        });
    }

    public void deleteActionPerformed() {
        var treeModel = (DefaultTreeModel) keyTree.getModel();
        var selectNode = keyTree.getLastSelectedPathComponent();
        if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
            if (treeNodeInfo.getParent() != null) {
                RedisBasicService.service.del(connectInfo, treeNodeInfo.key());
                treeModel.removeNodeFromParent(treeNodeInfo);
            }
        }
    }

    public void deleteActionPerformed(TreeNodeInfo treeNodeInfo) {
        if (treeNodeInfo.getChildCount() > 0) {
            for (int i = 0; i < treeNodeInfo.getChildCount(); i++) {
                var subNode = (TreeNodeInfo) treeNodeInfo.getChildAt(i);
                deleteActionPerformed(subNode);
            }
        } else {
            RedisBasicService.service.del(connectInfo, treeNodeInfo.key());
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

        addBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataSearchForm.addBtn.title"));
                setToolTipText(LocaleUtils.getMessageFromBundle("DataSearchForm.addBtn.title"));
            }
        };
        addBtn.setIcon(UI.PLUS_ICON);
        addBtn.addActionListener(e -> AddKeyDialog.showAddDialog(connectInfo, null, (key) -> {
            var res = JOptionPane.showConfirmDialog(RedisFrontApplication.frame,
                    LocaleUtils.getMessageFromBundle("DataSearchForm.showConfirmDialog.message"),
                    LocaleUtils.getMessageFromBundle("DataSearchForm.showConfirmDialog.title"), JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
                scanKeysAndInitScanInfo();
            }
        }));

        refreshBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setToolTipText(LocaleUtils.getMessageFromBundle("DataSearchForm.refreshBtn.title"));
            }
        };

        //刷新按钮事件
        refreshBtn.addActionListener(e -> {
            refreshBtn.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                var selectedIndex = databaseComboBox.getSelectedIndex();
                searchTextField.setText("");
                databaseComboBox.removeAllItems();
                databaseComboBoxInit(selectedIndex);
            });
        });
        refreshBtn.setIcon(UI.REFRESH_ICON);

        deleteAllBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setToolTipText(LocaleUtils.getMessageFromBundle("DataSearchForm.deleteAllBtn.title"));
            }
        };

        deleteAllBtn.addActionListener(e -> FutureUtils.runAsync(
                () -> {
                    String operation = JOptionPane.showInputDialog(LocaleUtils.getMessageFromBundle("DataSearchForm.showInputDialog.title") + "\n “flushdb” or “flushall” ");
                    if (Fn.equal(operation, "flushdb")) {
                        RedisBasicService.service.flushdb(connectInfo);
                    } else if (Fn.equal(operation, "flushall")) {
                        RedisBasicService.service.flushall(connectInfo);
                    }
                },
                this::scanBeforeProcess,
                this::scanAfterProcess
        ));
        deleteAllBtn.setIcon(UI.DELETE_ICON);


        databaseComboBox = new JComboBox<>();

        loadMoreBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataSearchForm.loadMoreBtn.continue.title"));
            }
        };
        loadMoreBtn.setIcon(UI.LOAD_MORE_ICON);
        loadMoreBtn.addActionListener(e -> {
            FutureUtils.runAsync(() -> LoadingUtils.showDialog(LocaleUtils.getMessageFromBundle("MainWindowForm.loading.title")));
            scanKeysActionPerformed();
        });


        scanKeysContextMap = new ConcurrentHashMap<>();

        databaseComboBoxInit(0);

        databaseComboBox.addActionListener(e -> {
            var db = (DbInfo) databaseComboBox.getSelectedItem();
            if (Fn.isNull(db)) {
                return;
            }
            connectInfo.setDatabase(db.dbIndex());
            scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
            var limit = PrefUtils.getState().getLong(Const.KEY_KEY_MAX_LOAD_NUM, 10000L);
            var flag = !Fn.isNull(db.dbSize()) && (db.dbSize() > limit);
            allField.setText(String.valueOf(db.dbSize()));
            loadMorePanel.setVisible(flag);
            FutureUtils.runAsync(() -> LoadingUtils.showDialog(LocaleUtils.getMessageFromBundle("MainWindowForm.loading.title")));
            scanKeysActionPerformed();
        });

        searchTextField = new JTextField() {
            @Override
            public void updateUI() {
                super.updateUI();
                putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, LocaleUtils.getMessageFromBundle("DataSearchForm.searchTextField.placeholder.text"));
            }
        };
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
                var addMenuItem = new JMenuItem() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setText(LocaleUtils.getMessageFromBundle("DataSearchForm.addMenuItem.title"));
                    }
                };
                addMenuItem.addActionListener((e) -> {
                    var selectNode = keyTree.getLastSelectedPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        AddKeyDialog.showAddDialog(connectInfo, treeNodeInfo.key(), (key) -> {
                            var res = JOptionPane.showConfirmDialog(RedisFrontApplication.frame,
                                    LocaleUtils.getMessageFromBundle("DataSearchForm.showConfirmDialog.message"),
                                    LocaleUtils.getMessageFromBundle("DataSearchForm.showConfirmDialog.title"), JOptionPane.YES_NO_OPTION);
                            if (res == JOptionPane.YES_OPTION) {
                                scanKeysContextMap.put(connectInfo.database(), new ScanContext<>());
                                scanKeysAndInitScanInfo();
                            }
                        });
                    }
                });
                add(addMenuItem);
                var delMenuItem = new JMenuItem() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setText(LocaleUtils.getMessageFromBundle("DataSearchForm.delMenuItem.title"));
                    }
                };
                delMenuItem.addActionListener((e) -> {
                    DefaultTreeModel treeModel = (DefaultTreeModel) keyTree.getModel();
                    var selectNode = keyTree.getLastSelectedPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        FutureUtils.runAsync(() -> deleteActionPerformed(treeNodeInfo),
                                //前置方法
                                () -> SwingUtilities.invokeLater(() -> {
                                    var title = treeNodeInfo.title();
                                    treeNodeInfo.setTitle(title.concat(" ").concat(LocaleUtils.getMessageFromBundle("DataSearchForm.treeNodeInfo.del.doing.message")));
                                    keyTree.updateUI();
                                }),
                                //后置方法
                                () -> SwingUtilities.invokeLater(() -> treeModel.removeNodeFromParent(treeNodeInfo)));
                    }
                });
                add(delMenuItem);


                var memoryMenuItem = new JMenuItem() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setText(LocaleUtils.getMessageFromBundle("DataSearchForm.memoryMenuItem.title"));
                    }
                };
                memoryMenuItem.addActionListener((e) -> {
                    var selectionPath = keyTree.getLeadSelectionPath();
                    var selectNode = selectionPath.getLastPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        var title = treeNodeInfo.title();
                        SwingUtilities.invokeLater(() -> {

                        });
                        FutureUtils.runAsync(() -> {
                            SwingUtilities.invokeLater(() -> {
                                treeNodeInfo.setTitle(title.concat(" ").concat(LocaleUtils.getMessageFromBundle("DataSearchForm.treeNodeInfo.memory.doing.message")));
                                expandPath(selectionPath);
                                keyTree.updateUI();
                            });

                            memoryAnalysis(treeNodeInfo);

                            SwingUtilities.invokeLater(() -> {
                                treeNodeInfo.setTitle(title);
                                keyTree.updateUI();
                            });
                        }, executorService);
                    }
                });
                add(memoryMenuItem);
            }

            private void expandPath(TreePath treePath) {
                keyTree.expandPath(treePath);
                var treeNodeInfo = (TreeNodeInfo) treePath.getLastPathComponent();
                if (treeNodeInfo.getChildCount() > 0) {
                    for (int i = 0; i < treeNodeInfo.getChildCount(); i++) {
                        var childPath = treePath.pathByAddingChild(treeNodeInfo.getChildAt(i));
                        keyTree.expandPath(childPath);
                        expandPath(childPath);
                    }
                }
            }

            private void memoryAnalysis(TreeNodeInfo treeNodeInfo) {
                if (treeNodeInfo.getChildCount() > 0) {
                    for (int i = 0; i < treeNodeInfo.getChildCount(); i++) {
                        memoryAnalysis((TreeNodeInfo) treeNodeInfo.getChildAt(i));
                    }
                } else {
                    FutureUtils.supplyAsync(
                            () -> RedisBasicService.service.type(connectInfo, treeNodeInfo.key()),
                            type -> {
                                var typeEnum = Enum.KeyTypeEnum.valueOf(type.toUpperCase());

                                if (typeEnum.equals(Enum.KeyTypeEnum.STRING)) {
                                    var value = RedisStringService.service.get(connectInfo, treeNodeInfo.key());
                                    SwingUtilities.invokeLater(() -> {
                                        treeNodeInfo.setMemorySize(value.length());
                                        keyTree.updateUI();
                                    });
                                }

                                if (typeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
                                    var valueScanCursor = RedisZSetService.service.zscan(connectInfo, treeNodeInfo.key(), ScoredValueScanCursor.INITIAL);
                                    var dataList = new ArrayList<>(valueScanCursor.getValues());
                                    while (!valueScanCursor.isFinished()) {
                                        valueScanCursor = RedisZSetService.service.zscan(connectInfo, treeNodeInfo.key(), valueScanCursor);
                                        dataList.addAll(valueScanCursor.getValues());
                                    }
                                    if (Fn.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }
                                }

                                if (typeEnum.equals(Enum.KeyTypeEnum.HASH)) {

                                    var mapScanCursor = RedisHashService.service.hscan(connectInfo, treeNodeInfo.key(), MapScanCursor.INITIAL);
                                    var dataList = new ArrayList<>(mapScanCursor.getMap().entrySet());
                                    while (!mapScanCursor.isFinished()) {
                                        mapScanCursor = RedisHashService.service.hscan(connectInfo, treeNodeInfo.key(), mapScanCursor);
                                        dataList.addAll(new ArrayList<>(mapScanCursor.getMap().entrySet()));
                                    }

                                    if (Fn.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }
                                }

                                if (typeEnum.equals(Enum.KeyTypeEnum.LIST)) {
                                    var len = RedisListService.service.llen(connectInfo, treeNodeInfo.key());
                                    var start = 0;
                                    var dataList = new ArrayList<>();
                                    while (start >= len) {
                                        var stop = start + (1000 - 1);
                                        var values = RedisListService.service.lrange(connectInfo, treeNodeInfo.key(), start, stop);
                                        dataList.addAll(values);
                                        start += 1000;
                                    }
                                    if (Fn.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> ((String) e).getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }

                                }

                                if (typeEnum.equals(Enum.KeyTypeEnum.SET)) {
                                    var valueScanCursor = RedisSetService.service.sscan(connectInfo, treeNodeInfo.key(), ValueScanCursor.INITIAL);
                                    var dataList = new ArrayList<>(valueScanCursor.getValues());
                                    while (!valueScanCursor.isFinished()) {
                                        valueScanCursor = RedisSetService.service.sscan(connectInfo, treeNodeInfo.key(), valueScanCursor);
                                        dataList.addAll(valueScanCursor.getValues());
                                    }
                                    if (Fn.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }
                                }
                            }).join();
                }
            }

        };

        //邮件菜单
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
        var currentLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataSearchForm.currentLabel.title"));
            }
        };

        currentLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        currentLabel.setSize(5, -1);
        currentField.setBorder(new EmptyBorder(0, 5, 0, 0));
        currentField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, currentLabel);

        allField = new JTextField();
        var allLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataSearchForm.allLabel.title"));
            }
        };
        allLabel.setOpaque(true);
        allLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        allLabel.setSize(5, -1);
        allField.setBorder(new EmptyBorder(0, 5, 0, 0));
        allField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, allLabel);

    }

    private void databaseComboBoxInit(int selectedIndex) {
        if (Fn.notEqual(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            var keySpace = RedisBasicService.service.getKeySpace(connectInfo);
            for (int i = 0; i < dbList.size(); i++) {
                var dbInfo = dbList.get(i);
                var value = (String) keySpace.get(dbInfo.dbName().toLowerCase());
                if (Fn.isNotEmpty(value)) {
                    String[] s = value.split(",");
                    String[] sub = s[0].split("=");
                    dbInfo.setDbSize(Long.valueOf(sub[1]));
                } else {
                    dbInfo.setDbSize(0L);
                }
                scanKeysContextMap.put(dbInfo.dbIndex(), new ScanContext<>());
                databaseComboBox.insertItemAt(dbInfo, i);
            }
        } else {
            var dbInfo = dbList.get(0);
            scanKeysContextMap.put(dbInfo.dbIndex(), new ScanContext<>());
            var dbSize = RedisBasicService.service.dbSize(connectInfo);
            dbInfo.setDbSize(dbSize);
            databaseComboBox.insertItemAt(dbInfo, 0);
            databaseComboBox.setEnabled(false);
        }
        databaseComboBox.setSelectedIndex(selectedIndex);
    }

    private void scanKeysAndInitScanInfo() {
        var all = allField.getText();
        var scanInfo = all.split(SEPARATOR_FLAG);
        if (scanInfo.length > 1) {
            allField.setText("0" + SEPARATOR_FLAG + scanInfo[1]);
            allField.setToolTipText(String.format(LocaleUtils.getMessageFromBundle("DataSearchForm.allLabel.toolTip.text"), 0, scanInfo[1]));
        }
        FutureUtils.runAsync(() -> LoadingUtils.showDialog(LocaleUtils.getMessageFromBundle("MainWindowForm.loading.title")));
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
        this.$$$loadButtonText$$$(addBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataSearchForm.addBtn.title"));
        panel2.add(addBtn, BorderLayout.CENTER);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel2.add(panel3, BorderLayout.EAST);
        refreshBtn.setText("");
        panel3.add(refreshBtn, BorderLayout.CENTER);
        deleteAllBtn.setText("");
        panel3.add(deleteAllBtn, BorderLayout.EAST);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel1.add(panel4, BorderLayout.SOUTH);
        panel4.add(searchTextField, BorderLayout.CENTER);
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

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
