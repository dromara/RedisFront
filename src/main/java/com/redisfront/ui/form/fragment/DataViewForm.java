package com.redisfront.ui.form.fragment;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ActionHandler;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LocaleUtils;
import com.redisfront.model.*;
import com.redisfront.service.*;
import com.redisfront.ui.component.LoadingPanel;
import com.redisfront.ui.component.TextEditor;
import com.redisfront.ui.dialog.AddOrUpdateItemDialog;
import io.lettuce.core.*;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * DataViewForm
 *
 * @author Jin
 */
public class DataViewForm {

    private JPanel contentPanel;
    private JTextField keyField;
    private JPanel bodyPanel;
    private JPanel valueViewPanel;
    private JPanel basicPanel;
    private JLabel keyTypeLabel;
    private JButton delBtn;
    private JButton refBtn;
    private JLabel keyLabel;
    private JLabel lengthLabel;
    private JLabel keySizeLabel;
    private JButton valueUpdateSaveBtn;
    private JButton saveBtn;
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
    private JButton closeBtn;
    private TextEditor textEditor;
    private JTextField fieldOrScoreField;
    private JComboBox<String> jComboBox;
    private final ConnectInfo connectInfo;

    private final Map<String, ScanContext<String>> scanSetContextMap;
    private final Map<String, ScanContext<String>> scanListContextMap;

    private final Map<String, ScanContext<StreamMessage<String, String>>> xRangeContextMap;
    private final Map<String, ScanContext<ScoredValue<String>>> scanZSetContextMap;
    private final Map<String, ScanContext<Map.Entry<String, String>>> scanHashContextMap;

    private ActionHandler deleteActionHandler;
    private ActionHandler closeActionHandler;
    private ActionHandler refreshBeforeHandler;
    private ActionHandler refreshAfterHandler;

    private String lastKeyName;
    private Long lastKeyTTL;

    public void setRefreshBeforeHandler(ActionHandler refreshBeforeHandler) {
        this.refreshBeforeHandler = refreshBeforeHandler;
    }

    public void setRefreshAfterHandler(ActionHandler refreshAfterHandler) {
        this.refreshAfterHandler = refreshAfterHandler;
    }

    public static DataViewForm newInstance(ConnectInfo connectInfo) {
        return new DataViewForm(connectInfo);
    }

    public void setDeleteActionHandler(ActionHandler handler) {
        this.deleteActionHandler = handler;
    }

    public void setCloseActionHandler(ActionHandler closeActionHandler) {
        this.closeActionHandler = closeActionHandler;
    }

    private void refreshDisableBtn() {
        refBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        delBtn.setEnabled(false);
    }

    private void refreshEnableBtn() {
        saveBtn.setEnabled(true);
        delBtn.setEnabled(true);
        refBtn.setEnabled(true);
    }

    public DataViewForm(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        scanZSetContextMap = new LinkedHashMap<>();
        scanSetContextMap = new LinkedHashMap<>();
        scanListContextMap = new LinkedHashMap<>();
        xRangeContextMap = new LinkedHashMap<>();
        scanHashContextMap = new LinkedHashMap<>();
        $$$setupUI$$$();
        dataTableInit();
    }

