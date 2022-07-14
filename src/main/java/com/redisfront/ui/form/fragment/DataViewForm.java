package com.redisfront.ui.form.fragment;

import cn.hutool.core.io.unit.DataSizeUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.commons.Handler.ActionHandler;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.util.ExecutorUtil;
import com.redisfront.commons.util.FutureUtil;
import com.redisfront.model.*;
import com.redisfront.service.*;
import com.redisfront.ui.component.TextEditor;
import com.redisfront.ui.form.MainNoneForm;
import io.lettuce.core.ScoredValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
    private JLabel lengthLabel;
    private JLabel keySizeLabel;

    private JLabel valueSizeLabel;
    private JButton valueSaveBtn;
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
    private JTextField pageNumField;
    private JTextField pageSizeField;
    private JButton prevBtn;
    private JButton nextBtn;
    private TextEditor textEditor;
    private JTextField keyTextField;

    private final ConnectInfo connectInfo;

    private ActionHandler deleteActionHandler;

    public static DataViewForm newInstance(ConnectInfo connectInfo) {
        return new DataViewForm(connectInfo);
    }

    public void setDeleteActionHandler(ActionHandler handler) {
        this.deleteActionHandler = handler;
    }

    public DataViewForm(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        $$$setupUI$$$();

        tableScorePanel.setPreferredSize(new Dimension(500, 190));

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

        dataPanel.add(MainNoneForm.getInstance().getContentPanel(), BorderLayout.CENTER, 0);

        keyTypeLabel.setOpaque(true);
        keyTypeLabel.setForeground(Color.WHITE);
        keyTypeLabel.setBorder(new EmptyBorder(2, 3, 2, 3));

        var keyLabel = new JLabel();
        keyLabel.setText("KEY:");
        keyLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        keyField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, keyLabel);

        var ttlLabel = new JLabel();
        ttlLabel.setText("TTL:");
        ttlLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        ttlField.setSize(5, -1);
        ttlField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, ttlLabel);

        delBtn.setIcon(UI.DELETE_ICON);
        delBtn.setText("删除");
        delBtn.addActionListener(e -> deleteActionHandler.handle());
        delBtn.setToolTipText("删除键");

        refBtn.setIcon(UI.REFRESH_ICON);
        refBtn.setText("重载");
        refBtn.addActionListener(e -> {
            String key = keyField.getText();
            dataChangeActionPerformed(key);
        });
        refBtn.setToolTipText("重载");


        saveBtn.setIcon(UI.SAVE_ICON);
        saveBtn.setText("保存");
        saveBtn.setToolTipText("保存");


        tableSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入关键字");
        var searchBtn = new JButton(new FlatSearchIcon());
        searchBtn.addActionListener(actionEvent -> System.out.println());

        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, searchBtn);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        tableSearchField.putClientProperty(FlatClientProperties.TEXT_FIELD_CLEAR_CALLBACK, (Consumer<JTextComponent>) textField -> System.out.println());

        tableAddBtn.setIcon(UI.PLUS_ICON);
        tableAddBtn.setText("插入元素");

        tableDelBtn.setIcon(UI.DELETE_ICON);
        tableDelBtn.setText("删除元素");

        tableRefreshBtn.setIcon(UI.REFRESH_ICON);
        tableRefreshBtn.setText("重新载入");

        var pageNumLabel = new JLabel();
        pageNumLabel.setText("页码:");
        pageNumLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        pageNumField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, pageNumLabel);

        var pageSizeLabel = new JLabel();
        pageSizeLabel.setText("大小:");
        pageSizeLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        pageSizeField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, pageSizeLabel);

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
                    if (dataTable.getModel() instanceof SortedSetTableModel) {
                        var value = dataTable.getValueAt(row, 2);
                        SwingUtilities.invokeLater(() -> {
                            valueSizeLabel.setText("Size: ".concat(DataSizeUtil.format(((String) value).getBytes().length)));
                            valueSaveBtn.setEnabled(true);
                            textEditor.textArea().setText((String) value);
                        });
                    } else {
                        var value = dataTable.getValueAt(row, 1);
                        SwingUtilities.invokeLater(() -> {
                            valueSizeLabel.setText("Size: ".concat(DataSizeUtil.format(((String) value).getBytes().length)));
                            valueSaveBtn.setEnabled(true);
                            textEditor.textArea().setText((String) value);
                        });
                    }
                }
            }
        });
    }

    public JPanel contentPanel() {
        return contentPanel;
    }

    public void dataChangeActionPerformed(String key) {
        CompletableFuture<Void> updateContentFuture = CompletableFuture.supplyAsync(() -> {
                    String type = RedisBasicService.service.type(connectInfo, key);
                    Enum.KeyTypeEnum keyTypeEnum = Enum.KeyTypeEnum.valueOf(type.toUpperCase());
                    SwingUtilities.invokeLater(() -> {
                        keyTypeLabel.setText(keyTypeEnum.typeName());
                        keyTypeLabel.setBackground(keyTypeEnum.color());
                    });
                    return keyTypeEnum;
                }, ExecutorUtil.getExecutorService())
                .thenAccept((keyTypeEnum -> {
                    if (keyTypeEnum == Enum.KeyTypeEnum.STRING || keyTypeEnum == Enum.KeyTypeEnum.JSON) {
                        Long strLen = RedisStringService.service.strlen(connectInfo, key);
                        String value = RedisStringService.service.get(connectInfo, key);
                        SwingUtilities.invokeLater(() -> {
                            valueSizeLabel.setText("Size: ".concat(DataSizeUtil.format(value.getBytes().length)));
                            valueSaveBtn.setEnabled(true);
                            lengthLabel.setText("Length: " + strLen);
                            keySizeLabel.setText("Size: " + DataSizeUtil.format(value.getBytes().length));
                            textEditor.textArea().setText(value);
                            dataPanel.add(valueViewPanel, BorderLayout.CENTER, 0);
                        });
                    } else if (keyTypeEnum == Enum.KeyTypeEnum.HASH) {
                        Long len = RedisHashService.service.hlen(connectInfo, key);
                        Map<String, String> value = RedisHashService.service.hgetall(connectInfo, key);
                        HashTableModel hashTableModel = new HashTableModel(new ArrayList<>(value.entrySet()));
                        SwingUtilities.invokeLater(() -> {
                            lengthLabel.setText("Length: " + len);
                            keySizeLabel.setText("Size: " + DataSizeUtil.format(value.values().stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0)));
                            dataTable.setModel(hashTableModel);
                            dataPanel.add(dataSplitPanel, BorderLayout.CENTER, 0);
                        });
                    } else if (keyTypeEnum == Enum.KeyTypeEnum.SET) {
                        Long scard = RedisSetService.service.scard(connectInfo, key);
                        Set<String> value = RedisSetService.service.smembers(connectInfo, key);
                        SetTableModel setTableModel = new SetTableModel(new ArrayList<>(value));
                        SwingUtilities.invokeLater(() -> {
                            lengthLabel.setText("Length: " + scard);
                            keySizeLabel.setText("Size: " + DataSizeUtil.format(value.stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0)));
                            dataTable.setModel(setTableModel);
                            dataPanel.add(dataSplitPanel, BorderLayout.CENTER, 0);
                        });
                    } else if (keyTypeEnum == Enum.KeyTypeEnum.ZSET) {
                        Long strLen = RedisZSetService.service.zcard(connectInfo, key);
                        List<ScoredValue<String>> value = RedisZSetService.service.zrange(connectInfo, key, 0, -1);
                        SortedSetTableModel sortedSetTableModel = new SortedSetTableModel(value);
                        SwingUtilities.invokeLater(() -> {
                            lengthLabel.setText("Length: " + strLen);
                            keySizeLabel.setText("Size: " + DataSizeUtil.format(value.stream().map(e -> e.getValue().getBytes().length).reduce(Integer::sum).orElse(0)));
                            dataTable.setModel(sortedSetTableModel);
                            dataPanel.add(dataSplitPanel, BorderLayout.CENTER, 0);
                        });
                    } else if (keyTypeEnum == Enum.KeyTypeEnum.LIST) {
                        Long llen = RedisListService.service.llen(connectInfo, key);
                        List<String> value = RedisListService.service.lrange(connectInfo, key, 0, -1);
                        ListTableModel listTableModel = new ListTableModel(value);
                        SwingUtilities.invokeLater(() -> {
                            lengthLabel.setText("Length: " + llen);
                            keySizeLabel.setText("Size: " + DataSizeUtil.format(value.stream().map(e -> e.getBytes().length).reduce(Integer::sum).orElse(0)));
                            dataTable.setModel(listTableModel);
                            dataPanel.add(dataSplitPanel, BorderLayout.CENTER, 0);
                        });
                    }
                }));

        CompletableFuture.allOf(
                FutureUtil.completableFuture(() -> RedisBasicService.service.ttl(connectInfo, key), ttl -> SwingUtilities.invokeLater(() ->
                        {
                            ttlField.setText(ttl.toString());
                            keyField.setText(key);
                        }
                )), updateContentFuture);
    }

    private void createUIComponents() {
        bodyPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                var flatLineBorder = new FlatLineBorder(new Insets(0, 2, 0, 0), UIManager.getColor("Component.borderColor"));
                setBorder(flatLineBorder);
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

        JToolBar jToolBar = new JToolBar();
        jToolBar.setBorder(new EmptyBorder(0, 10, 0, 15));
        valueSizeLabel = new JLabel();
        valueSizeLabel.setText("Size: 0");
        jToolBar.add(valueSizeLabel);
        jToolBar.add(Box.createGlue());
        valueSaveBtn = new JButton();
        valueSaveBtn.setEnabled(false);
        valueSaveBtn.setIcon(UI.SAVE_ICON);
        jToolBar.add(valueSaveBtn);

        keyTextField = new JTextField();
        var keyLabel = new JLabel();
        keyLabel.setText("Key:");
        keyLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        keyTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, keyLabel);

        var sizeLabel = new JLabel();
        sizeLabel.setText("100KB");
        sizeLabel.setBorder(new EmptyBorder(2, 10, 2, 10));
        keyTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT, sizeLabel);

        valueViewPanel.add(new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
                setLayout(new BorderLayout());
                setBorder(new FlatEmptyBorder(0, 0, 5, 0));
                add(new JSeparator(), BorderLayout.NORTH);
                add(jToolBar, BorderLayout.SOUTH);
                add(new JPanel() {
                    {
                        setLayout(new BorderLayout());
                        setBorder(new FlatEmptyBorder(5, 5, 5, 5));
                        add(keyTextField, BorderLayout.CENTER);
                    }
                }, BorderLayout.CENTER);
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

        basicPanel = new JPanel() {
            @Override
            public void updateUI() {
                super.updateUI();
            }

            {
                setLayout(new BorderLayout());
            }
        };
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
        basicPanel.setLayout(new GridLayoutManager(2, 9, new Insets(0, 0, 0, 0), -1, -1));
        bodyPanel.add(basicPanel, BorderLayout.NORTH);
        basicPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        keyTypeLabel = new JLabel();
        Font keyTypeLabelFont = this.$$$getFont$$$(null, Font.BOLD, 14, keyTypeLabel.getFont());
        if (keyTypeLabelFont != null) keyTypeLabel.setFont(keyTypeLabelFont);
        keyTypeLabel.setText("");
        basicPanel.add(keyTypeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        basicPanel.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        keyField = new JTextField();
        basicPanel.add(keyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        lengthLabel = new JLabel();
        lengthLabel.setText("");
        basicPanel.add(lengthLabel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keySizeLabel = new JLabel();
        keySizeLabel.setText("");
        basicPanel.add(keySizeLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        delBtn = new JButton();
        delBtn.setText("");
        basicPanel.add(delBtn, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refBtn = new JButton();
        refBtn.setText("");
        basicPanel.add(refBtn, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveBtn = new JButton();
        saveBtn.setText("");
        basicPanel.add(saveBtn, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ttlField = new JTextField();
        basicPanel.add(ttlField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        basicPanel.add(spacer2, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        dataPanel = new JPanel();
        dataPanel.setLayout(new BorderLayout(0, 0));
        bodyPanel.add(dataPanel, BorderLayout.CENTER);
        dataSplitPanel = new JSplitPane();
        dataSplitPanel.setOrientation(0);
        dataPanel.add(dataSplitPanel, BorderLayout.CENTER);
        tableViewPanel = new JPanel();
        tableViewPanel.setLayout(new BorderLayout(0, 0));
        dataSplitPanel.setLeftComponent(tableViewPanel);
        tableViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        tableViewPanel.add(panel1, BorderLayout.CENTER);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tableSearchField = new JTextField();
        panel2.add(tableSearchField, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableDelBtn = new JButton();
        tableDelBtn.setText("删除");
        panel2.add(tableDelBtn, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tableRefreshBtn = new JButton();
        tableRefreshBtn.setText("重载");
        panel2.add(tableRefreshBtn, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tableAddBtn = new JButton();
        tableAddBtn.setText("添加");
        panel2.add(tableAddBtn, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pagePanel = new JPanel();
        pagePanel.setLayout(new BorderLayout(0, 0));
        panel2.add(pagePanel, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pagePanel.add(panel3, BorderLayout.NORTH);
        pageNumField = new JTextField();
        panel3.add(pageNumField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        pageSizeField = new JTextField();
        panel3.add(pageSizeField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pagePanel.add(panel4, BorderLayout.SOUTH);
        prevBtn = new JButton();
        prevBtn.setText("上一页");
        panel4.add(prevBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextBtn = new JButton();
        nextBtn.setText("下一页");
        panel4.add(nextBtn, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tableScorePanel = new JScrollPane();
        panel1.add(tableScorePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
