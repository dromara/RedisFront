package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.util.StringUtils;
import com.google.gson.Gson;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.quickswing.ui.swing.AnimateButton;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.HashTableModel;
import org.dromara.redisfront.model.table.SortedSetTableModel;
import org.dromara.redisfront.model.table.StreamTableModel;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.model.turbo.Turbo2;
import org.dromara.redisfront.model.turbo.Turbo3;
import org.dromara.redisfront.service.*;
import org.dromara.redisfront.ui.components.editor.TextEditor;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.scanner.core.*;
import org.dromara.redisfront.ui.components.scanner.model.ScanDataResult;
import org.dromara.redisfront.ui.dialog.AddOrUpdateValueDialog;
import org.dromara.redisfront.ui.event.KeyDeleteSuccessEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raven.toast.Notifications;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * DataViewForm
 *
 * @author Jin
 */
public class RightViewFragment {

    private static final Logger log = LoggerFactory.getLogger(RightViewFragment.class);

    private JPanel contentPanel;
    private JTextField keyField;
    private JPanel bodyPanel;
    private JPanel valueViewPanel;
    private JPanel basicPanel;
    private JLabel keyTypeLabel;
    private AnimateButton delBtn;
    private AnimateButton refBtn;
    private JLabel keyLabel;
    private JLabel lengthLabel;
    private JLabel keySizeLabel;
    private AnimateButton valueUpdateSaveBtn;
    private AnimateButton saveBtn;
    private JTextField ttlField;
    private JTextField tableSearchField;
    private JButton tableAddBtn;
    private JButton tableDelBtn;
    private JPanel tableViewPanel;
    private JSplitPane dataSplitPanel;
    private JTable dataTable;
    private JButton tableRefreshBtn;
    private JPanel dataPanel;
    private JScrollPane tableScorePanel;
    private JPanel pagePanel;
    private JTextField currentCountField;
    private JTextField allCountField;
    private JButton loadMoreBtn;
    private TextEditor textEditor;
    private JTextField fieldOrScoreField;
    private JComboBox<String> jComboBox;

    private final RedisConnectContext redisConnectContext;
    private final TreeNodeInfo treeNodeInfo;
    private final RedisFrontWidget owner;

    private final StringRedisDataScanner stringDataFetcher;
    private final HashRedisDataScanner hashDataFetcher;
    private final SetRedisDataScanner setDataFetcher;
    private final ZSetRedisDataScanner zSetDataFetcher;
    private final ListRedisDataScanner listDataFetcher;
    private final StreamRedisDataScanner streamDataFetcher;
    private final Gson gson = new Gson();
    private String lastKeyName;
    private Long lastKeyTTL;
    private KeyTypeEnum keyTypeEnum;
    private String lastSyntaxStyle;


    public RightViewFragment(RedisConnectContext redisConnectContext, TreeNodeInfo treeNodeInfo, RedisFrontWidget owner) {
        this.owner = owner;
        this.redisConnectContext = redisConnectContext;
        this.treeNodeInfo = treeNodeInfo;

        this.stringDataFetcher = new StringRedisDataScanner(redisConnectContext, this::refreshStringUI);

        this.hashDataFetcher = new HashRedisDataScanner(redisConnectContext, treeNodeInfo.key(), this::refreshTableUI, owner.getResourceBundle());

        this.setDataFetcher = new SetRedisDataScanner(redisConnectContext, treeNodeInfo.key(), this::refreshTableUI, owner.getResourceBundle());

        this.zSetDataFetcher = new ZSetRedisDataScanner(redisConnectContext, treeNodeInfo.key(), this::refreshTableUI, owner.getResourceBundle());

        this.listDataFetcher = new ListRedisDataScanner(redisConnectContext, treeNodeInfo.key(), this::refreshTableUI, owner.getResourceBundle());

        this.streamDataFetcher = new StreamRedisDataScanner(redisConnectContext, treeNodeInfo.key(), this::refreshTableUI, owner.getResourceBundle());

        $$$setupUI$$$();
        this.initialize();
    }