    private void dataTableInit() {

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
                        SwingUtilities.invokeLater(() -> {
                            fieldOrScoreField.setText(score.toString());
                            valueUpdateSaveBtn.setEnabled(true);
                            jsonValueFormat((String) value);
                        });
                    } else if (dataTable.getModel() instanceof HashTableModel) {
                        var value = dataTable.getValueAt(row, 1);
                        var key = dataTable.getValueAt(row, 0);
                        SwingUtilities.invokeLater(() -> {
                            fieldOrScoreField.setText(key.toString());
                            valueUpdateSaveBtn.setEnabled(true);
                            jsonValueFormat((String) value);
                        });
                    } else if (dataTable.getModel() instanceof StreamTableModel) {
                        var value = dataTable.getValueAt(row, 2);
                        SwingUtilities.invokeLater(() -> {
                            valueUpdateSaveBtn.setEnabled(true);
                            textEditor.textArea().setText(JSONUtil.toJsonPrettyStr(value));
                        });
                    } else {
                        var value = dataTable.getValueAt(row, 1);
                        SwingUtilities.invokeLater(() -> {
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
            textEditor.textArea().setText(JSONUtil.toJsonPrettyStr(value));
            jComboBox.setSelectedIndex(1);
        } else {
            textEditor.textArea().setText(value);
        }
    }

    public JPanel contentPanel() {
        return contentPanel;
    }

    private void reloadAllActionPerformed() {
        if (refBtn.isEnabled()) {
            var key = keyField.getText();
            this.lastKeyName = key;
            FutureUtils.supplyAsync(() -> keyTypeLabel.getText(), keyType -> {
                if (Fn.notEqual(keyType, "none")) {
                    Enum.KeyTypeEnum keyTypeEnum = Enum.KeyTypeEnum.valueOf(keyType.toUpperCase());
                    FutureUtils.runAsync(() ->
                            dataChangeActionPerformed(key, () -> {
                                SwingUtilities.invokeLater(() -> {
                                    refreshDisableBtn();
                                    refreshBeforeHandler.handle();
                                    Fn.removeAllComponent(dataPanel);
                                    dataPanel.add(LoadingPanel.newInstance(), BorderLayout.CENTER);
                                    dataPanel.updateUI();
                                });
                                //加载数据
                                {
                                    if (keyTypeEnum == Enum.KeyTypeEnum.STRING || keyTypeEnum == Enum.KeyTypeEnum.JSON) {
                                        loadStringActionPerformed(key);
                                    }
                                    if (keyTypeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
                                        this.scanZSetContextMap.put(key, new ScanContext<>());
                                    }
                                    if (keyTypeEnum.equals(Enum.KeyTypeEnum.HASH)) {
                                        this.scanHashContextMap.put(key, new ScanContext<>());
                                    }
                                    if (keyTypeEnum.equals(Enum.KeyTypeEnum.LIST)) {
                                        this.scanListContextMap.put(key, new ScanContext<>());
                                    }
                                    if (keyTypeEnum.equals(Enum.KeyTypeEnum.SET)) {
                                        this.scanSetContextMap.put(key, new ScanContext<>());
                                    }
                                    if (keyTypeEnum.equals(Enum.KeyTypeEnum.STREAM)) {
                                        this.xRangeContextMap.put(key, new ScanContext<>());
                                    }
                                }
                            }, () -> SwingUtilities.invokeLater(() -> {
                                refreshEnableBtn();
                                refreshAfterHandler.handle();
                                Fn.removeAllComponent(dataPanel);
                                dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
                                dataPanel.updateUI();
                            })), throwable -> refreshAfterHandler.handle());
                } else {
                    ttlField.setText("-2");
                    AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showInformationDialog.message"));
                }

            });

        }
    }

    private void reloadTableDataActionPerformed(Boolean init) {
        FutureUtils.runAsync(() -> {
            var key = keyField.getText();
            this.lastKeyName = key;
            var keyType = keyTypeLabel.getText();
            var keyTypeEnum = Enum.KeyTypeEnum.valueOf(keyType.toUpperCase());

            SwingUtilities.invokeLater(() -> {
                refreshBeforeHandler.handle();
                refreshDisableBtn();
                tableAddBtn.setEnabled(false);
                tableDelBtn.setEnabled(false);
                tableRefreshBtn.setEnabled(false);
            });

            if (keyTypeEnum == Enum.KeyTypeEnum.STRING || keyTypeEnum == Enum.KeyTypeEnum.JSON) {
                loadStringActionPerformed(key);
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
                if (init)
                    scanZSetContextMap.put(key, new ScanContext<>());
                loadZSetDataActionPerformed(key);
            }
            if (keyTypeEnum.equals(Enum.KeyTypeEnum.HASH)) {
                if (init)
                    scanHashContextMap.put(key, new ScanContext<>());
                loadHashDataActionPerformed(key);
            }
            if (keyTypeEnum.equals(Enum.KeyTypeEnum.LIST)) {
                if (init)
                    scanListContextMap.put(key, new ScanContext<>());
                loadListDataActionPerformed(key);
            }
            if (keyTypeEnum.equals(Enum.KeyTypeEnum.SET)) {
                if (init)
                    scanSetContextMap.put(key, new ScanContext<>());
                loadSetDataActionPerformed(key);
            }
            if (keyTypeEnum.equals(Enum.KeyTypeEnum.STREAM)) {
                if (init)
                    xRangeContextMap.put(key, new ScanContext<>());
                loadStreamDataActionPerformed(key);
            }
            SwingUtilities.invokeLater(() -> {
                refreshBeforeHandler.handle();
                refreshEnableBtn();
                loadMoreBtn.requestFocus();
                tableAddBtn.setEnabled(true);
                tableRefreshBtn.setEnabled(true);
                refreshAfterHandler.handle();
            });
        });
    }

    public synchronized void dataChangeActionPerformed(String key, ActionHandler beforeActionHandler, ActionHandler afterActionHandler) {
        refreshBeforeHandler.handle();
        beforeActionHandler.handle();

        var type = RedisBasicService.service.type(connectInfo, key);
        if (Fn.notEqual(type, "none")) {
            var keyTypeEnum = Enum.KeyTypeEnum.valueOf(type.toUpperCase());
            var ttl = RedisBasicService.service.ttl(connectInfo, key);
            SwingUtilities.invokeLater(() -> {
                fieldOrScoreField.setVisible(keyTypeEnum == Enum.KeyTypeEnum.ZSET || keyTypeEnum == Enum.KeyTypeEnum.HASH);
                keyTypeLabel.setText(keyTypeEnum.typeName());
                keyTypeLabel.setBackground(keyTypeEnum.color());
                ttlField.setText(ttl.toString());
                keyField.setText(key);
                this.lastKeyName = key;
                this.lastKeyTTL = ttl;
            });
            if (keyTypeEnum == Enum.KeyTypeEnum.STRING || keyTypeEnum == Enum.KeyTypeEnum.JSON) {
                loadStringActionPerformed(key);
            } else if (keyTypeEnum == Enum.KeyTypeEnum.HASH) {
                loadHashDataActionPerformed(key);
            } else if (keyTypeEnum == Enum.KeyTypeEnum.SET) {
                loadSetDataActionPerformed(key);
            } else if (keyTypeEnum == Enum.KeyTypeEnum.ZSET) {
                loadZSetDataActionPerformed(key);
            } else if (keyTypeEnum == Enum.KeyTypeEnum.LIST) {
                loadListDataActionPerformed(key);
            } else if (keyTypeEnum == Enum.KeyTypeEnum.STREAM) {
                loadStreamDataActionPerformed(key);
            }
        } else {
            AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showInformationDialog.message"));
            refreshDisableBtn();
        }

        refreshAfterHandler.handle();
        afterActionHandler.handle();
    }

    private void loadStringActionPerformed(String key) {
        var strLen = RedisStringService.service.strlen(connectInfo, key);
        var value = RedisStringService.service.get(connectInfo, key);
        SwingUtilities.invokeLater(() -> {
            tableViewPanel.setVisible(false);
            valueUpdateSaveBtn.setEnabled(true);
            lengthLabel.setText("Length: " + strLen);
            keySizeLabel.setText("Size: " + Fn.getDataSize(value));
            jsonValueFormat(value);
        });
    }

    private void loadHashDataActionPerformed(String key) {
        var len = RedisHashService.service.hlen(connectInfo, key);
        var scanContext = scanHashContextMap.getOrDefault(key, new ScanContext<>());
        var lastSearchKey = scanContext.getSearchKey();

        scanContext.setSearchKey(tableSearchField.getText());
        scanContext.setLimit(500L);

        var mapScanCursor = RedisHashService.service.hscan(connectInfo, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(mapScanCursor);

        if (Fn.equal(scanContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 1000) {
                System.gc();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(new ArrayList<>(mapScanCursor.getMap().entrySet()));
        } else {
            scanContext.setKeyList(new ArrayList<>(mapScanCursor.getMap().entrySet()));
        }

        scanHashContextMap.put(key, scanContext);

        var hashTableModel = new HashTableModel(scanContext.getKeyList());

        SwingUtilities.invokeLater(() -> {

            LoadAfterUpdate(len, DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0)), String.valueOf(scanContext.getKeyList().size()), mapScanCursor.isFinished());

            keyLabel.setText(LocaleUtils.getMessageFromBundle("DataViewForm.keyLabel.title"));
            keyLabel.setOpaque(true);
            keyLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            tableViewPanel.setVisible(true);
            dataTable.setModel(hashTableModel);
            Fn.removeAllComponent(dataPanel);
            dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        });
    }

    private void loadSetDataActionPerformed(String key) {
        var len = RedisSetService.service.scard(connectInfo, key);
        var scanContext = scanSetContextMap.getOrDefault(key, new ScanContext<>());

        var lastSearchKey = scanContext.getSearchKey();
        scanContext.setSearchKey(tableSearchField.getText());
        scanContext.setLimit(500L);

        var valueScanCursor = RedisSetService.service.sscan(connectInfo, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(valueScanCursor);

        if (Fn.equal(scanContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 2000) {
                System.gc();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(valueScanCursor.getValues());
        } else {
            scanContext.setKeyList(valueScanCursor.getValues());
        }


        scanSetContextMap.put(key, scanContext);

        var setTableModel = new SetTableModel(scanContext.getKeyList());

        SwingUtilities.invokeLater(() -> {
            LoadAfterUpdate(len, DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0)), String.valueOf(scanContext.getKeyList().size()), valueScanCursor.isFinished());
            tableViewPanel.setVisible(true);
            dataTable.setModel(setTableModel);
            Fn.removeAllComponent(dataPanel);
            dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        });
    }

    private void loadListDataActionPerformed(String key) {
        var len = RedisListService.service.llen(connectInfo, key);

        var scanContext = scanListContextMap.getOrDefault(key, new ScanContext<>());

        var lastSearchKey = scanContext.getSearchKey();
        scanContext.setSearchKey(tableSearchField.getText());
        scanContext.setLimit(500L);
        var start = Long.parseLong(scanContext.getScanCursor().getCursor());
        var stop = start + (scanContext.getLimit() - 1);
        var value = RedisListService.service.lrange(connectInfo, key, start, stop);

        var nextCursor = start + scanContext.getLimit();
        if (nextCursor >= len) {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(len), true));
        } else {
            scanContext.setScanCursor(new ScanCursor(String.valueOf(nextCursor), false));
        }

