package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import cn.hutool.core.util.NumberUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScoredValueScanCursor;
import io.lettuce.core.ValueScanCursor;
import lombok.Getter;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.AlertUtils;
import org.dromara.redisfront.commons.utils.FutureUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.utils.TreeUtils;
import org.dromara.redisfront.model.DbInfo;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.model.turbo.Turbo3;
import org.dromara.redisfront.service.*;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.scanner.context.RedisScanContext;
import org.dromara.redisfront.ui.dialog.AddKeyDialog;
import org.dromara.redisfront.ui.event.AddKeySuccessEvent;
import org.dromara.redisfront.ui.event.ClickKeyTreeNodeEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * DataSearchForm
 *
 * @author Jin
 */
@Getter
public class LeftSearchFragment {
    private static final Logger log = LoggerFactory.getLogger(LeftSearchFragment.class);
    private final RedisFrontWidget owner;
    private final RedisFrontContext context;
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
    private volatile Map<Integer, RedisScanContext<String>> scanKeysContextMap;
    private final RedisConnectContext redisConnectContext;
    private FlatToggleButton fuzzyMatchToggleButton;

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

    public LeftSearchFragment(RedisFrontWidget owner, RedisConnectContext redisConnectContext) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.redisConnectContext = redisConnectContext;
        $$$setupUI$$$();
        this.databaseComboBox.setSelectedIndex(0);
    }

    private void createUIComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
        borderPanel = new JPanel() {
        };
        borderPanel.setLayout(new BorderLayout());

        treePanel = new JPanel();
        treePanel.setBorder(new EmptyBorder(3, 2, 2, 2));

        addBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataSearchForm.addBtn.title"));
                setToolTipText(owner.$tr("DataSearchForm.addBtn.title"));
            }
        };
        addBtn.setIcon(Icons.PLUS_ICON_16X16);
        addBtn.setFocusable(false);
        addBtn.addActionListener(_ -> AddKeyDialog.showAddDialog(owner, redisConnectContext, null));

        this.owner.getEventListener().bind(redisConnectContext.getId(), AddKeySuccessEvent.class, qsEvent -> {
            if (qsEvent instanceof AddKeySuccessEvent addKeySuccessEvent) {
                if (redisConnectContext.getId() == addKeySuccessEvent.getId()) {
                    Object message = addKeySuccessEvent.getMessage();
                    var res = JOptionPane.showConfirmDialog(owner,
                            owner.$tr("DataSearchForm.showConfirmDialog.message"),
                            owner.$tr("DataSearchForm.showConfirmDialog.title"), JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        scanKeysContextMap.put(redisConnectContext.getDatabase(), new RedisScanContext<>());
                        scanKeysAndUpdateScanInfo();
                    }
                }
            }
        });

        refreshBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setToolTipText(owner.$tr("DataSearchForm.refreshBtn.title"));
            }
        };

        //刷新按钮事件
        refreshBtn.addActionListener(_ -> {
            var selectedIndex = databaseComboBox.getSelectedIndex();
            searchTextField.setText("");
            databaseComboBox.removeAllItems();
            changeDatabaseActionPerformed(selectedIndex);
        });
        refreshBtn.setFocusable(false);
        refreshBtn.setIcon(Icons.REFRESH_ICON);

        deleteAllBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setToolTipText(owner.$tr("DataSearchForm.deleteAllBtn.title"));
            }
        };
        deleteAllBtn.setFocusable(false);

        deleteAllBtn.addActionListener(_ -> {
            String operation = JOptionPane.showInputDialog(owner.$tr("DataSearchForm.showInputDialog.title") + "\n “flushdb” or “flushall” ");
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                if (RedisFrontUtils.equal(operation, "flushdb")) {
                    RedisBasicService.service.flushdb(redisConnectContext);
                } else if (RedisFrontUtils.equal(operation, "flushall")) {
                    RedisBasicService.service.flushall(redisConnectContext);
                }
                return null;
            }, (_, e) -> {
                if (e != null) {
                    owner.displayException(e);
                    return;
                }
                refreshBtn.doClick();
            });

        });
        deleteAllBtn.setIcon(Icons.DELETE_B_ICON);


        databaseComboBox = new JComboBox<>();
        databaseComboBox.setFocusable(false);
        loadMoreBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataSearchForm.loadMoreBtn.continue.title"));
            }
        };
        loadMoreBtn.setFocusable(false);
        loadMoreBtn.setIcon(Icons.LOAD_MORE_ICON);
        loadMoreBtn.addActionListener(_ -> scanKeysActionPerformed());


        scanKeysContextMap = new ConcurrentHashMap<>();

        changeDatabaseActionPerformed(0);

        databaseComboBox.addActionListener(_ -> {
            var db = (DbInfo) databaseComboBox.getSelectedItem();
            if (RedisFrontUtils.isNull(db)) {
                return;
            }
            redisConnectContext.setDatabase(db.dbIndex());
            scanKeysContextMap.put(redisConnectContext.getDatabase(), new RedisScanContext<>());
            var limit = redisConnectContext.getSetting().getLoadKeyNum();
            var flag = !RedisFrontUtils.isNull(db.dbSize()) && (db.dbSize() > limit);
            allField.setText(String.valueOf(db.dbSize()));
            loadMorePanel.setVisible(flag);
            scanKeysActionPerformed();
        });

        searchTextField = new JTextField() {
            @Override
            public void updateUI() {
                super.updateUI();
                putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, owner.$tr("DataSearchForm.searchTextField.placeholder.text"));
            }
        };

        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (RedisFrontUtils.equal(e.getKeyCode(), KeyEvent.VK_ENTER)) {
                    scanKeysContextMap.put(redisConnectContext.getDatabase(), new RedisScanContext<>());
                    scanKeysAndUpdateScanInfo();
                }
            }
        });

        JButton searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.setFocusable(false);
        searchBtn.addActionListener(_ -> {
            scanKeysContextMap.put(redisConnectContext.getDatabase(), new RedisScanContext<>());
            scanKeysAndUpdateScanInfo();
        });

        fuzzyMatchToggleButton = new FlatToggleButton();
        fuzzyMatchToggleButton.setButtonType(FlatButton.ButtonType.tab);
        fuzzyMatchToggleButton.setTabUnderlineHeight(0);
        fuzzyMatchToggleButton.setSelectedIcon(Icons.MATCH_SELECTED);
        fuzzyMatchToggleButton.setIcon(Icons.MATCH_UNSELECTED);
        fuzzyMatchToggleButton.setFocusable(false);
        fuzzyMatchToggleButton.setToolTipText(owner.$tr("DataSearchForm.toggleButton.tooltip.text"));

        FlatToolBar flatToolBar = new FlatToolBar();
        flatToolBar.add(fuzzyMatchToggleButton);
        flatToolBar.add(searchBtn);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, flatToolBar);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) _ -> {
            scanKeysContextMap.put(redisConnectContext.getDatabase(), new RedisScanContext<>());
            searchTextField.setText("");
            scanKeysAndUpdateScanInfo();
        });


        keyTree = new JXTree();
        keyTree.setFocusable(false);
        keyTree.setRootVisible(false);
        keyTree.setModel(null);
        keyTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        keyTree.setCellRenderer(new DefaultXTreeCellRenderer() {
            {
                setLeafIcon(Icons.TREE_KEY_ICON);
            }
        });
        keyTree.addTreeSelectionListener(_ -> {
            var selectNode = keyTree.getLastSelectedPathComponent();
            if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                if (treeNodeInfo.getChildCount() == 0) {
                    context.getEventBus().publish(new ClickKeyTreeNodeEvent(treeNodeInfo, redisConnectContext.getId()));
                }
            }
        });

        var popupMenu = new JPopupMenu() {
            {
                //天机 Add
                var addMenuItem = getAddMenuItem();
                add(addMenuItem);

                //删除 Delete
                var delMenuItem = getDelMenuItem();

                KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK);

                InputMap inputMap = keyTree.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                inputMap.put(keyStroke, "triggerDelete");

                ActionMap actionMap = keyTree.getActionMap();
                actionMap.put("triggerDelete", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        delMenuItem.doClick();
                    }
                });

                add(delMenuItem);

                //内存分析 Memory analysis
                var memoryMenuItem = getMemoryMenuItem();
                add(memoryMenuItem);
            }

            private @NotNull JMenuItem getMemoryMenuItem() {
                var memoryMenuItem = new JMenuItem() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setText(owner.$tr("DataSearchForm.memoryMenuItem.title"));
                    }
                };
                memoryMenuItem.addActionListener(_ -> {
                    var selectionPath = keyTree.getLeadSelectionPath();
                    var selectNode = selectionPath.getLastPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        var title = treeNodeInfo.title();
                        SwingUtilities.invokeLater(() -> {

                        });
                        FutureUtils.runAsync(() -> {
                            SwingUtilities.invokeLater(() -> {
                                treeNodeInfo.setTitle(title.concat(" ").concat(owner.$tr("DataSearchForm.treeNodeInfo.memory.doing.message")));
                                expandPath(selectionPath);
                                keyTree.updateUI();
                            });
                            memoryAnalysis(treeNodeInfo);
                            SwingUtilities.invokeLater(() -> {
                                treeNodeInfo.setTitle(title);
                                keyTree.updateUI();
                            });
                        });
                    }
                });
                return memoryMenuItem;
            }

            private @NotNull JMenuItem getDelMenuItem() {
                var delMenuItem = new JMenuItem() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setText(owner.$tr("DataSearchForm.delMenuItem.title"));
                    }
                };

                delMenuItem.addActionListener((_) -> {
                    //删除，需要进行弹框确认，生产项目，如果大规模删除，就是灾难
                    int reply = AlertUtils.showConfirmDialog(owner,
                            owner.$tr("DataSearchForm.delMenuItem.confirm"),
                            JOptionPane.YES_NO_OPTION);
                    if (reply != JOptionPane.YES_OPTION) {
                        //用户：确认的不是YES, 返回
                        return;
                    }

                    DefaultTreeModel treeModel = (DefaultTreeModel) keyTree.getModel();
                    var selectionPaths = keyTree.getSelectionModel().getSelectionPaths();
                    if (RedisFrontUtils.isNotEmpty(selectionPaths)) {
                        for (TreePath selectionPath : selectionPaths) {
                            if (selectionPath.getLastPathComponent() instanceof TreeNodeInfo treeNodeInfo) {
                                var title = treeNodeInfo.title();
                                treeNodeInfo.setTitle(title.concat(" ").concat(owner.$tr("DataSearchForm.treeNodeInfo.del.doing.message")));
                                keyTree.updateUI();
                                SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                                    deleteKeysActionPerformed(treeNodeInfo);
                                    return findTreeNodeParent(treeNodeInfo);
                                }, (e, ex) -> {
                                    if (ex == null) {
                                        treeModel.removeNodeFromParent(e);
                                    }
                                });
                            }
                        }
                    }
                });
                return delMenuItem;
            }

            private @NotNull JMenuItem getAddMenuItem() {
                var addMenuItem = new JMenuItem() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        setText(owner.$tr("DataSearchForm.addMenuItem.title"));
                    }
                };
                addMenuItem.addActionListener(_ -> {
                    var selectNode = keyTree.getLastSelectedPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        AddKeyDialog.showAddDialog(owner, redisConnectContext, treeNodeInfo.key());
                    }
                });
                return addMenuItem;
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
                            () -> RedisBasicService.service.type(redisConnectContext, treeNodeInfo.key()),
                            type -> {
                                var typeEnum = KeyTypeEnum.valueOf(type.toUpperCase());

                                if (typeEnum.equals(KeyTypeEnum.STRING)) {
                                    var value = RedisStringService.service.get(redisConnectContext, treeNodeInfo.key());
                                    SwingUtilities.invokeLater(() -> {
                                        treeNodeInfo.setMemorySize(value.length());
                                        keyTree.updateUI();
                                    });
                                }

                                if (typeEnum.equals(KeyTypeEnum.ZSET)) {
                                    var valueScanCursor = RedisZSetService.service.zscan(redisConnectContext, treeNodeInfo.key(), ScoredValueScanCursor.INITIAL);
                                    var dataList = new ArrayList<>(valueScanCursor.getValues());
                                    while (!valueScanCursor.isFinished()) {
                                        valueScanCursor = RedisZSetService.service.zscan(redisConnectContext, treeNodeInfo.key(), valueScanCursor);
                                        dataList.addAll(valueScanCursor.getValues());
                                    }
                                    if (RedisFrontUtils.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }
                                }

                                if (typeEnum.equals(KeyTypeEnum.HASH)) {

                                    var mapScanCursor = RedisHashService.service.hscan(redisConnectContext, treeNodeInfo.key(), MapScanCursor.INITIAL);
                                    var dataList = new ArrayList<>(mapScanCursor.getMap().entrySet());
                                    while (!mapScanCursor.isFinished()) {
                                        mapScanCursor = RedisHashService.service.hscan(redisConnectContext, treeNodeInfo.key(), mapScanCursor);
                                        dataList.addAll(new ArrayList<>(mapScanCursor.getMap().entrySet()));
                                    }

                                    if (RedisFrontUtils.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }
                                }

                                if (typeEnum.equals(KeyTypeEnum.LIST)) {
                                    var len = RedisListService.service.llen(redisConnectContext, treeNodeInfo.key());
                                    var start = 0;
                                    var dataList = new ArrayList<>();
                                    while (start >= len) {
                                        var stop = start + (1000 - 1);
                                        var values = RedisListService.service.lrange(redisConnectContext, treeNodeInfo.key(), start, stop);
                                        dataList.addAll(values);
                                        start += 1000;
                                    }
                                    if (RedisFrontUtils.isNotEmpty(dataList)) {
                                        SwingUtilities.invokeLater(() -> {
                                            treeNodeInfo.setMemorySize(dataList.stream().map(e -> ((String) e).getBytes().length).reduce(Integer::sum).orElse(0));
                                            keyTree.updateUI();
                                        });
                                    }

                                }

                                if (typeEnum.equals(KeyTypeEnum.SET)) {
                                    var valueScanCursor = RedisSetService.service.sscan(redisConnectContext, treeNodeInfo.key(), ValueScanCursor.INITIAL);
                                    var dataList = new ArrayList<>(valueScanCursor.getValues());
                                    while (!valueScanCursor.isFinished()) {
                                        valueScanCursor = RedisSetService.service.sscan(redisConnectContext, treeNodeInfo.key(), valueScanCursor);
                                        dataList.addAll(valueScanCursor.getValues());
                                    }
                                    if (RedisFrontUtils.isNotEmpty(dataList)) {
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
                setText(owner.$tr("DataSearchForm.currentLabel.title"));
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
                setText(owner.$tr("DataSearchForm.allLabel.title"));
            }
        };
        allLabel.setOpaque(true);
        allLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        allLabel.setSize(5, -1);
        allField.setBorder(new EmptyBorder(0, 5, 0, 0));
        allField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, allLabel);

    }


    public void loadTreeModelData(String key) {
        SyncLoadingDialog.builder(owner, owner.$tr("MainWindowForm.loading.title")).showSyncLoadingDialog(() -> {
            var scanKeysContext = scanKeysContextMap.get(redisConnectContext.getDatabase());

            if (RedisFrontUtils.isNull(scanKeysContext.getLimit())) {
                Long limit = Long.valueOf(redisConnectContext.getSetting().getLoadKeyNum());
                scanKeysContext.setLimit(limit);
            }

            var lastSearchKey = scanKeysContext.getSearchKey();
            scanKeysContext.setSearchKey(key);

            if (!key.contains("*")) {
                var all = allField.getText();
                scanKeysContext.setLimit(Long.valueOf(all));
            }

            KeyScanCursor<String> keyScanCursor = RedisBasicService.service.scan(redisConnectContext, scanKeysContext.getScanCursor(), scanKeysContext.getScanArgs());
            scanKeysContext.setScanCursor(keyScanCursor);
            log.debug("本次扫描到：{}", keyScanCursor.getKeys().size());

            var scanKeysList = new ArrayList<>(keyScanCursor.getKeys());

            //模糊匹配(模糊匹配在key数量小于 limit 的情况加全部查询出来)
            if (!loadMorePanel.isVisible() && RedisFrontUtils.equal("*", key)) {
                while (RedisFrontUtils.equal("*", key) && !keyScanCursor.isFinished()) {
                    keyScanCursor = RedisBasicService.service.scan(redisConnectContext, scanKeysContext.getScanCursor(), scanKeysContext.getScanArgs());
                    scanKeysContext.setScanCursor(keyScanCursor);
                    scanKeysList.addAll(keyScanCursor.getKeys());
                }
            }

            //数据扫描上限判断！
            if (RedisFrontUtils.equal(scanKeysContext.getSearchKey(), lastSearchKey) && RedisFrontUtils.isNotEmpty(scanKeysContext.getKeyList())) {
                if (scanKeysContext.getKeyList().size() >= 300000) {
                    System.gc();
                    throw new RedisFrontException(owner.$tr("DataSearchForm.exception.loadUpperLimit.message"));
                }
                scanKeysContext.getKeyList().addAll(scanKeysList);
            } else {
                scanKeysContext.setKeyList(scanKeysList);
            }

            var delim = redisConnectContext.getSetting().getKeySeparator();
            DefaultTreeModel treeModel = TreeUtils.toTreeModel(new HashSet<>(scanKeysContext.getKeyList()), delim);
            var finalKeyScanCursor = keyScanCursor;
            return new Turbo3<>(treeModel, finalKeyScanCursor, scanKeysContext);
        }, (turbo, e) -> {
            if (e != null) {
                owner.displayException(e);
                return;
            }
            var treeModel = turbo.getT1();
            var keyScanCursor = turbo.getT2();
            var scanKeysContext = turbo.getT3();
            currentField.setText(String.valueOf(scanKeysContext.getKeyList().size()));
            loadMoreBtn.setEnabled(!NumberUtil.equals(scanKeysContext.getKeyList().size(), Long.parseLong(allField.getText())) && !keyScanCursor.isFinished());
            keyTree.setModel(treeModel);
            keyTree.updateUI();
        });
    }

    public void scanKeysActionPerformed() {
        String searchTextFieldText = searchTextField.getText();
        if (RedisFrontUtils.isEmpty(searchTextFieldText)) {
            loadTreeModelData("*");
        } else {
            if (fuzzyMatchToggleButton.isSelected()) {
                searchTextFieldText += "*";
            }
            loadTreeModelData(searchTextFieldText);
        }
    }


    public void deleteKeysActionPerformed() {
        var treeModel = (DefaultTreeModel) keyTree.getModel();
        var selectNode = keyTree.getLastSelectedPathComponent();
        if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
            if (treeNodeInfo.getParent() != null) {
                RedisBasicService.service.del(redisConnectContext, treeNodeInfo.key());
                treeModel.removeNodeFromParent(treeNodeInfo);
            }
        }
    }

    public void deleteKeysActionPerformed(TreeNodeInfo treeNodeInfo) {
        if (treeNodeInfo.getChildCount() > 0) {
            for (int i = 0; i < treeNodeInfo.getChildCount(); i++) {
                var subNode = (TreeNodeInfo) treeNodeInfo.getChildAt(i);
                deleteKeysActionPerformed(subNode);
            }
        } else if (treeNodeInfo.getIsLeafNode()) {
            RedisBasicService.service.del(redisConnectContext, treeNodeInfo.key());
        }
    }

    private TreeNodeInfo findTreeNodeParent(TreeNodeInfo treeNodeInfo) {
        TreeNodeInfo parent = (TreeNodeInfo) treeNodeInfo.getParent();
        if (parent == null || parent.key() == null || parent.getChildCount() > 1) {
            return treeNodeInfo;
        }
        return findTreeNodeParent(parent);
    }

    private void changeDatabaseActionPerformed(int selectedIndex) {
        if (RedisFrontUtils.notEqual(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            Map<String, String> databases = RedisBasicService.service.configGet(redisConnectContext, "databases");
            var dbNum = Integer.parseInt(databases.get("databases"));
            if (dbNum > 16) {
                for (int i = 16; i < dbNum; i++) {
                    dbList.add(new DbInfo("DB" + i, i));
                }
            }
            var keySpace = RedisBasicService.service.getKeySpace(redisConnectContext);
            for (int i = 0; i < dbList.size(); i++) {
                var dbInfo = dbList.get(i);
                var value = (String) keySpace.get(dbInfo.dbName().toLowerCase());
                if (RedisFrontUtils.isNotEmpty(value)) {
                    String[] s = value.split(",");
                    String[] sub = s[0].split("=");
                    dbInfo.setDbSize(Long.valueOf(sub[1]));
                } else {
                    dbInfo.setDbSize(0L);
                }
                scanKeysContextMap.put(dbInfo.dbIndex(), new RedisScanContext<>());
                databaseComboBox.insertItemAt(dbInfo, i);
            }
        } else {
            var dbInfo = dbList.getFirst();
            scanKeysContextMap.put(dbInfo.dbIndex(), new RedisScanContext<>());
            var dbSize = RedisBasicService.service.dbSize(redisConnectContext);
            dbInfo.setDbSize(dbSize);
            databaseComboBox.insertItemAt(dbInfo, 0);
            databaseComboBox.setEnabled(false);
        }
        databaseComboBox.setSelectedIndex(selectedIndex);
    }

    private void scanKeysAndUpdateScanInfo() {
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
        contentPanel.setMinimumSize(new Dimension(-1, -1));
        contentPanel.setPreferredSize(new Dimension(300, 681));
        borderPanel.setLayout(new BorderLayout(0, 0));
        borderPanel.setMinimumSize(new Dimension(350, 199));
        contentPanel.add(borderPanel, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        borderPanel.add(panel1, BorderLayout.NORTH);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.add(panel2, BorderLayout.NORTH);
        Font databaseComboBoxFont = this.$$$getFont$$$(null, -1, -1, databaseComboBox.getFont());
        if (databaseComboBoxFont != null) databaseComboBox.setFont(databaseComboBoxFont);
        panel2.add(databaseComboBox, BorderLayout.WEST);
        Font addBtnFont = this.$$$getFont$$$(null, -1, -1, addBtn.getFont());
        if (addBtnFont != null) addBtn.setFont(addBtnFont);
        addBtn.setHorizontalAlignment(0);
        addBtn.setHorizontalTextPosition(11);
        this.$$$loadButtonText$$$(addBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataSearchForm.addBtn.title"));
        panel2.add(addBtn, BorderLayout.CENTER);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel2.add(panel3, BorderLayout.EAST);
        Font refreshBtnFont = this.$$$getFont$$$(null, -1, 16, refreshBtn.getFont());
        if (refreshBtnFont != null) refreshBtn.setFont(refreshBtnFont);
        refreshBtn.setText("");
        panel3.add(refreshBtn, BorderLayout.CENTER);
        Font deleteAllBtnFont = this.$$$getFont$$$(null, -1, 16, deleteAllBtn.getFont());
        if (deleteAllBtnFont != null) deleteAllBtn.setFont(deleteAllBtnFont);
        deleteAllBtn.setText("");
        panel3.add(deleteAllBtn, BorderLayout.EAST);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel1.add(panel4, BorderLayout.SOUTH);
        panel4.add(searchTextField, BorderLayout.CENTER);
        treePanel.setLayout(new BorderLayout(0, 0));
        borderPanel.add(treePanel, BorderLayout.CENTER);
        treePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
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
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
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