    private void refreshStringUI(Turbo2<Long, String> turbo) {
        tableViewPanel.setVisible(false);
        valueUpdateSaveBtn.setEnabled(true);
        lengthLabel.setText("Length: " + turbo.getT1());
        keySizeLabel.setText("Size: " + RedisFrontUtils.getDataSize(turbo.getT2()));
        dataSplitPanel.setDividerSize(0);
        jsonValueFormat(turbo.getT2());
    }

    private <T extends TableModel> void refreshTableUI(ScanDataResult<T> scanData) {
        keyLabel.setText(owner.$tr("DataViewForm.keyLabel.title"));
        keyLabel.setOpaque(true);
        keyLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        tableViewPanel.setVisible(true);
        dataTable.setModel(scanData.getData());
        lengthLabel.setText("Length: " + scanData.getLen());
        keySizeLabel.setText("Size: " + scanData.getDataSize());
        currentCountField.setText(scanData.getLoadSize());
        allCountField.setText(String.valueOf(scanData.getLen()));
        if (scanData.getIsFinished()) {
            loadMoreBtn.setText(owner.$tr("DataViewForm.loadMoreBtn.complete.title"));
            loadMoreBtn.setEnabled(false);
        } else {
            loadMoreBtn.setText(owner.$tr("DataViewForm.loadMoreBtn.title"));
            loadMoreBtn.setEnabled(true);
        }
        dataPanel.remove(dataSplitPanel);
        dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
    }

