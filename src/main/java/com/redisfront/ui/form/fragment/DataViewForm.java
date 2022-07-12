package com.redisfront.ui.form.fragment;

import cn.hutool.core.io.unit.DataSizeUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatEmptyBorder;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.service.RedisStringService;
import com.redisfront.commons.Handler.ActionHandler;
import com.redisfront.ui.component.TextEditor;
import com.redisfront.commons.util.ExecutorUtil;
import com.redisfront.commons.util.FutureUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

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
    private JPanel stringViewPanel;
    private JLabel keyTypeLabel;
    private JButton delBtn;
    private JButton refBtn;
    private JLabel lengthLabel;
    private JLabel keySizeLabel;
    private JButton saveBtn;
    private JTextField ttlField;
    private JTable table1;
    private JTextField textField1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JPanel tableViewPanel;
    private JSplitPane dataSplitPanel;
    private TextEditor textEditor;

    private final ConnectInfo connectInfo;

    private ActionHandler deleteActionHandler;

    public static DataViewForm newInstance(ConnectInfo connectInfo) {
        return new DataViewForm(connectInfo);
    }

    public DataViewForm setDeleteActionHandler(ActionHandler handler) {
        this.deleteActionHandler = handler;
        return this;
    }

    public DataViewForm(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        $$$setupUI$$$();
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
                    if (keyTypeEnum == Enum.KeyTypeEnum.STRING) {
                        Long strLen = RedisStringService.service.strlen(connectInfo, key);
                        String value = RedisStringService.service.get(connectInfo, key);
                        SwingUtilities.invokeLater(() -> {
                            lengthLabel.setText("Length: " + strLen);
                            keySizeLabel.setText("Size: " + DataSizeUtil.format(value.getBytes().length));
                            textEditor.textArea().setText(value);
                        });
                    }
                }));
        CompletableFuture.allOf(FutureUtil.completableFuture(() -> RedisBasicService.service.ttl(connectInfo, key), ttl -> SwingUtilities.invokeLater(() ->
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
//        valueViewPanel.add(new JPanel() {
//            @Override
//            public void updateUI() {
//                super.updateUI();
//                setLayout(new BorderLayout());
//                setBorder(new FlatEmptyBorder(0, 0, 5, 0));
//                add(new JSeparator(), BorderLayout.CENTER);
//            }
//        }, BorderLayout.NORTH);

        textEditor = TextEditor.newInstance();
        valueViewPanel.add(textEditor, BorderLayout.CENTER);

        stringViewPanel = new JPanel() {
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
        stringViewPanel.setLayout(new GridLayoutManager(2, 9, new Insets(0, 0, 0, 0), -1, -1));
        bodyPanel.add(stringViewPanel, BorderLayout.NORTH);
        stringViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        keyTypeLabel = new JLabel();
        Font keyTypeLabelFont = this.$$$getFont$$$(null, Font.BOLD, 14, keyTypeLabel.getFont());
        if (keyTypeLabelFont != null) keyTypeLabel.setFont(keyTypeLabelFont);
        keyTypeLabel.setText("");
        stringViewPanel.add(keyTypeLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        stringViewPanel.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        keyField = new JTextField();
        stringViewPanel.add(keyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        lengthLabel = new JLabel();
        lengthLabel.setText("");
        stringViewPanel.add(lengthLabel, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keySizeLabel = new JLabel();
        keySizeLabel.setText("");
        stringViewPanel.add(keySizeLabel, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        delBtn = new JButton();
        delBtn.setText("");
        stringViewPanel.add(delBtn, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refBtn = new JButton();
        refBtn.setText("");
        stringViewPanel.add(refBtn, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveBtn = new JButton();
        saveBtn.setText("");
        stringViewPanel.add(saveBtn, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ttlField = new JTextField();
        stringViewPanel.add(ttlField, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer2 = new Spacer();
        stringViewPanel.add(spacer2, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        bodyPanel.add(panel1, BorderLayout.CENTER);
        dataSplitPanel = new JSplitPane();
        dataSplitPanel.setOrientation(0);
        panel1.add(dataSplitPanel, BorderLayout.CENTER);
        tableViewPanel = new JPanel();
        tableViewPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        dataSplitPanel.setLeftComponent(tableViewPanel);
        tableViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        table1 = new JTable();
        tableViewPanel.add(table1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        tableViewPanel.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        textField1 = new JTextField();
        panel2.add(textField1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        button1 = new JButton();
        button1.setText("Button");
        panel2.add(button1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button2 = new JButton();
        button2.setText("Button");
        panel2.add(button2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button3 = new JButton();
        button3.setText("Button");
        panel2.add(button3, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel2.add(spacer4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        valueViewPanel.setMinimumSize(new Dimension(-1, -1));
        dataSplitPanel.setRightComponent(valueViewPanel);
        valueViewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 10, 8, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JSeparator separator1 = new JSeparator();
        panel1.add(separator1, BorderLayout.NORTH);
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
