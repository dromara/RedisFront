package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.quickswing.ui.swing.AnimateButton;
import org.dromara.redisfront.commons.codec.RedisValueCodec;
import org.dromara.redisfront.commons.codec.ValueViewType;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.table.HashTableModel;
import org.dromara.redisfront.model.table.SortedSetTableModel;
import org.dromara.redisfront.model.table.StreamTableModel;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.model.turbo.Turbo2;
import org.dromara.redisfront.model.value.RedisValueItem;
import org.dromara.redisfront.service.*;
import org.dromara.redisfront.ui.components.editor.TextEditor;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.components.scanner.core.*;
import org.dromara.redisfront.ui.components.scanner.model.ScanDataResult;
import org.dromara.redisfront.ui.dialog.AddOrUpdateValueDialog;
import org.dromara.redisfront.ui.dialog.ValueChangesDialog;
import org.dromara.redisfront.ui.event.KeyDeleteSuccessEvent;
import org.dromara.redisfront.ui.event.KeyRenameSuccessEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import raven.toast.Notifications;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.Arrays;
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
    private JComboBox<ValueViewType> valueViewComboBox;
    private JCheckBox lineWrapCheckBox;
    private AnimateButton copyBtn;
    private AnimateButton jsonFormatBtn;
    private JButton viewChangesBtn;

    private final RedisConnectContext redisConnectContext;
    private final TreeNodeInfo treeNodeInfo;
    private final RedisFrontWidget owner;

    private final StringRedisDataScanner stringDataFetcher;
    private final HashRedisDataScanner hashDataFetcher;
    private final SetRedisDataScanner setDataFetcher;
    private final ZSetRedisDataScanner zSetDataFetcher;
    private final ListRedisDataScanner listDataFetcher;
    private final StreamRedisDataScanner streamDataFetcher;
    private String lastKeyName;
    private Long lastKeyTTL;
    private KeyTypeEnum keyTypeEnum;
    private ValueViewType lastValueViewType = ValueViewType.AUTO;
    private byte[] currentRawValue;
    private ValueViewType editingValueType = ValueViewType.UTF8;
    private boolean tableListenersInstalled;


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
        configureEditorHeader();
        this.initialize();
    }

    private void configureEditorHeader() {
        // Keep the designer's fields, but let only the key input grow with the window.
        basicPanel.removeAll();
        basicPanel.setLayout(new BorderLayout(8, 0));
        basicPanel.setBorder(new EmptyBorder(0, 0, 6, 0));
        var keyIdentity = new JPanel(new BorderLayout(8, 0));
        keyIdentity.add(keyTypeLabel, BorderLayout.WEST);
        keyIdentity.add(keyField, BorderLayout.CENTER);
        basicPanel.add(keyIdentity, BorderLayout.CENTER);

        ttlField.setColumns(7);
        var keyActions = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 0));
        keyActions.add(ttlField);
        keyActions.add(delBtn);
        keyActions.add(refBtn);
        keyActions.add(saveBtn);
        basicPanel.add(keyActions, BorderLayout.EAST);

        var valueInfo = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 3));
        lengthLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground");
        keySizeLabel.putClientProperty(FlatClientProperties.STYLE, "foreground: $Label.disabledForeground");
        valueInfo.add(lengthLabel);
        valueInfo.add(keySizeLabel);
        valueViewPanel.add(valueInfo, BorderLayout.SOUTH);
    }

    private void refreshStringUI(Turbo2<Long, RedisValueItem> turbo) {
        tableViewPanel.setVisible(false);
        lengthLabel.setText("Length: " + turbo.getT1());
        keySizeLabel.setText("Size: " + DataSizeUtil.format(turbo.getT2().byteLength()));
        dataSplitPanel.setDividerSize(0);
        setCurrentValue(turbo.getT2());
    }

    private <T extends TableModel> void refreshTableUI(ScanDataResult<T> scanData) {
        keyLabel.setText(owner.$tr("DataViewForm.keyLabel.title"));
        keyLabel.setOpaque(true);
        keyLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        tableViewPanel.setVisible(true);
        setCurrentValue(null);
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

        if (tableListenersInstalled) return;
        tableListenersInstalled = true;
        dataTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) loadSelectedTableValue();
        });
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = dataTable.getSelectedRow();
                if (e.getButton() == MouseEvent.BUTTON1 && row >= 0) {
                    tableDelBtn.setEnabled(true);
                    loadSelectedTableValue();
                    if (e.getClickCount() == 2) updateValueActionPerformed();
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

    private void loadSelectedTableValue() {
        int row = dataTable.getSelectedRow();
        if (row < 0) {
            setCurrentValue(null);
            return;
        }
        switch (dataTable.getModel()) {
            case SortedSetTableModel ignored -> {
                keyLabel.setText(owner.$tr("DataViewForm.keyLabel.score.title"));
                fieldOrScoreField.setText(String.valueOf(dataTable.getValueAt(row, 1)));
                setCurrentValue((RedisValueItem) dataTable.getValueAt(row, 2));
            }
            case HashTableModel ignored -> {
                keyLabel.setText(owner.$tr("DataViewForm.keyLabel.title"));
                fieldOrScoreField.setText(String.valueOf(dataTable.getValueAt(row, 0)));
                setCurrentValue((RedisValueItem) dataTable.getValueAt(row, 1));
            }
            case StreamTableModel ignored -> {
                setCurrentValue(null);
                Object value = dataTable.getValueAt(row, 2);
                textEditor.setEditable(false);
                textEditor.setSyntaxHighlightingSupported(true);
                try {
                    textEditor.loadText(JSONUtil.toJsonPrettyStr(value));
                } catch (Exception e) {
                    textEditor.loadText(String.valueOf(value));
                }
            }
            default -> setCurrentValue((RedisValueItem) dataTable.getValueAt(row, 1));
        }
    }

    private void setCurrentValue(RedisValueItem valueItem) {
        if (valueItem == null) {
            currentRawValue = null;
            if (valueViewComboBox != null) {
                valueViewComboBox.setEnabled(false);
            }
            textEditor.loadText("");
            textEditor.setSyntaxHighlightingSupported(false);
            jsonFormatBtn.setEnabled(false);
            refreshSaveButtonByView();
            refreshValueChangesButton();
            return;
        }
        currentRawValue = valueItem.raw();
        if (valueViewComboBox != null) {
            valueViewComboBox.setEnabled(true);
        }
        refreshEditorByView();
    }

    private void refreshEditorByView() {
        if (currentRawValue == null) {
            textEditor.loadText("");
            textEditor.setSyntaxHighlightingSupported(false);
            jsonFormatBtn.setEnabled(false);
            refreshSaveButtonByView();
            return;
        }
        ValueViewType viewType = lastValueViewType == null ? ValueViewType.AUTO : lastValueViewType;
        var display = RedisValueCodec.forEditing(currentRawValue, viewType);
        editingValueType = display.effectiveType();
        boolean syntaxSupported = editingValueType == ValueViewType.UTF8
                || editingValueType == ValueViewType.GBK || editingValueType == ValueViewType.GB18030;
        boolean editable = viewType != ValueViewType.ESCAPED;
        textEditor.setEditable(editable);
        textEditor.setSyntaxHighlightingSupported(syntaxSupported);
        jsonFormatBtn.setEnabled(syntaxSupported);
        textEditor.loadText(display.text());
        valueViewComboBox.setToolTipText(viewType == ValueViewType.AUTO
                ? String.format(owner.$tr("DataEdit.autoView"), editingValueType.name()) : null);
        refreshSaveButtonByView();
        refreshValueChangesButton();
    }

    private void refreshValueChangesButton() {
        if (viewChangesBtn != null) viewChangesBtn.setVisible(hasValueChanges());
    }

    private boolean hasValueChanges() {
        if (currentRawValue == null || lastValueViewType == ValueViewType.ESCAPED) return false;
        try {
            if (!Arrays.equals(currentRawValue, RedisValueCodec.decode(textEditor.getText(), editingValueType)))
                return true;
            String original = originalFieldOrScore();
            if (original == null) return false;
            if (keyTypeEnum == KeyTypeEnum.ZSET) {
                return Double.compare(RedisEditService.parseScore(original), RedisEditService.parseScore(fieldOrScoreField.getText())) != 0;
            }
            return !original.equals(fieldOrScoreField.getText());
        } catch (IllegalArgumentException | RedisEditService.EditException e) {
            return true;
        }
    }

    private String originalFieldOrScore() {
        int row = dataTable.getSelectedRow();
        if (row < 0 || keyTypeEnum == null) return null;
        return switch (keyTypeEnum) {
            case HASH -> String.valueOf(dataTable.getValueAt(row, 0));
            case ZSET -> String.valueOf(dataTable.getValueAt(row, 1));
            default -> null;
        };
    }

    private RedisEditService.Target captureEditTarget() {
        if (!keyField.getText().equals(lastKeyName))
            throw new IllegalArgumentException(owner.$tr("DataEdit.keyPending"));
        if (currentRawValue == null) throw new RedisEditService.EditException(RedisEditService.Reason.MISSING);
        int row = dataTable.getSelectedRow();
        if (keyTypeEnum != KeyTypeEnum.STRING && row < 0)
            throw new RedisEditService.EditException(RedisEditService.Reason.MISSING);
        return new RedisEditService.Target(keyTypeEnum, lastKeyName, currentRawValue,
                keyTypeEnum == KeyTypeEnum.HASH ? originalFieldOrScore() : null,
                keyTypeEnum == KeyTypeEnum.ZSET ? ((Number) dataTable.getValueAt(row, 1)).doubleValue() : null,
                keyTypeEnum == KeyTypeEnum.LIST ? ((Number) dataTable.getValueAt(row, 0)).longValue() - 1 : null);
    }

    private void restoreSavedSelection(RedisEditService.Target target, byte[] value, String identity) {
        for (int row = 0; row < dataTable.getRowCount(); row++) {
            boolean match = switch (target.type()) {
                case HASH -> identity.equals(String.valueOf(dataTable.getValueAt(row, 0)));
                case LIST -> ((Number) dataTable.getValueAt(row, 0)).longValue() - 1 == target.index()
                        && Arrays.equals(((RedisValueItem) dataTable.getValueAt(row, 1)).raw(), value);
                case SET -> Arrays.equals(((RedisValueItem) dataTable.getValueAt(row, 1)).raw(), value);
                case ZSET -> Arrays.equals(((RedisValueItem) dataTable.getValueAt(row, 2)).raw(), value);
                default -> false;
            };
            if (match) {
                dataTable.setRowSelectionInterval(row, row);
                loadSelectedTableValue();
                return;
            }
        }
        setCurrentValue(null);
    }

    private void showValueChanges() {
        String before = RedisValueCodec.encode(currentRawValue, lastValueViewType);
        String after = textEditor.getText();
        String identity = originalFieldOrScore();
        if (identity != null && !identity.equals(fieldOrScoreField.getText())) {
            String label = owner.$tr(keyTypeEnum == KeyTypeEnum.HASH ? "DataEdit.field" : "DataEdit.score");
            before = label + ": " + identity + "\n\n" + before;
            after = label + ": " + fieldOrScoreField.getText() + "\n\n" + after;
        }
        ValueChangesDialog.showDialog(owner, keyField.getText(), before, after, owner::$tr);
    }

    private void acceptMetadataUpdate(String oldKey, String newKey, Long requestedTtl) {
        lastKeyName = newKey;
        keyField.setText(newKey);
        if (requestedTtl != null) {
            lastKeyTTL = requestedTtl;
            ttlField.setText(requestedTtl.toString());
        }
        hashDataFetcher.renameContext(oldKey, newKey);
        setDataFetcher.renameContext(oldKey, newKey);
        zSetDataFetcher.renameContext(oldKey, newKey);
        listDataFetcher.renameContext(oldKey, newKey);
        streamDataFetcher.renameContext(oldKey, newKey);
        hashDataFetcher.setKey(newKey);
        setDataFetcher.setKey(newKey);
        zSetDataFetcher.setKey(newKey);
        listDataFetcher.setKey(newKey);
        streamDataFetcher.setKey(newKey);
        // Saving key metadata must preserve the pending value and its undo history.
        refreshValueChangesButton();
    }

    private void copyValue(String value) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), null);
        Notifications.getInstance().show(Notifications.Type.INFO, owner.$tr("DataViewForm.copyBtn.success.message"));
    }

    private void refreshSaveButtonByView() {
        if (valueUpdateSaveBtn == null) {
            return;
        }
        boolean enabled = currentRawValue != null && lastValueViewType != ValueViewType.ESCAPED;
        valueUpdateSaveBtn.setEnabled(enabled);
        if (copyBtn != null) {
            // 复制与编码视图无关，只要当前有值就可用
            copyBtn.setEnabled(currentRawValue != null);
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
        reloadTableDataActionPerformed(reset, null);
    }

    private void reloadTableDataActionPerformed(Boolean reset, Runnable afterReload) {
        String key = lastKeyName;
        String searchText = tableSearchField.getText();
        KeyTypeEnum requestedType = keyTypeEnum;
        SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
            this.lastKeyTTL = RedisBasicService.service.ttl(redisConnectContext, key);
            switch (requestedType) {
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
        }, (ignore, e) -> {
            if (e != null) {
                Notifications.getInstance().show(Notifications.Type.ERROR, e.getMessage());
                return;
            }
            refreshUI();
            if (afterReload != null) afterReload.run();
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
        if (keyTypeEnum == KeyTypeEnum.STREAM) return;
        try {
            var target = captureEditTarget();
            AddOrUpdateValueDialog.showDialog(owner, owner.$tr("DataViewForm.showAddOrUpdateItemDialog.title"),
                    target.key(), originalFieldOrScore(), target, redisConnectContext, target.type(), () -> {
                        setCurrentValue(null);
                        reloadAllActionPerformed();
                    });
        } catch (Exception e) {
            Notifications.getInstance().show(Notifications.Type.ERROR, ExceptionMessageUtils.bestMessage(e));
        }
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
        jToolBar.setFloatable(false);
        jToolBar.setBorder(BorderFactory.createEmptyBorder());
        textEditor = TextEditor.newInstance();
        textEditor.installSyntaxHighlightingMenu(owner::$tr);
        textEditor.installEditingTools(owner::$tr);
        textEditor.addTextChangeListener(this::refreshValueChangesButton);

        fieldOrScoreField = new JTextField();
        fieldOrScoreField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshValueChangesButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshValueChangesButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        keyLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.keyLabel.title"));
            }
        };
        keyLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
        fieldOrScoreField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, keyLabel);

        valueViewComboBox = new JComboBox<>() {
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        valueViewComboBox.addItem(ValueViewType.AUTO);
        valueViewComboBox.addItem(ValueViewType.UTF8);
        valueViewComboBox.addItem(ValueViewType.GBK);
        valueViewComboBox.addItem(ValueViewType.GB18030);
        valueViewComboBox.addItem(ValueViewType.LATIN1);
        valueViewComboBox.addItem(ValueViewType.ESCAPED);
        valueViewComboBox.addItem(ValueViewType.BASE64);
        valueViewComboBox.addItem(ValueViewType.HEX);
        valueViewComboBox.setSelectedItem(ValueViewType.AUTO);
        valueViewComboBox.addActionListener(ignore -> {
            Object item = valueViewComboBox.getSelectedItem();
            if (item instanceof ValueViewType valueViewType) {
                lastValueViewType = valueViewType;
                refreshEditorByView();
            }
        });
        var encodingLabel = new JLabel(owner.$tr("DataViewForm.valueView.label"));
        encodingLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
        jToolBar.add(encodingLabel);
        jToolBar.add(valueViewComboBox);
        jToolBar.addSeparator();

        lineWrapCheckBox = new JCheckBox() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.wrapToggle.title"));
                setToolTipText(owner.$tr("DataViewForm.wrapToggle.tooltip.text"));
            }
        };
        lineWrapCheckBox.setOpaque(false);
        lineWrapCheckBox.setFocusable(false);
        lineWrapCheckBox.addActionListener(ignore -> textEditor.setLineWrap(lineWrapCheckBox.isSelected()));
        jToolBar.add(lineWrapCheckBox);
        jToolBar.addSeparator();

        var findButton = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.search.title"));
                setToolTipText(owner.$tr("DataViewForm.search.tooltip"));
            }
        };
        findButton.setIcon(new FlatSearchIcon());
        findButton.setFocusable(false);
        findButton.addActionListener(e -> textEditor.showSearch(false));
        jToolBar.add(findButton);
        var undoButton = new JButton(textEditor.getUndoAction());
        undoButton.setHideActionText(true);
        undoButton.setIcon(Icons.UNDO_ICON);
        undoButton.setFocusable(false);
        jToolBar.add(undoButton);
        var redoButton = new JButton(textEditor.getRedoAction());
        redoButton.setHideActionText(true);
        redoButton.setIcon(Icons.REDO_ICON);
        redoButton.setFocusable(false);
        jToolBar.add(redoButton);
        jToolBar.addSeparator();

        copyBtn = new AnimateButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.copyBtn.title") + " ▾");
                setToolTipText(owner.$tr("DataViewForm.copyBtn.tooltip.text"));
                setBackground(UIManager.getColor("RedisFront.animateButton.background"));
            }
        };
        copyBtn.setText(owner.$tr("DataViewForm.copyBtn.title") + " ▾");
        copyBtn.setIcon(Icons.COPY_A_ICON);
        copyBtn.setEffectColor(Color.BLUE);
        copyBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        copyBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        copyBtn.setArcHeight(10);
        copyBtn.setArcWidth(10);
        copyBtn.addActionListener(ignore -> {
            if (currentRawValue == null) {
                return;
            }
            var menu = new JPopupMenu();
            var current = new JMenuItem(owner.$tr("DataViewForm.copyBtn.current"));
            current.addActionListener(e -> copyValue(textEditor.getText()));
            menu.add(current);
            var original = new JMenuItem(owner.$tr("DataViewForm.copyBtn.original"));
            original.addActionListener(e -> copyValue(RedisValueCodec.encode(currentRawValue, ValueViewType.AUTO)));
            menu.add(original);
            menu.show(copyBtn, 0, copyBtn.getHeight());
        });
        jToolBar.add(copyBtn);

        jsonFormatBtn = new AnimateButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.jsonFormatBtn.title"));
                setToolTipText(owner.$tr("DataViewForm.jsonFormatBtn.tooltip.text"));
                setBackground(UIManager.getColor("RedisFront.animateButton.background"));
            }
        };
        jsonFormatBtn.setText(owner.$tr("DataViewForm.jsonFormatBtn.title"));
        jsonFormatBtn.setIcon(Icons.JSON_FORMAT_ICON);
        jsonFormatBtn.setEffectColor(Color.ORANGE);
        jsonFormatBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        jsonFormatBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        jsonFormatBtn.setArcHeight(10);
        jsonFormatBtn.setArcWidth(10);
        jsonFormatBtn.addActionListener(ignore -> {
            var text = textEditor.getText();
            if (RedisFrontUtils.isEmpty(text)) {
                return;
            }
            var trimmed = text.strip();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                Notifications.getInstance().show(Notifications.Type.ERROR, owner.$tr("DataViewForm.jsonFormatBtn.invalid.message"));
                return;
            }
            try {
                // 根据当前排版切换：单行 JSON 格式化，多行 JSON 压缩。
                boolean pretty = !text.contains("\n");
                String formatted;
                if (pretty) {
                    formatted = JSONUtil.toJsonPrettyStr(text);
                } else if (trimmed.startsWith("{")) {
                    formatted = JSONUtil.parseObj(text).toString();
                } else {
                    formatted = JSONUtil.parseArray(text).toString();
                }
                textEditor.setText(formatted);
            } catch (JSONException e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, owner.$tr("DataViewForm.jsonFormatBtn.invalid.message"));
            }
        });
        jToolBar.add(jsonFormatBtn);

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

        InputMap inputMap = jToolBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(keyStroke, "triggerSave");

        ActionMap actionMap = jToolBar.getActionMap();
        actionMap.put("triggerSave", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focus == keyField || focus == ttlField) saveBtn.doClick();
                else valueUpdateSaveBtn.doClick();
            }
        });
        valueUpdateSaveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        valueUpdateSaveBtn.setEffectColor(Color.decode("#389FD6"));
        valueUpdateSaveBtn.setBorder(new EmptyBorder(5, 5, 5, 5));
        valueUpdateSaveBtn.setBackground(UIManager.getColor("RedisFront.animateButton.background"));
        valueUpdateSaveBtn.setEnabled(false);
        valueUpdateSaveBtn.setIcon(Icons.SAVE_ICON);
        valueUpdateSaveBtn.addActionListener(ignore -> {
            if (lastValueViewType == ValueViewType.ESCAPED) return;
            final RedisEditService.Target target;
            try {
                target = captureEditTarget();
            } catch (Exception e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, ExceptionMessageUtils.bestMessage(e));
                return;
            }
            String newText = textEditor.getText();
            String identity = fieldOrScoreField.getText();
            ValueViewType decoder = editingValueType;
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                byte[] value = RedisValueCodec.decode(newText, decoder);
                RedisEditService.service.save(redisConnectContext, target, value, identity);
                return value;
            }, (savedBytes, e) -> {
                if (e != null) {
                    Notifications.getInstance().show(Notifications.Type.ERROR, ExceptionMessageUtils.bestMessage(e));
                    return;
                }
                currentRawValue = savedBytes;
                refreshEditorByView();
                Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("DataViewForm.showInformationDialog.updateSuccess.message"));
                if (target.type() == KeyTypeEnum.STRING) {
                    reloadAllActionPerformed();
                } else {
                    setCurrentValue(null);
                    reloadTableDataActionPerformed(true, () -> restoreSavedSelection(target, savedBytes, identity));
                }
            });
        });
        jToolBar.add(Box.createHorizontalGlue());
        viewChangesBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(owner.$tr("DataViewForm.changes.button"));
            }
        };
        viewChangesBtn.setVisible(false);
        viewChangesBtn.addActionListener(e -> showValueChanges());
        jToolBar.add(viewChangesBtn);
        jToolBar.add(valueUpdateSaveBtn);

        var editorHeader = new JPanel(new BorderLayout(0, 6));
        editorHeader.setBorder(new EmptyBorder(2, 0, 6, 0));
        editorHeader.add(fieldOrScoreField, BorderLayout.NORTH);
        editorHeader.add(jToolBar, BorderLayout.CENTER);
        valueViewPanel.add(editorHeader, BorderLayout.NORTH);

        valueViewPanel.add(new JPanel() {
            {
                setLayout(new BorderLayout());
                add(textEditor, BorderLayout.CENTER);
            }
        }, BorderLayout.CENTER);

        basicPanel = new JPanel();

        loadMoreBtn = new JButton();
        loadMoreBtn.setIcon(Icons.LOAD_MORE_ICON);
        loadMoreBtn.addActionListener((ignore) -> {
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
        delBtn.addActionListener(ignore -> {
            var key = keyField.getText();
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() ->
                            RedisBasicService.service.del(redisConnectContext, key),
                    (ignore1, e) -> {
                        if (e == null) {
                            owner.getContext().getEventBus().publish(new KeyDeleteSuccessEvent(key, redisConnectContext.getId()));
                            return;
                        }
                        Notifications.getInstance().show(Notifications.Type.ERROR, owner.$tr("DataViewForm.keyDeleteFail.message"));
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
        refBtn.addActionListener(ignore -> reloadAllActionPerformed());

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

        ttlField.setToolTipText(owner.$tr("DataEdit.ttlHelp"));
        saveBtn.addActionListener(ignore -> {
            String oldKey = lastKeyName;
            String newKey = keyField.getText();
            final Long requestedTtl;
            try {
                requestedTtl = ttlField.getText().equals(String.valueOf(lastKeyTTL)) ? null
                        : RedisEditService.parseTtl(ttlField.getText());
            } catch (Exception e) {
                Notifications.getInstance().show(Notifications.Type.ERROR, ExceptionMessageUtils.bestMessage(e));
                return;
            }
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                RedisEditService.service.updateMetadata(redisConnectContext, oldKey, newKey, requestedTtl);
                return newKey;
            }, (savedKey, e) -> {
                if (e == null) {
                    acceptMetadataUpdate(oldKey, savedKey, requestedTtl);
                    Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("DataViewForm.showInformationDialog.updateSuccess.message"));
                    if (!oldKey.equals(savedKey)) {
                        owner.getContext().getEventBus().publish(new KeyRenameSuccessEvent(savedKey, redisConnectContext.getId()));
                    }
                } else {
                    Notifications.getInstance().show(Notifications.Type.ERROR, ExceptionMessageUtils.bestMessage(e));
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
        searchBtn.addActionListener(ignore -> reloadTableDataActionPerformed(true));
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) ignore -> {
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

        tableAddBtn.addActionListener((ignore) -> {
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
        tableDelBtn.addActionListener(ignore -> {
            SyncLoadingDialog.builder(owner).showSyncLoadingDialog(() -> {
                var row = dataTable.getSelectedRow();
                if (row != -1) {
                    tableDelBtn.setEnabled(false);
                    var keyType = keyTypeLabel.getText();
                    KeyTypeEnum keyTypeEnum = KeyTypeEnum.valueOf(keyType.toUpperCase());
                    String key = keyField.getText();

                    switch (keyTypeEnum) {
                        case ZSET -> {
                            var value = (RedisValueItem) dataTable.getValueAt(row, 2);
                            RedisZSetService.service.zrem(redisConnectContext, key, value.raw());
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
                            var value = (RedisValueItem) dataTable.getValueAt(row, 1);
                            RedisListService.service.lrem(redisConnectContext, key, 1, value.raw());
                            return (Runnable) () -> {
                                textEditor.clear();
                                valueUpdateSaveBtn.setEnabled(false);
                            };
                        }
                        case SET -> {
                            var value = (RedisValueItem) dataTable.getValueAt(row, 1);
                            RedisSetService.service.srem(redisConnectContext, key, value.raw());
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

        tableRefreshBtn.addActionListener(ignore -> {
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

}