        if (Fn.equal(scanContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 2000) {
                System.gc();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(value);
        } else {
            scanContext.setKeyList(value);
        }

        scanListContextMap.put(key, scanContext);
        ListTableModel listTableModel;
        if (Fn.isNotEmpty(tableSearchField.getText())) {
            var findList = scanContext.getKeyList().stream().filter(s -> s.contains(tableSearchField.getText())).collect(Collectors.toList());
            listTableModel = new ListTableModel(findList);
        } else {
            listTableModel = new ListTableModel(scanContext.getKeyList());
        }

        final var finalListTableModel = listTableModel;
        SwingUtilities.invokeLater(() -> {
            LoadAfterUpdate(len, DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0)), String.valueOf(scanContext.getKeyList().size()), scanContext.getScanCursor().isFinished());
            tableViewPanel.setVisible(true);
            dataTable.setModel(finalListTableModel);
            Fn.removeAllComponent(dataPanel);
            dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        });
    }

    private void loadStreamDataActionPerformed(String key) {
        var len = RedisStreamService.service.xlen(connectInfo, key);

        var xRangeContext = xRangeContextMap.getOrDefault(key, new ScanContext<>());

        var lastSearchKey = xRangeContext.getSearchKey();
        xRangeContext.setSearchKey(tableSearchField.getText());
        xRangeContext.setLimit(500L);
        var start = Long.parseLong(xRangeContext.getScanCursor().getCursor());
        var stop = start + (xRangeContext.getLimit() - 1);
        var value = RedisStreamService.service.xrange(connectInfo, key, Range.unbounded(), Limit.create(start, stop));

        var nextCursor = start + xRangeContext.getLimit();
        if (nextCursor >= len) {
            xRangeContext.setScanCursor(new ScanCursor(String.valueOf(len), true));
        } else {
            xRangeContext.setScanCursor(new ScanCursor(String.valueOf(nextCursor), false));
        }

        if (Fn.equal(xRangeContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(xRangeContext.getKeyList())) {
            if (xRangeContext.getKeyList().size() >= 2000) {
                System.gc();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataViewForm.redisFrontException.message"));
            }
            xRangeContext.getKeyList().addAll(value);
        } else {
            xRangeContext.setKeyList(value);
        }

        xRangeContextMap.put(key, xRangeContext);

        final var finalListTableModel = new StreamTableModel(xRangeContext.getKeyList());
        SwingUtilities.invokeLater(() -> {
            LoadAfterUpdate(len, DataSizeUtil.format(xRangeContext.getKeyList().stream().map(e -> Fn.getByteSize(e.getBody())).reduce(Integer::sum).orElse(0)), String.valueOf(xRangeContext.getKeyList().size()), xRangeContext.getScanCursor().isFinished());
            tableViewPanel.setVisible(true);
            dataTable.setModel(finalListTableModel);
            Fn.removeAllComponent(dataPanel);
            dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        });
    }

    private void loadZSetDataActionPerformed(String key) {
        var len = RedisZSetService.service.zcard(connectInfo, key);

        var scanContext = scanZSetContextMap.getOrDefault(key, new ScanContext<>());

        var lastSearchKey = scanContext.getSearchKey();
        scanContext.setSearchKey(tableSearchField.getText());
        scanContext.setLimit(500L);

        var valueScanCursor = RedisZSetService.service.zscan(connectInfo, key, scanContext.getScanCursor(), scanContext.getScanArgs());
        scanContext.setScanCursor(valueScanCursor);

        if (Fn.equal(scanContext.getSearchKey(), lastSearchKey) && Fn.isNotEmpty(scanContext.getKeyList())) {
            if (scanContext.getKeyList().size() >= 2000) {
                scanContext.getKeyList().clear();
                System.gc();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("DataViewForm.redisFrontException.message"));
            }
            scanContext.getKeyList().addAll(valueScanCursor.getValues());
        } else {
            scanContext.setKeyList(valueScanCursor.getValues());
        }

        scanZSetContextMap.put(key, scanContext);

        var sortedSetTableModel = new SortedSetTableModel(scanContext.getKeyList());

        SwingUtilities.invokeLater(() -> {
            keyLabel.setText(LocaleUtils.getMessageFromBundle("DataViewForm.keyLabel.score.title"));
            LoadAfterUpdate(len, DataSizeUtil.format(scanContext.getKeyList().stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0)), String.valueOf(scanContext.getKeyList().size()), valueScanCursor.isFinished());
            tableViewPanel.setVisible(true);
            dataTable.setModel(sortedSetTableModel);
            Fn.removeAllComponent(dataPanel);
            dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        });
    }

    private void LoadAfterUpdate(Long len, String dataSize, String loadSize, Boolean isFinished) {
        lengthLabel.setText("Length: " + len);
        keySizeLabel.setText("Size: " + dataSize);
        currentCountField.setText(loadSize);
        allCountField.setText(String.valueOf(len));
        if (isFinished) {
            loadMoreBtn.setText(LocaleUtils.getMessageFromBundle("DataViewForm.loadMoreBtn.complete.title"));
            loadMoreBtn.setEnabled(false);
        } else {
            loadMoreBtn.setText(LocaleUtils.getMessageFromBundle("DataViewForm.loadMoreBtn.title"));
            loadMoreBtn.setEnabled(true);
        }

    }


    private void updateValueActionPerformed() {
        var row = dataTable.getSelectedRow();

        if (row == -1) {
            return;
        }
        var keyType = keyTypeLabel.getText();
        Enum.KeyTypeEnum keyTypeEnum = Enum.KeyTypeEnum.valueOf(keyType.toUpperCase());

        if (keyTypeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
            var score = (Double) dataTable.getValueAt(row, 1);
            var value = (String) dataTable.getValueAt(row, 2);
            AddOrUpdateItemDialog.showAddOrUpdateItemDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showAddOrUpdateItemDialog.title"), keyField.getText(), score.toString(), value, connectInfo, keyTypeEnum, () -> System.out.println("OK"));
        }

        if (keyTypeEnum.equals(Enum.KeyTypeEnum.HASH)) {
            var key = (String) dataTable.getValueAt(row, 0);
            var value = (String) dataTable.getValueAt(row, 1);
            AddOrUpdateItemDialog.showAddOrUpdateItemDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showAddOrUpdateItemDialog.title"), keyField.getText(), key, value, connectInfo, keyTypeEnum, () -> System.out.println("OK"));
        }

        if (keyTypeEnum.equals(Enum.KeyTypeEnum.LIST)) {
            var value = (String) dataTable.getValueAt(row, 1);
            AddOrUpdateItemDialog.showAddOrUpdateItemDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showAddOrUpdateItemDialog.title"), keyField.getText(), null, value, connectInfo, keyTypeEnum, () -> System.out.println("OK"));
        }

        if (keyTypeEnum.equals(Enum.KeyTypeEnum.SET)) {
            var value = (String) dataTable.getValueAt(row, 1);
            AddOrUpdateItemDialog.showAddOrUpdateItemDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showAddOrUpdateItemDialog.title"), keyField.getText(), null, value, connectInfo, keyTypeEnum, () -> System.out.println("OK"));
        }
    }

    private void createUIComponents() {
        bodyPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                var flatLineBorder = new FlatLineBorder(new Insets(0, 2, 0, 0), UIManager.getColor("Component.borderColor"));
                setBorder(flatLineBorder);
                if (Fn.isNotNull(dataTable)) {
                    dataTableInit();
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
        jToolBar.setBorder(new EmptyBorder(5, 8, 0, 10));

        fieldOrScoreField = new JTextField();
        keyLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.keyLabel.title"));
            }
        };
        keyLabel.setBackground(UIManager.getColor("background"));
        keyLabel.setBorder(new EmptyBorder(0, 2, 0, 2));
        fieldOrScoreField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, keyLabel);
        fieldOrScoreField.setBackground(UIManager.getColor("FlatEditorPane.background"));
        jToolBar.add(fieldOrScoreField);

        jComboBox = new JComboBox<String>();
        jComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_NONE);
        jComboBox.addItem(SyntaxConstants.SYNTAX_STYLE_JSON);
        jComboBox.addActionListener((event) -> {
            var item = jComboBox.getSelectedItem();
            String value = textEditor.textArea().getText();
            if (item instanceof String itemValue) {
                if (Fn.equal(itemValue, SyntaxConstants.SYNTAX_STYLE_JSON)) {
                    if (JSONUtil.isTypeJSON(value)) {
                        SwingUtilities.invokeLater(() -> textEditor.textArea().setText(JSONUtil.toJsonPrettyStr(value)));
                    }
                }
            }
        });
        jComboBox.setBackground(UIManager.getColor("FlatEditorPane.background"));
        jToolBar.add(jComboBox);
        valueUpdateSaveBtn = new JButton() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.valueUpdateSaveBtn.title"));
                setToolTipText(LocaleUtils.getMessageFromBundle("DataViewForm.valueUpdateSaveBtn.toolTip.text"));
            }
        };
        valueUpdateSaveBtn.setEnabled(false);
        valueUpdateSaveBtn.setIcon(UI.SAVE_ICON);
        valueUpdateSaveBtn.addActionListener((e) -> {
            SwingUtilities.invokeLater(this::refreshDisableBtn);
            FutureUtils.runAsync(() -> {
                var keyType = keyTypeLabel.getText();
                Enum.KeyTypeEnum typeEnum = Enum.KeyTypeEnum.valueOf(keyType.toUpperCase());
                var key = keyField.getText();
                var newValue = textEditor.textArea().getText();

                if (typeEnum.equals(Enum.KeyTypeEnum.STRING)) {
                    RedisBasicService.service.del(connectInfo, key);
                    RedisStringService.service.set(connectInfo, key, newValue);
                } else {

                    var row = dataTable.getSelectedRow();

                    if (row == -1) {
                        return;
                    }

                    if (typeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
                        var fieldOrScore = fieldOrScoreField.getText();
                        var value = (String) dataTable.getValueAt(row, 2);
                        RedisZSetService.service.zrem(connectInfo, key, value);
                        RedisZSetService.service.zadd(connectInfo, key, Double.parseDouble(fieldOrScore), newValue);
                    }

                    if (typeEnum.equals(Enum.KeyTypeEnum.HASH)) {
                        var fieldOrScore = fieldOrScoreField.getText();
                        var filed = (String) dataTable.getValueAt(row, 0);
                        RedisHashService.service.hdel(connectInfo, key, filed);
                        RedisHashService.service.hset(connectInfo, key, fieldOrScore, newValue);
                    }

                    if (typeEnum.equals(Enum.KeyTypeEnum.LIST)) {
                        var value = (String) dataTable.getValueAt(row, 1);
                        RedisListService.service.lrem(connectInfo, key, 1, value);
                        RedisListService.service.lpush(connectInfo, key, newValue);
                    }

                    if (typeEnum.equals(Enum.KeyTypeEnum.SET)) {
                        var value = (String) dataTable.getValueAt(row, 1);
                        RedisSetService.service.srem(connectInfo, key, value);
                        RedisSetService.service.sadd(connectInfo, key, newValue);
                    }
                }
            });
            AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("DataViewForm.showInformationDialog.updateSuccess.message"));
            SwingUtilities.invokeLater(this::refreshEnableBtn);
        });
        jToolBar.add(valueUpdateSaveBtn);


        valueViewPanel.add(new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setLayout(new BorderLayout());
                setBorder(new FlatEmptyBorder(0, 0, 5, 0));
                add(new JSeparator(), BorderLayout.NORTH);
                add(jToolBar, BorderLayout.SOUTH);
            }
        }, BorderLayout.NORTH);

        textEditor = TextEditor.newInstance();
        valueViewPanel.add(new JPanel() {
            {
                setLayout(new BorderLayout());
                setBorder(new FlatEmptyBorder(0, 10, 5, 10));
                add(textEditor, BorderLayout.CENTER);
            }
        }, BorderLayout.CENTER);

        basicPanel = new JPanel();

        loadMoreBtn = new JButton();
        loadMoreBtn.setIcon(UI.LOAD_MORE_ICON);
        loadMoreBtn.addActionListener((e) -> {
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
        delBtn = new JButton(UI.DELETE_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.delBtn.title"));
                setToolTipText(LocaleUtils.getMessageFromBundle("DataViewForm.delBtn.toolTip.Text"));
            }
        };
        delBtn.addActionListener(e -> deleteActionHandler.handle());

        refBtn = new JButton(UI.REFRESH_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.refBtn.title"));
                setToolTipText(LocaleUtils.getMessageFromBundle("DataViewForm.refBtn.toolTip.Text"));
            }
        };
        refBtn.addActionListener(e -> reloadAllActionPerformed());

        saveBtn = new JButton(UI.SAVE_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.saveBtn.title"));
                setToolTipText(LocaleUtils.getMessageFromBundle("DataViewForm.saveBtn.toolTip.Text"));
            }
        };
        saveBtn.addActionListener((e) -> {
            SwingUtilities.invokeLater(() -> {
                refreshBeforeHandler.handle();
                refreshDisableBtn();
            });
            String ttl = ttlField.getText();
            String key = keyField.getText();
            if (Fn.notEqual(key, lastKeyName)) {
                RedisBasicService.service.rename(connectInfo, lastKeyName, key);
            }
            if (Fn.notEqual(ttl, lastKeyTTL.toString())) {
                RedisBasicService.service.expire(connectInfo, key, Long.valueOf(ttl));
            }
            reloadAllActionPerformed();
            SwingUtilities.invokeLater(() -> {
                refreshEnableBtn();
                refreshAfterHandler.handle();
            });
        });
        tableSearchField = new JTextField() {
            @Override
            public void updateUI() {
                super.updateUI();
                putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, LocaleUtils.getMessageFromBundle("DataViewForm.tableSearchField.placeholder.text"));
            }
        };

        var searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.addActionListener(actionEvent -> reloadTableDataActionPerformed(true));
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) textField -> {
            tableSearchField.setText("");
            reloadTableDataActionPerformed(true);
        });
        tableAddBtn = new JButton(UI.PLUS_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.tableAddBtn.title"));
            }
        };

        tableAddBtn.addActionListener((event) -> {
            var keyType = keyTypeLabel.getText();
            Enum.KeyTypeEnum keyTypeEnum = Enum.KeyTypeEnum.valueOf(keyType.toUpperCase());

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
                AddOrUpdateItemDialog.showAddOrUpdateItemDialog("插入元素", keyField.getText(), null, null, connectInfo, keyTypeEnum, () -> System.out.println("添加成功！"));
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.HASH)) {
                AddOrUpdateItemDialog.showAddOrUpdateItemDialog("插入元素", keyField.getText(), null, null, connectInfo, keyTypeEnum, () -> System.out.println("添加成功！"));
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.LIST)) {
                AddOrUpdateItemDialog.showAddOrUpdateItemDialog("插入元素", keyField.getText(), null, null, connectInfo, keyTypeEnum, () -> System.out.println("添加成功！"));
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.SET)) {
                AddOrUpdateItemDialog.showAddOrUpdateItemDialog("插入元素", keyField.getText(), null, null, connectInfo, keyTypeEnum, () -> System.out.println("添加成功！"));
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.STREAM)) {
                AddOrUpdateItemDialog.showAddOrUpdateItemDialog("插入元素", keyField.getText(), null, null, connectInfo, keyTypeEnum, () -> System.out.println("添加成功！"));
            }
        });

        tableDelBtn = new JButton(UI.DELETE_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.tableDelBtn.title"));
            }
        };
        tableDelBtn.addActionListener((event) -> {
            var row = dataTable.getSelectedRow();

            if (row == -1) {
                return;
            }
            tableDelBtn.setEnabled(false);
            var keyType = keyTypeLabel.getText();
            Enum.KeyTypeEnum keyTypeEnum = Enum.KeyTypeEnum.valueOf(keyType.toUpperCase());
            String key = keyField.getText();
            if (keyTypeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
                var value = (String) dataTable.getValueAt(row, 2);
                RedisZSetService.service.zrem(connectInfo, key, value);
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.HASH)) {
                var field = (String) dataTable.getValueAt(row, 0);
                RedisHashService.service.hdel(connectInfo, key, field);
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.LIST)) {
                var value = (String) dataTable.getValueAt(row, 1);
                RedisListService.service.lrem(connectInfo, key, 1, value);
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.SET)) {
                var value = (String) dataTable.getValueAt(row, 1);
                RedisSetService.service.srem(connectInfo, key, value);
            }

            if (keyTypeEnum.equals(Enum.KeyTypeEnum.STREAM)) {
                var id = (String) dataTable.getValueAt(row, 1);
                RedisStreamService.service.xdel(connectInfo, key, id);
            }

            tableDelBtn.setEnabled(false);
            reloadTableDataActionPerformed(true);
        });
        tableRefreshBtn = new JButton(UI.REFRESH_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.tableRefreshBtn.title"));
            }
        };

        tableRefreshBtn.addActionListener(e -> {
            if (tableRefreshBtn.isEnabled()) {
                tableRefreshBtn.setEnabled(false);
                reloadTableDataActionPerformed(true);
            }
        });

        var pageNumLabel = new JLabel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.pageNumLabel.title"));
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
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.pageSizeLabel.title"));
            }
        };
        pageSizeLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        allCountField = new JTextField();
        allCountField.setEnabled(false);
        allCountField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, pageSizeLabel);

        closeBtn = new JButton(UI.CLOSE_ICON) {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(LocaleUtils.getMessageFromBundle("DataViewForm.closeBtn.title"));
                setToolTipText(LocaleUtils.getMessageFromBundle("DataViewForm.closeBtn.toolTip.Text"));
            }
        };

        closeBtn.addActionListener(e -> closeActionHandler.handle());

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
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        bodyPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.add(bodyPanel, BorderLayout.CENTER);
        basicPanel.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        bodyPanel.add(basicPanel, BorderLayout.NORTH);
        basicPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        Font keyTypeLabelFont = this.$$$getFont$$$(null, Font.BOLD, 14, keyTypeLabel.getFont());
        if (keyTypeLabelFont != null) keyTypeLabel.setFont(keyTypeLabelFont);
        keyTypeLabel.setText("");
        basicPanel.add(keyTypeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyField = new JTextField();
        basicPanel.add(keyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        this.$$$loadButtonText$$$(saveBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.saveBtn.title"));
        saveBtn.setToolTipText(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.saveBtn.toolTip.Text"));
        basicPanel.add(saveBtn, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        this.$$$loadButtonText$$$(delBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.delBtn.title"));
        delBtn.setToolTipText(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.delBtn.toolTip.Text"));
        basicPanel.add(delBtn, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        this.$$$loadButtonText$$$(refBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.refBtn.title"));
        refBtn.setToolTipText(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.refBtn.toolTip.Text"));
        basicPanel.add(refBtn, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(ttlField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        basicPanel.add(panel1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lengthLabel = new JLabel();
        lengthLabel.setText("");
        panel1.add(lengthLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        keySizeLabel = new JLabel();
        keySizeLabel.setText("");
        panel1.add(keySizeLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        this.$$$loadButtonText$$$(closeBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.closeBtn.title"));
        closeBtn.setToolTipText(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.closeBtn.toolTip.Text"));
        basicPanel.add(closeBtn, new GridConstraints(1, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.add(tableSearchField, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableDelBtn.setEnabled(true);
        this.$$$loadButtonText$$$(tableDelBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.tableDelBtn.title"));
        panel3.add(tableDelBtn, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        this.$$$loadButtonText$$$(tableRefreshBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.tableRefreshBtn.title"));
        panel3.add(tableRefreshBtn, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        this.$$$loadButtonText$$$(tableAddBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.tableAddBtn.title"));
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
        this.$$$loadButtonText$$$(loadMoreBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "DataViewForm.loadMoreBtn.title"));
        panel5.add(loadMoreBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, -1), null, 0, false));
        panel2.add(tableScorePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        dataTable = new JTable();
        tableScorePanel.setViewportView(dataTable);
        valueViewPanel.setMinimumSize(new Dimension(-1, -1));
        dataSplitPanel.setRightComponent(valueViewPanel);
        valueViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
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