    private void initialize() {
        dataTable.setShowHorizontalLines(true);
        dataTable.setShowVerticalLines(true);
        dataTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        dataTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        dataTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        dataTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var row = dataTable.getSelectedRow();
                if (e.getButton() == MouseEvent.BUTTON1 && row != -1) {
                    tableDelBtn.setEnabled(true);
                    if (dataTable.getModel() instanceof SortedSetTableModel) {
                        var value = dataTable.getValueAt(row, 2);
                        var score = dataTable.getValueAt(row, 1);
                        RedisFrontUtils.runEDT(() -> {
                            keyLabel.setText(owner.$tr("DataViewForm.keyLabel.score.title"));
                            fieldOrScoreField.setText(score.toString());
                            valueUpdateSaveBtn.setEnabled(true);
                            jsonValueFormat((String) value);
                        });
                    } else if (dataTable.getModel() instanceof HashTableModel) {
                        var value = dataTable.getValueAt(row, 1);
                        var key = dataTable.getValueAt(row, 0);
                        RedisFrontUtils.runEDT(() -> {
                            keyLabel.setText(owner.$tr("DataViewForm.keyLabel.title"));
                            fieldOrScoreField.setText(key.toString());
                            valueUpdateSaveBtn.setEnabled(true);
                            jsonValueFormat((String) value);
                        });
                    } else if (dataTable.getModel() instanceof StreamTableModel) {
                        valueUpdateSaveBtn.setEnabled(true);
                        var value = dataTable.getValueAt(row, 2);
                        RedisFrontUtils.runEDT(() -> {
                            try {
                                String prettyStr = JSONUtil.toJsonPrettyStr(value);
                                textEditor.setText(prettyStr);
                            } catch (Exception ex) {
                                //json格式化异常
                                textEditor.setText(value.toString());
                            }
                        });
                    } else {
                        var value = dataTable.getValueAt(row, 1);
                        RedisFrontUtils.runEDT(() -> {
                            valueUpdateSaveBtn.setEnabled(true);
                            jsonValueFormat((String) value);
                        });
                    }
                } else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    tableDelBtn.setEnabled(true);
                    updateValueActionPerformed();
                } else {
                    tableDelBtn.setEnabled(false);
                }
            }


        });
        dataTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                tableDelBtn.setEnabled(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                var row = dataTable.getSelectedRow();
                if (row == -1) {
                    tableDelBtn.setEnabled(false);
                }
            }
        });
    }

    private void jsonValueFormat(String value) {
        if (JSONUtil.isTypeJSON(value)) {
            try {
                String prettyStr = JSONUtil.toJsonPrettyStr(value);
                textEditor.setText(prettyStr);
                jComboBox.setSelectedIndex(1);
            } catch (JSONException e) {
                //json格式化异常
                textEditor.setText(value);
            }
        } else {
            textEditor.setText(value);
        }
    }

    public JPanel contentPanel() {
        return contentPanel;
    }

    private void reloadAllActionPerformed() {
        String keyType = keyTypeLabel.getText();
        if (RedisFrontUtils.notEqual(keyType, "none")) {
            reloadTableDataActionPerformed(true);
        } else {
            ttlField.setText("-2");
            owner.displayMessage("Key not found", owner.$tr("DataViewForm.showInformationDialog.message"));
        }
    }

    private void reloadTableDataActionPerformed(Boolean reset) {
        SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
            var key = keyField.getText();
            this.lastKeyName = key;
            this.lastKeyTTL = RedisBasicService.service.ttl(redisConnectContext, key);
            var keyType = keyTypeLabel.getText();
            var keyTypeEnum = KeyTypeEnum.valueOf(keyType.toUpperCase());
            String searchText = tableSearchField.getText();
            switch (keyTypeEnum) {
                case ZSET -> {
                    if (reset) {
                        zSetDataFetcher.reset(key);
                    }
                    zSetDataFetcher.fetchData(searchText);
                }
                case HASH -> {
                    if (reset) {
                        hashDataFetcher.reset(key);
                    }
                    hashDataFetcher.fetchData(searchText);
                }
                case LIST -> {
                    if (reset) {
                        listDataFetcher.reset(key);
                    }
                    listDataFetcher.fetchData(searchText);
                }
                case SET -> {
                    if (reset) {
                        setDataFetcher.reset(key);
                    }
                    setDataFetcher.fetchData(searchText);
                }
                case STREAM -> {
                    if (reset) {
                        streamDataFetcher.reset(key);
                    }
                    streamDataFetcher.fetchData(searchText);
                }
                default -> stringDataFetcher.fetchData(key);
            }
            return 0;
        }, (_, e) -> {
            if (e != null) {
                Notifications.getInstance().show(Notifications.Type.ERROR, e.getMessage());
                return;
            }
            refreshUI();
        });
    }

    public void fetchDataActionPerformed() {
        String key = treeNodeInfo.key();
        var type = RedisBasicService.service.type(redisConnectContext, key);
        if (RedisFrontUtils.notEqual(type, "none")) {
            keyTypeEnum = KeyTypeEnum.valueOf(type.toUpperCase());
            lastKeyTTL = RedisBasicService.service.ttl(redisConnectContext, key);
            lastKeyName = key;
            switch (keyTypeEnum) {
                case ZSET -> zSetDataFetcher.fetchData(null);
                case HASH -> hashDataFetcher.fetchData(null);
                case SET -> setDataFetcher.fetchData(null);
                case LIST -> listDataFetcher.fetchData(null);
                case STREAM -> streamDataFetcher.fetchData(null);
                default -> stringDataFetcher.fetchData(key);
            }
        } else {
            throw new RuntimeException("Key [ " + key + " ] load failed, please check the key!");
        }
    }

    public void refreshUI() {
        this.fieldOrScoreField.setVisible(keyTypeEnum == KeyTypeEnum.ZSET || keyTypeEnum == KeyTypeEnum.HASH);
        this.keyTypeLabel.setText(keyTypeEnum.typeName());
        this.keyTypeLabel.setBackground(keyTypeEnum.color());
        this.ttlField.setText(lastKeyTTL.toString());
        this.keyField.setText(lastKeyName);
        switch (keyTypeEnum) {
            case ZSET -> zSetDataFetcher.refreshUI();
            case HASH -> hashDataFetcher.refreshUI();
            case SET -> setDataFetcher.refreshUI();
            case LIST -> listDataFetcher.refreshUI();
            case STREAM -> streamDataFetcher.refreshUI();
            default -> stringDataFetcher.refreshUI();
        }
    }

    private void updateValueActionPerformed() {
        var row = dataTable.getSelectedRow();
        SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
            if (row != -1) {
                var keyType = keyTypeLabel.getText();
                KeyTypeEnum keyTypeEnum = KeyTypeEnum.valueOf(keyType.toUpperCase());
                switch (keyTypeEnum) {
                    case ZSET -> {
                        var score = (Double) dataTable.getValueAt(row, 1);
                        var value = (String) dataTable.getValueAt(row, 2);
                        return new Turbo3<>(keyTypeEnum, score.toString(), value);
                    }
                    case HASH -> {
                        var key = (String) dataTable.getValueAt(row, 0);
                        var value = (String) dataTable.getValueAt(row, 1);
                        return new Turbo3<>(keyTypeEnum, key, value);
                    }
                    case LIST, SET -> {
                        var value = (String) dataTable.getValueAt(row, 1);
                        return new Turbo3<KeyTypeEnum, String, String>(keyTypeEnum, null, value);
                    }
                }
            }
            return null;
        }, (turbo, e) -> {
            if (e != null) {
                Notifications.getInstance().show(Notifications.Type.INFO, e.getMessage());
            } else {
                switch (turbo.getT1()) {
                    case ZSET, LIST, SET, HASH ->
                            AddOrUpdateValueDialog.showDialog(owner, owner.$tr("DataViewForm.showAddOrUpdateItemDialog.title"), keyField.getText(), turbo.getT2(), turbo.getT3(), redisConnectContext, keyTypeEnum, () -> {
                            });
                }
            }
        });
    }

    private void createUIComponents() {
        bodyPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                if (RedisFrontUtils.isNotNull(dataTable)) {
                    initialize();
                }
            }
        };

        valueViewPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
            }

            {
                setLayout(new BorderLayout());
            }
        };

        var jToolBar = new JToolBar();
        jToolBar.setBorder(new EmptyBorder(5, 0, 0, 10));

        fieldOrScoreField = new JTextField();
        keyLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.keyLabel.title"));
            }
        };
        keyLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
        fieldOrScoreField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, keyLabel);
        jToolBar.add(fieldOrScoreField);

        jComboBox = new JComboBox<>();
        jComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_NONE);
        jComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_JSON);
        jComboBox.addActionListener(_ -> {
            var item = jComboBox.getSelectedItem();
            String value = textEditor.getText();
            if (item instanceof String itemValue) {
                if (RedisFrontUtils.equal(itemValue, SyntaxConstants.SYNTAX_STYLE_JSON)) {
                    this.lastSyntaxStyle = SyntaxConstants.SYNTAX_STYLE_JSON;
                    try {
                        if (JSONUtil.isTypeJSON(value)) {
                            String prettyStr = JSONUtil.toJsonPrettyStr(value);
                            textEditor.setText(prettyStr);
                        } else {
                            value = gson.fromJson(value, String.class);
                            if (JSONUtil.isTypeJSON(value)) {
                                textEditor.setText(JSONUtil.toJsonPrettyStr(value));
                            }
                        }
                    } catch (JSONException e) {
                        textEditor.setText(value);
                    }
                } else {
                    if (StringUtils.isEmpty(value)) {
                        return;
                    }
                    if (this.lastSyntaxStyle.equals(SyntaxConstants.SYNTAX_STYLE_JSON)) {
                        if (JSONUtil.isTypeJSON(value)) {
                            value = JSONUtil.parse(value).toJSONString(0);
                            value = gson.toJson(value);
                        }
                        textEditor.setText(value);
                    }
                }
            }
        });
        jComboBox.setBackground(UIManager.getColor("FlatEditorPane.background"));
        jToolBar.add(jComboBox);
        valueUpdateSaveBtn = new AnimateButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.valueUpdateSaveBtn.title"));
                setToolTipText(owner.$tr("DataViewForm.valueUpdateSaveBtn.toolTip.text"));
                setBackground(UIManager.getColor("RedisFront.animateButton.background"));
            }
        };
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);

        InputMap inputMap = valueUpdateSaveBtn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyStroke, "triggerSave");

        ActionMap actionMap = valueUpdateSaveBtn.getActionMap();
        actionMap.put("triggerSave", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                valueUpdateSaveBtn.doClick();
            }
        });
        valueUpdateSaveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        valueUpdateSaveBtn.setEffectColor(Color.decode("#389FD6"));
        valueUpdateSaveBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        valueUpdateSaveBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        valueUpdateSaveBtn.setEnabled(false);
        valueUpdateSaveBtn.setIcon(Icons.SAVE_ICON);
        valueUpdateSaveBtn.addActionListener((_) -> {
            var keyType = keyTypeLabel.getText();
            KeyTypeEnum typeEnum = KeyTypeEnum.valueOf(keyType.toUpperCase());
            var key = keyField.getText();
            var newValue = textEditor.getText();
            if (StrUtil.isNotEmpty(lastSyntaxStyle)) {
                int i = JOptionPane.showConfirmDialog(owner, owner.$tr("DataViewForm.valueUpdateSaveBtn.syntaxStyleTip"), owner.$tr("DataViewForm.valueUpdateSaveBtn.syntaxStyleTipTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (i == JOptionPane.CANCEL_OPTION) {
                    return;
                }
            }
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                if (typeEnum.equals(KeyTypeEnum.STRING)) {
                    RedisBasicService.service.del(redisConnectContext, key);
                    RedisStringService.service.set(redisConnectContext, key, newValue);
                } else {
                    var row = dataTable.getSelectedRow();
                    if (row != -1) {

                        switch (typeEnum) {
                            case HASH -> {
                                var fieldOrScore = fieldOrScoreField.getText();
                                var filed = (String) dataTable.getValueAt(row, 0);
                                RedisHashService.service.hdel(redisConnectContext, key, filed);
                                RedisHashService.service.hset(redisConnectContext, key, fieldOrScore, newValue);
                            }
                            case ZSET -> {
                                var fieldOrScore = fieldOrScoreField.getText();
                                var value = (String) dataTable.getValueAt(row, 2);
                                RedisZSetService.service.zrem(redisConnectContext, key, value);
                                RedisZSetService.service.zadd(redisConnectContext, key, Double.parseDouble(fieldOrScore), newValue);
                            }
                            case LIST -> {
                                var value = (String) dataTable.getValueAt(row, 1);
                                RedisListService.service.lrem(redisConnectContext, key, 1, value);
                                RedisListService.service.lpush(redisConnectContext, key, newValue);
                            }
                            case SET -> {
                                var value = (String) dataTable.getValueAt(row, 1);
                                RedisSetService.service.srem(redisConnectContext, key, value);
                                RedisSetService.service.sadd(redisConnectContext, key, newValue);
                            }
                        }
                    }
                }
                return null;
            }, (_, e) -> {
                if (e == null) {
                    Notifications.getInstance().show(Notifications.Type.INFO, owner.$tr("DataViewForm.showInformationDialog.updateSuccess.message"));
                    return;
                }
                Notifications.getInstance().show(Notifications.Type.INFO, owner.$tr("DataViewForm.showInformationDialog.updateSuccess.message"));
            });
        });
        jToolBar.add(valueUpdateSaveBtn);

        valueViewPanel.add(new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setLayout(new BorderLayout());
                setBorder(new FlatEmptyBorder(0, 0, 5, 0));
                add(jToolBar, BorderLayout.SOUTH);
            }
        }, BorderLayout.NORTH);

        textEditor = TextEditor.newInstance();
        valueViewPanel.add(new JPanel() {
            {
                setLayout(new BorderLayout());
                add(textEditor, BorderLayout.CENTER);
            }
        }, BorderLayout.CENTER);

        basicPanel = new JPanel();

        loadMoreBtn = new JButton();
        loadMoreBtn.setIcon(Icons.LOAD_MORE_ICON);
        loadMoreBtn.addActionListener((_) -> {
            if (loadMoreBtn.isEnabled()) {
                loadMoreBtn.setEnabled(false);
                reloadTableDataActionPerformed(false);
            }
        });
        tableScorePanel = new JScrollPane();
        tableScorePanel.setPreferredSize(new Dimension(500, 190));

        tableViewPanel = new JPanel();
        tableViewPanel.setVisible(false);
        tableViewPanel.add(new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setLayout(new BorderLayout());
                setBorder(new FlatEmptyBorder(5, 0, 0, 0));
                add(new JSeparator(), BorderLayout.CENTER);
            }
        }, BorderLayout.SOUTH);

        tableViewPanel.add(new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setLayout(new BorderLayout());
                setBorder(new FlatEmptyBorder(0, 0, 5, 0));
                add(new JSeparator(), BorderLayout.CENTER);
            }
        }, BorderLayout.NORTH);

        keyTypeLabel = new JLabel();
        keyTypeLabel.setOpaque(true);
        keyTypeLabel.setForeground(Color.WHITE);
        keyTypeLabel.setBorder(new EmptyBorder(2, 3, 2, 3));

        var ttlLabel = new JLabel();
        ttlLabel.setText("TTL");
        ttlLabel.setOpaque(true);
        ttlLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        ttlField = new JTextField();
        ttlField.setSize(5, -1);
        ttlField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, ttlLabel);
        delBtn = new AnimateButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.delBtn.title"));
                setToolTipText(owner.$tr("DataViewForm.delBtn.toolTip.Text"));
                setBackground(UIManager.getColor("RedisFront.animateButton.background"));
            }
        };
        delBtn.setText(owner.$tr("DataViewForm.delBtn.title"));
        delBtn.setIcon(Icons.DELETE_A_ICON);
        delBtn.setEffectColor(Color.red);
        delBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        delBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        delBtn.setArcHeight(10);
        delBtn.setArcWidth(10);
        delBtn.addActionListener(_ -> {
            var key = keyField.getText();
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() ->
                            RedisBasicService.service.del(redisConnectContext, key),
                    (_, e) -> {
                        if (e == null) {
                            owner.getContext().getEventBus().publish(new KeyDeleteSuccessEvent(key, redisConnectContext.getId()));
                            return;
                        }
                        Notifications.getInstance().show(Notifications.Type.ERROR, "key删除失败！");
                    });
        });

        refBtn = new AnimateButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.refBtn.title"));
                setToolTipText(owner.$tr("DataViewForm.refBtn.toolTip.Text"));
                setBackground(UIManager.getColor("RedisFront.animateButton.background"));
            }
        };
        refBtn.setText(owner.$tr("DataViewForm.refBtn.title"));
        refBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        refBtn.setIcon(Icons.REFRESH_A_ICON);
        refBtn.setEffectColor(Color.GREEN);
        refBtn.setArcHeight(10);
        refBtn.setArcWidth(10);
        refBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        refBtn.addActionListener(_ -> reloadAllActionPerformed());

        saveBtn = new AnimateButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.saveBtn.title"));
                setToolTipText(owner.$tr("DataViewForm.saveBtn.toolTip.Text"));
                setBackground(UIManager.getColor("RedisFront.animateButton.background"));
            }
        };
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setText(owner.$tr("DataViewForm.saveBtn.title"));
        saveBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        saveBtn.setIcon(Icons.SAVE_A_ICON);
        saveBtn.setEffectColor(Color.blue);
        saveBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        saveBtn.setArcHeight(10);
        saveBtn.setArcWidth(10);

        saveBtn.addActionListener((_) -> {
            String ttl = ttlField.getText();
            String key = keyField.getText();
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                if (RedisFrontUtils.notEqual(key, lastKeyName)) {
                    lastKeyName = key;
                    RedisBasicService.service.rename(redisConnectContext, lastKeyName, key);
                }
                if (RedisFrontUtils.notEqual(ttl, lastKeyTTL.toString())) {
                    lastKeyTTL = Long.valueOf(ttl);
                    RedisBasicService.service.expire(redisConnectContext, key, Long.valueOf(ttl));
                }
                return null;
            }, (_, e) -> {
                if (e == null) {
                    reloadAllActionPerformed();
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, "key保存失败！");
                }
            });
        });

        tableSearchField = new JTextField() {
            @Override
            public void updateUI() {
                super.updateUI();
                putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, owner.$tr("DataViewForm.tableSearchField.placeholder.text"));
            }
        };

        var searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.addActionListener(_ -> reloadTableDataActionPerformed(true));
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) _ -> {
            tableSearchField.setText("");
            reloadTableDataActionPerformed(true);
        });
        tableAddBtn = new JButton(Icons.PLUS_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.tableAddBtn.title"));
            }
        };

        tableAddBtn.addActionListener((_) -> {
            var keyType = keyTypeLabel.getText();
            KeyTypeEnum keyTypeEnum = KeyTypeEnum.valueOf(keyType.toUpperCase());
            String key = keyField.getText();
            switch (keyTypeEnum) {
                case ZSET ->
                        AddOrUpdateValueDialog.showDialog(owner, "ZSET", key, null, null, redisConnectContext, keyTypeEnum, tableRefreshBtn::doClick);
                case HASH ->
                        AddOrUpdateValueDialog.showDialog(owner, "HASH", key, null, null, redisConnectContext, keyTypeEnum, tableRefreshBtn::doClick);
                case LIST ->
                        AddOrUpdateValueDialog.showDialog(owner, "LIST", key, null, null, redisConnectContext, keyTypeEnum, tableRefreshBtn::doClick);
                case SET ->
                        AddOrUpdateValueDialog.showDialog(owner, "SET", key, null, null, redisConnectContext, keyTypeEnum, tableRefreshBtn::doClick);
                case STREAM ->
                        AddOrUpdateValueDialog.showDialog(owner, "STREAM", key, null, null, redisConnectContext, keyTypeEnum, tableRefreshBtn::doClick);
            }
        });

        tableDelBtn = new JButton(Icons.DELETE_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.tableDelBtn.title"));
            }
        };
        tableDelBtn.addActionListener(_ -> {
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                var row = dataTable.getSelectedRow();
                if (row != -1) {
                    tableDelBtn.setEnabled(false);
                    var keyType = keyTypeLabel.getText();
                    KeyTypeEnum keyTypeEnum = KeyTypeEnum.valueOf(keyType.toUpperCase());
                    String key = keyField.getText();

                    switch (keyTypeEnum) {
                        case ZSET -> {
                            var value = (String) dataTable.getValueAt(row, 2);
                            RedisZSetService.service.zrem(redisConnectContext, key, value);
                            return (Runnable) () -> {
                                fieldOrScoreField.setText("");
                                textEditor.clear();
                                valueUpdateSaveBtn.setEnabled(false);
                            };
                        }
                        case HASH -> {
                            var field = (String) dataTable.getValueAt(row, 0);
                            RedisHashService.service.hdel(redisConnectContext, key, field);
                            return (Runnable) () -> {
                                fieldOrScoreField.setText("");
                                textEditor.clear();
                                valueUpdateSaveBtn.setEnabled(false);
                            };
                        }
                        case LIST -> {
                            var value = (String) dataTable.getValueAt(row, 1);
                            RedisListService.service.lrem(redisConnectContext, key, 1, value);
                            return (Runnable) () -> {
                                textEditor.clear();
                                valueUpdateSaveBtn.setEnabled(false);
                            };
                        }
                        case SET -> {
                            var value = (String) dataTable.getValueAt(row, 1);
                            RedisSetService.service.srem(redisConnectContext, key, value);
                            return (Runnable) () -> {
                                textEditor.clear();
                                valueUpdateSaveBtn.setEnabled(false);
                            };
                        }
                        case STREAM -> {
                            var id = (String) dataTable.getValueAt(row, 1);
                            RedisStreamService.service.xdel(redisConnectContext, key, id);
                            return (Runnable) () -> {
                                fieldOrScoreField.setText("");
                                textEditor.clear();
                                valueUpdateSaveBtn.setEnabled(false);
                            };
                        }
                    }
                }
                return (Runnable) () -> {
                };
            }, (runnable, e) -> {
                if (e != null) {
                    owner.displayException(e);
                } else {
                    runnable.run();
                    tableDelBtn.setEnabled(false);
                    reloadTableDataActionPerformed(true);
                }
            });


        });
        tableRefreshBtn = new JButton(Icons.REFRESH_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.tableRefreshBtn.title"));
            }
        };

        tableRefreshBtn.addActionListener(_ -> {
            reloadTableDataActionPerformed(true);
        });

        var pageNumLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.pageNumLabel.title"));
            }
        };
        pageNumLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        currentCountField = new JTextField();
        currentCountField.setEnabled(false);
        currentCountField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, pageNumLabel);

        var pageSizeLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.pageSizeLabel.title"));
            }
        };
        pageSizeLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        allCountField = new JTextField();
        allCountField.setEnabled(false);
        allCountField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, pageSizeLabel);

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
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setMinimumSize(new Dimension(-1, -1));
        contentPanel.setPreferredSize(new Dimension(-1, -1));
        bodyPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(bodyPanel, BorderLayout.CENTER);
        basicPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        bodyPanel.add(basicPanel, BorderLayout.NORTH);
        Font keyTypeLabelFont = this.$$$getFont$$$(null, Font.BOLD, -1, keyTypeLabel.getFont());
        if (keyTypeLabelFont != null) keyTypeLabel.setFont(keyTypeLabelFont);
        keyTypeLabel.setText("");
        basicPanel.add(keyTypeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyField = new JTextField();
        Font keyFieldFont = this.$$$getFont$$$(null, -1, -1, keyField.getFont());
        if (keyFieldFont != null) keyField.setFont(keyFieldFont);
        basicPanel.add(keyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        Font ttlFieldFont = this.$$$getFont$$$(null, -1, -1, ttlField.getFont());
        if (ttlFieldFont != null) ttlField.setFont(ttlFieldFont);
        basicPanel.add(ttlField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        basicPanel.add(toolBar1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        Font delBtnFont = this.$$$getFont$$$(null, -1, 12, delBtn.getFont());
        if (delBtnFont != null) delBtn.setFont(delBtnFont);
        delBtn.setMargin(new Insets(0, 0, 0, 10));
        this.$$$loadButtonText$$$(delBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.delBtn.title"));
        delBtn.setToolTipText(this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.delBtn.toolTip.Text"));
        toolBar1.add(delBtn);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        Font refBtnFont = this.$$$getFont$$$(null, -1, 12, refBtn.getFont());
        if (refBtnFont != null) refBtn.setFont(refBtnFont);
        refBtn.setMargin(new Insets(0, 0, 0, 10));
        this.$$$loadButtonText$$$(refBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.refBtn.title"));
        refBtn.setToolTipText(this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.refBtn.toolTip.Text"));
        toolBar1.add(refBtn);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator2);
        Font saveBtnFont = this.$$$getFont$$$(null, -1, 12, saveBtn.getFont());
        if (saveBtnFont != null) saveBtn.setFont(saveBtnFont);
        this.$$$loadButtonText$$$(saveBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.saveBtn.title"));
        saveBtn.setToolTipText(this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.saveBtn.toolTip.Text"));
        toolBar1.add(saveBtn);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 5, 0, 0), -1, -1));
        basicPanel.add(panel1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lengthLabel = new JLabel();
        lengthLabel.setText("");
        panel1.add(lengthLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keySizeLabel = new JLabel();
        keySizeLabel.setText("");
        panel1.add(keySizeLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        dataPanel = new JPanel();
        dataPanel.setLayout(new BorderLayout(0, 0));
        bodyPanel.add(dataPanel, BorderLayout.CENTER);
        dataSplitPanel = new JSplitPane();
        dataSplitPanel.setOrientation(0);
        dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        tableViewPanel.setLayout(new BorderLayout(0, 0));
        dataSplitPanel.setLeftComponent(tableViewPanel);
        tableViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        tableViewPanel.add(panel2, BorderLayout.CENTER);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.add(tableSearchField, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableDelBtn.setEnabled(true);
        this.$$$loadButtonText$$$(tableDelBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.tableDelBtn.title"));
        panel3.add(tableDelBtn, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        this.$$$loadButtonText$$$(tableRefreshBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.tableRefreshBtn.title"));
        panel3.add(tableRefreshBtn, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        this.$$$loadButtonText$$$(tableAddBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.tableAddBtn.title"));
        panel3.add(tableAddBtn, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        pagePanel = new JPanel();
        pagePanel.setLayout(new BorderLayout(0, 0));
        panel3.add(pagePanel, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pagePanel.add(panel4, BorderLayout.NORTH);
        panel4.add(currentCountField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, -1), null, 0, false));
        panel4.add(allCountField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        pagePanel.add(panel5, BorderLayout.SOUTH);
        this.$$$loadButtonText$$$(loadMoreBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "DataViewForm.loadMoreBtn.title"));
        panel5.add(loadMoreBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        panel2.add(tableScorePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        dataTable = new JTable();
        tableScorePanel.setViewportView(dataTable);
        valueViewPanel.setMinimumSize(new Dimension(-1, -1));
        dataSplitPanel.setRightComponent(valueViewPanel);
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
