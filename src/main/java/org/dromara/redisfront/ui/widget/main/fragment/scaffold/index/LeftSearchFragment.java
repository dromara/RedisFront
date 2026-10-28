package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.Getter;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.KeySearchMode;
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
import org.dromara.redisfront.service.*;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.memory.KeyMemoryAnalysisController;
import org.dromara.redisfront.ui.dialog.AddKeyDialog;
import org.dromara.redisfront.ui.event.AddKeySuccessEvent;
import org.dromara.redisfront.ui.event.ClickKeyTreeNodeEvent;
import org.dromara.redisfront.ui.event.KeyRenameSuccessEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    private JComboBox<KeySearchMode> searchModeComboBox;
    private JLabel searchStatus;
    private JButton cancelSearchBtn;
    private RedisKeySearchService.Progress lastSearchProgress;
    private RedisKeySearchService.Session keySearchSession;
    private int searchDatabase;
    private long searchRevision;
    private SearchTask activeSearch;
    private boolean searchDisposed;
    private KeyMemoryAnalysisController memoryAnalysis;
    private final RedisConnectContext redisConnectContext;
    private boolean rebuildingDatabases, restoringKeySelection, pruningDeletedNodes;
    private BooleanSupplier databaseChangeGuard = () -> true;

    public void setDatabaseChangeGuard(BooleanSupplier guard) {
        databaseChangeGuard = guard;
    }

    public void restoreKeySelection(TreeNodeInfo previous) {
        restoringKeySelection = true;
        try {
            if (previous != null && keyTree.getModel() != null && keyTree.getModel().getRoot() instanceof TreeNodeInfo root) {
                var nodes = root.depthFirstEnumeration();
                while (nodes.hasMoreElements())
                    if (nodes.nextElement() instanceof TreeNodeInfo node && Objects.equals(node.key(), previous.key())
                            && Objects.equals(node.getIsLeafNode(), previous.getIsLeafNode())) {
                        keyTree.setSelectionPath(new TreePath(node.getPath()));
                        return;
                    }
            }
            keyTree.clearSelection();
        } finally {
            restoringKeySelection = false;
        }
    }

    public LeftSearchFragment(RedisFrontWidget owner, RedisConnectContext redisConnectContext) {
        this.owner = owner;
        this.context = (RedisFrontContext) owner.getContext();
        this.redisConnectContext = redisConnectContext;
        $$$setupUI$$$();
        configureSearchFooter();
        int defaultDbIndex = 0;
        if (RedisFrontUtils.isNotNull(redisConnectContext.getSetting())
                && RedisFrontUtils.isNotNull(redisConnectContext.getSetting().getDefaultDatabase())) {
            defaultDbIndex = redisConnectContext.getSetting().getDefaultDatabase();
        }
        int requestedDatabase = defaultDbIndex;
        SwingUtilities.invokeLater(() -> {
            if (!searchDisposed) changeDatabaseActionPerformed(requestedDatabase);
        });
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
        addBtn.addActionListener(ignore -> AddKeyDialog.showAddDialog(owner, redisConnectContext, null));

        this.owner.getEventListener().bind(redisConnectContext.getId(), AddKeySuccessEvent.class, qsEvent -> {
            if (qsEvent instanceof AddKeySuccessEvent addKeySuccessEvent) {
                if (redisConnectContext.getId() == addKeySuccessEvent.getId()) {
                    Object message = addKeySuccessEvent.getMessage();
                    var res = JOptionPane.showConfirmDialog(owner,
                            owner.$tr("DataSearchForm.showConfirmDialog.message"),
                            owner.$tr("DataSearchForm.showConfirmDialog.title"), JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        scanKeysAndUpdateScanInfo();
                    }
                }
            }
        });

        owner.getEventListener().bind(redisConnectContext.getId(), KeyRenameSuccessEvent.class, event -> {
            if (event instanceof KeyRenameSuccessEvent renamed && renamed.getId() == redisConnectContext.getId()) {
                RedisFrontUtils.runEDT(() -> {
                    scanKeysAndUpdateScanInfo();
                });
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
        refreshBtn.addActionListener(ignore -> {
            invalidateSearch();
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

        deleteAllBtn.addActionListener(ignore -> {
            String operation = JOptionPane.showInputDialog(owner.$tr("DataSearchForm.showInputDialog.title") + "\n “flushdb” or “flushall” ");
            if (StrUtil.isEmpty(operation)) {
                return;
            }
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                if (RedisFrontUtils.equal(operation, "flushdb")) {
                    RedisBasicService.service.flushdb(redisConnectContext);
                } else if (RedisFrontUtils.equal(operation, "flushall")) {
                    RedisBasicService.service.flushall(redisConnectContext);
                }
                return null;
            }, (ret, e) -> {
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
        loadMoreBtn.setFocusable(true);
        loadMoreBtn.addActionListener(ignore -> continueKeySearch());

        databaseComboBox.addActionListener(ignore -> {
            if (rebuildingDatabases) return;
            var db = (DbInfo) databaseComboBox.getSelectedItem();
            if (RedisFrontUtils.isNull(db)) {
                return;
            }
            if (db.dbIndex() != redisConnectContext.getDatabase() && !databaseChangeGuard.getAsBoolean()) {
                rebuildingDatabases = true;
                try {
                    databaseComboBox.setSelectedIndex(redisConnectContext.getDatabase());
                } finally {
                    rebuildingDatabases = false;
                }
                return;
            }
            redisConnectContext.setDatabase(db.dbIndex());
            allField.setText(String.valueOf(db.dbSize()));
            scanKeysActionPerformed();
        });

        searchTextField = new JTextField() {
            @Override
            public void updateUI() {
                super.updateUI();
                if (searchModeComboBox != null) updateSearchHelp();
                else
                    putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, owner.$tr("DataSearchForm.searchTextField.placeholder.text"));
            }
        };

        searchTextField.addActionListener(ignore -> scanKeysActionPerformed());

        JButton searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.setFocusable(false);
        searchBtn.addActionListener(ignore -> scanKeysActionPerformed());

        searchModeComboBox = new JComboBox<>(KeySearchMode.values());
        searchModeComboBox.setOpaque(false);
        searchModeComboBox.putClientProperty(FlatClientProperties.STYLE,
                "buttonStyle: none; arrowType: chevron; minimumWidth: 0;"
                        + "borderWidth: 0; focusWidth: 0; innerFocusWidth: 0; arc: 0; padding: 0,4,0,1;"
                        + "background: #00000000; focusedBackground: #00000000; disabledBackground: #00000000;"
                        + "buttonBackground: #00000000; buttonEditableBackground: #00000000; buttonFocusedBackground: #00000000;"
                        + "buttonSeparatorWidth: 0; buttonArrowColor: $Label.disabledForeground;"
                        + "buttonHoverArrowColor: $Label.foreground; buttonPressedArrowColor: $Label.foreground;"
                        + "popupBackground: $PopupMenu.background");
        searchModeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                if (value instanceof KeySearchMode mode) setText(owner.$tr("KeySearch.mode." + mode.name()));
                return this;
            }
        });
        var metrics = searchModeComboBox.getFontMetrics(searchModeComboBox.getFont());
        searchModeComboBox.setPrototypeDisplayValue(Arrays.stream(KeySearchMode.values())
                .max(Comparator.comparingInt(mode -> metrics.stringWidth(owner.$tr("KeySearch.mode." + mode.name())))).orElse(KeySearchMode.CONTAINS));
        searchModeComboBox.addActionListener(ignore -> {
            updateSearchHelp();
            if (databaseComboBox.getSelectedItem() != null) scanKeysActionPerformed();
        });
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, searchModeComboBox);
        searchTextField.putClientProperty(FlatClientProperties.COMPONENT_FOCUS_OWNER,
                (Predicate<JComponent>) component -> component.isFocusOwner() || searchModeComboBox.isFocusOwner());
        searchModeComboBox.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                searchTextField.repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                searchTextField.repaint();
            }
        });
        updateSearchHelp();

        FlatToolBar flatToolBar = new FlatToolBar();
        flatToolBar.add(searchBtn);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, flatToolBar);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) ignore -> {
            searchTextField.setText("");
            scanKeysAndUpdateScanInfo();
        });


        keyTree = new JXTree() {
            @Override
            public String getToolTipText(MouseEvent event) {
                return null;
            }
        };
        keyTree.setFocusable(false);
        keyTree.setRootVisible(false);
        keyTree.setModel(null);
        memoryAnalysis = new KeyMemoryAnalysisController(keyTree, () -> redisConnectContext, owner::$tr);
        ToolTipManager.sharedInstance().unregisterComponent(keyTree);
        keyTree.setBorder(new EmptyBorder(5, 5, 5, 5));
        keyTree.setCellRenderer(new DefaultXTreeCellRenderer() {
            {
                setLeafIcon(Icons.TREE_KEY_ICON);
            }
        });
        keyTree.addTreeSelectionListener(ignore -> {
            if (restoringKeySelection || pruningDeletedNodes) return;
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
                memoryMenuItem.addActionListener(ignore -> {
                    var path = keyTree.getLeadSelectionPath();
                    if (path != null && path.getLastPathComponent() instanceof TreeNodeInfo node)
                        memoryAnalysis.start(node);
                });
                addPopupMenuListener(new PopupMenuListener() {
                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                        memoryMenuItem.setEnabled(!memoryAnalysis.isRunning() && keyTree.getLeadSelectionPath() != null);
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent event) {
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

                delMenuItem.addActionListener((result) -> {
                    //删除，需要进行弹框确认，生产项目，如果大规模删除，就是灾难
                    int reply = AlertUtils.showConfirmDialog(owner,
                            owner.$tr("DataSearchForm.delMenuItem.confirm"),
                            JOptionPane.YES_NO_OPTION);
                    if (reply != JOptionPane.YES_OPTION) {
                        //用户：确认的不是YES, 返回
                        return;
                    }

                    var selectionPaths = keyTree.getSelectionModel().getSelectionPaths();
                    if (RedisFrontUtils.isEmpty(selectionPaths)) {
                        return;
                    }
                    SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                        var deletedPaths = new ArrayList<TreePath>();
                        for (TreePath selectionPath : selectionPaths) {
                            if (selectionPath.getLastPathComponent() instanceof TreeNodeInfo treeNodeInfo) {
                                deleteKeysActionPerformed(treeNodeInfo);
                                deletedPaths.add(selectionPath);
                            }
                        }
                        return deletedPaths;
                    }, (deletedPaths, ex) -> {
                        if (deletedPaths != null) {
                            pruneDeletedNodes(deletedPaths);
                        }
                        if (ex != null) {
                            owner.displayException(ex);
                        }
                    });
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
                addMenuItem.addActionListener(ignore -> {
                    var selectNode = keyTree.getLastSelectedPathComponent();
                    if (selectNode instanceof TreeNodeInfo treeNodeInfo) {
                        AddKeyDialog.showAddDialog(owner, redisConnectContext, treeNodeInfo.key());
                    }
                });
                return addMenuItem;
            }


        };

        // Detached popup items are not visited when the main window changes theme.
        keyTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (MouseEvent.BUTTON3 == e.getButton() && keyTree.getSelectionCount() > 0) {
                    SwingUtilities.updateComponentTreeUI(popupMenu);
                    popupMenu.show(keyTree, e.getX(), e.getY());
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


    private String searchTr(String key) {
        return owner.$tr("KeySearch." + key);
    }

    private void configureSearchFooter() {
        searchStatus = new JLabel(searchTr("ready"));
        searchStatus.setBorder(new EmptyBorder(0, 1, 0, 0));
        cancelSearchBtn = new JButton(searchTr("cancel"));
        cancelSearchBtn.addActionListener(ignore -> {
            if (activeSearch != null) {
                activeSearch.cancelled.set(true);
                showSearchProgress("cancelling", lastSearchProgress);
            }
        });
        for (JButton action : List.of(loadMoreBtn, cancelSearchBtn)) {
            action.setIcon(null);
            action.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
            action.putClientProperty(FlatClientProperties.STYLE, "margin: 1,5,1,5");
        }
        var actions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 0, 0));
        actions.setOpaque(false);
        actions.add(loadMoreBtn);
        actions.add(cancelSearchBtn);
        var line = new JPanel(new BorderLayout(6, 0)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.height = Math.max(size.height, cancelSearchBtn.getPreferredSize().height + 3);
                return size;
            }
        };
        line.setOpaque(false);
        line.setBorder(new EmptyBorder(3, 0, 0, 0));
        line.add(searchStatus, BorderLayout.CENTER);
        line.add(actions, BorderLayout.EAST);
        loadMorePanel.removeAll();
        loadMorePanel.setOpaque(false);
        loadMorePanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        loadMorePanel.setLayout(new BorderLayout());
        loadMorePanel.add(new JSeparator(), BorderLayout.NORTH);
        loadMorePanel.add(line, BorderLayout.CENTER);
        loadMorePanel.add(memoryAnalysis.footer(), BorderLayout.SOUTH);
        loadMorePanel.setVisible(true);
        currentField.setVisible(false);
        allField.setVisible(false);
        currentField.setText("0");
        showSearchProgress("ready", new RedisKeySearchService.Progress(0, 0, false, false));
    }

    private void updateSearchHelp() {
        KeySearchMode mode = (KeySearchMode) searchModeComboBox.getSelectedItem();
        searchModeComboBox.setToolTipText(searchTr("help." + mode.name()));
        searchTextField.setToolTipText(searchTr("help." + mode.name()));
        searchTextField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, searchTr("placeholder." + mode.name()));
    }

    private RedisKeySearchService.Query currentQuery() {
        return new RedisKeySearchService.Query((KeySearchMode) searchModeComboBox.getSelectedItem(), searchTextField.getText());
    }

    private void invalidateSearch() {
        if (memoryAnalysis != null) memoryAnalysis.reset();
        if (activeSearch != null) activeSearch.cancelled.set(true);
        activeSearch = null;
        keySearchSession = null;
        searchRevision++;
        if (searchStatus != null) showSearchProgress("ready", new RedisKeySearchService.Progress(0, 0, false, false));
    }

    public void disposeSearch() {
        searchDisposed = true;
        memoryAnalysis.close();
        invalidateSearch();
    }

    public void loadTreeModelData(String pattern) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> loadTreeModelData(pattern));
            return;
        }
        startKeySearch(new RedisKeySearchService.Query(KeySearchMode.GLOB, pattern));
    }

    public void scanKeysActionPerformed() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::scanKeysActionPerformed);
            return;
        }
        startKeySearch(currentQuery());
    }

    private void startKeySearch(RedisKeySearchService.Query query) {
        if (searchDisposed) return;
        invalidateSearch();
        searchDatabase = redisConnectContext.getDatabase();
        keySearchSession = RedisKeySearchService.start(redisConnectContext, query);
        keyTree.setModel(TreeUtils.toTreeModel(Set.of(), ":"));
        currentField.setText("0");
        runKeySearch();
    }

    private void continueKeySearch() {
        if (searchDisposed || activeSearch != null) return;
        if (keySearchSession == null || searchDatabase != redisConnectContext.getDatabase() || !keySearchSession.query().equals(currentQuery())) {
            startKeySearch(currentQuery());
            return;
        }
        runKeySearch();
    }

    private void runKeySearch() {
        int requestedSize = redisConnectContext.getSetting().getLoadKeyNum();
        int pageSize = Math.max(1, Math.min(10_000, requestedSize));
        String separator = redisConnectContext.getSetting().getKeySeparator();
        if (separator == null || separator.isEmpty()) separator = ":";
        activeSearch = new SearchTask(keySearchSession, searchRevision, searchDatabase, pageSize, separator);
        loadMorePanel.setVisible(true);
        showSearchProgress("scanning", keySearchSession.progress());
        activeSearch.execute();
    }

    private void showSearchProgress(String state, RedisKeySearchService.Progress progress) {
        lastSearchProgress = progress;
        if (state.equals("complete") && keySearchSession != null && keySearchSession.query().exact())
            state = "exactComplete";
        searchStatus.setText(String.format(searchTr(state), progress.matches(), progress.batches()));
        var query = keySearchSession == null ? currentQuery() : keySearchSession.query();
        String total = allField.getText().isEmpty() ? "—" : allField.getText();
        String details = String.format(searchTr("details"), escapeTooltip(total), progress.batches(),
                escapeTooltip(searchTr("mode." + query.mode().name())), escapeTooltip(query.text()));
        searchStatus.setToolTipText(details);
        loadMorePanel.setToolTipText(details);
        boolean scanning = state.equals("scanning");
        boolean stopping = state.equals("cancelling");
        boolean resumable = (state.equals("partial") || state.equals("paused") || state.equals("failed"))
                && !progress.complete() && !progress.limited();
        cancelSearchBtn.setVisible(scanning || stopping);
        cancelSearchBtn.setEnabled(scanning);
        loadMoreBtn.setVisible(resumable);
        loadMoreBtn.setEnabled(resumable);
        loadMoreBtn.setText(owner.$tr("DataSearchForm.loadMoreBtn.continue.title"));
        currentField.setText(String.valueOf(progress.matches()));
        loadMorePanel.revalidate();
        loadMorePanel.repaint();
    }

    private static String escapeTooltip(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private record SearchResult(RedisKeySearchService.Snapshot snapshot, DefaultTreeModel tree, Exception failure) {
    }

    private final class SearchTask extends SwingWorker<SearchResult, RedisKeySearchService.Progress> {
        private final RedisKeySearchService.Session session;
        private final long revision;
        private final int database, pageSize;
        private final String separator;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final TreeNodeInfo previousSelection;
        private long lastPublished;

        private SearchTask(RedisKeySearchService.Session session, long revision, int database, int pageSize, String separator) {
            this.session = session;
            this.revision = revision;
            this.database = database;
            this.pageSize = pageSize;
            this.separator = separator;
            previousSelection = keyTree.getLastSelectedPathComponent() instanceof TreeNodeInfo node ? node : null;
        }

        private boolean current() {
            return !searchDisposed && activeSearch == this && revision == searchRevision && database == redisConnectContext.getDatabase();
        }

        @Override
        protected SearchResult doInBackground() {
            Exception failure = null;
            try {
                session.readPage(pageSize, cancelled::get, progress -> {
                    long now = System.nanoTime();
                    if (lastPublished == 0 || now - lastPublished >= 100_000_000L || progress.complete() || progress.limited()) {
                        publish(progress);
                        lastPublished = now;
                    }
                });
            } catch (Exception error) {
                failure = error;
            }
            var snapshot = session.snapshot();
            return new SearchResult(snapshot, TreeUtils.toTreeModel(new LinkedHashSet<>(snapshot.keys()), separator), failure);
        }

        @Override
        protected void process(List<RedisKeySearchService.Progress> chunks) {
            if (current() && !cancelled.get() && !chunks.isEmpty()) showSearchProgress("scanning", chunks.getLast());
        }

        @Override
        protected void done() {
            if (!current()) return;
            activeSearch = null;
            try {
                var result = get();
                var progress = result.snapshot().progress();
                keyTree.setModel(result.tree());
                restoreKeySelection(previousSelection);
                String state = result.failure() != null ? "failed" : progress.limited() ? "limited"
                                                                     : progress.complete() ? "complete" : cancelled.get() ? "paused" : "partial";
                showSearchProgress(state, progress);
                if (result.failure() != null) owner.displayException(result.failure());
            } catch (Exception error) {
                showSearchProgress("failed", session.progress());
                owner.displayException(error);
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

    private void pruneDeletedNodes(List<TreePath> deletedPaths) {
        var treeModel = (DefaultTreeModel) keyTree.getModel();
        var removeRoots = new LinkedHashSet<TreeNodeInfo>();
        for (TreePath deletedPath : deletedPaths) {
            if (deletedPath.getLastPathComponent() instanceof TreeNodeInfo treeNodeInfo) {
                var removeRoot = findTreeNodeParent(treeNodeInfo);
                boolean coveredByExisting = false;
                for (TreeNodeInfo existingRoot : removeRoots) {
                    if (existingRoot.isNodeAncestor(removeRoot)) {
                        coveredByExisting = true;
                        break;
                    }
                }
                if (!coveredByExisting) {
                    removeRoots.removeIf(existingRoot -> removeRoot.isNodeAncestor(existingRoot));
                    removeRoots.add(removeRoot);
                }
            }
        }
        //移除节点会让 JTree 把选中态挪到相邻节点上，屏蔽选中监听，避免去加载刚删除的 key
        pruningDeletedNodes = true;
        try {
            for (TreeNodeInfo removeRoot : removeRoots) {
                treeModel.removeNodeFromParent(removeRoot);
            }
        } finally {
            pruningDeletedNodes = false;
        }
    }

    private void changeDatabaseActionPerformed(int selectedIndex) {
        if (searchDisposed) return;
        SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
            var databases = new ArrayList<DbInfo>();
            if (redisConnectContext.getRedisMode() == RedisMode.CLUSTER) {
                var db = new DbInfo("DB0", 0);
                db.setDbSize(RedisBasicService.service.dbSize(redisConnectContext));
                databases.add(db);
            } else {
                int count;
                try {
                    count = Integer.parseInt(RedisBasicService.service.configGet(redisConnectContext, "databases").getOrDefault("databases", "16"));
                } catch (RuntimeException restricted) {
                    count = 16;
                }
                var keyspace = RedisBasicService.service.getKeySpace(redisConnectContext);
                for (int i = 0; i < Math.min(count, 10000); i++) {
                    var db = new DbInfo("DB" + i, i);
                    db.setDbSize(0L);
                    Object info = keyspace.get("db" + i);
                    if (info != null) for (String value : info.toString().split(",")) {
                        if (value.startsWith("keys=")) db.setDbSize(Long.parseLong(value.substring(5)));
                    }
                    databases.add(db);
                }
            }
            return databases;
        }, (databases, error) -> {
            if (searchDisposed) return;
            if (error != null) {
                owner.displayException(error);
                return;
            }
            rebuildingDatabases = true;
            try {
                databaseComboBox.removeAllItems();
                for (DbInfo database : databases) {
                    databaseComboBox.addItem(database);
                }
                databaseComboBox.setSelectedIndex(Math.max(0, Math.min(selectedIndex, databases.size() - 1)));
                databaseComboBox.setEnabled(redisConnectContext.getRedisMode() != RedisMode.CLUSTER);
            } finally {
                rebuildingDatabases = false;
            }
            for (var listener : databaseComboBox.getActionListeners())
                listener.actionPerformed(new ActionEvent(databaseComboBox, ActionEvent.ACTION_PERFORMED, "loaded"));
        });
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

    /**
     * @noinspection ALL
     */
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

    static List<long[]> listPageRanges(long len, int pageSize) {
        if (len <= 0) {
            return List.of();
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be > 0");
        }
        List<long[]> ranges = new ArrayList<>();
        long start = 0;
        while (start < len) {
            long stop = Math.min(start + pageSize - 1L, len - 1L);
            ranges.add(new long[]{start, stop});
            start += pageSize;
        }
        return ranges;
    }

}
