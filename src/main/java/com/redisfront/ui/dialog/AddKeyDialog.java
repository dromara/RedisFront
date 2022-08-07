package com.redisfront.ui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ProcessHandler;
import com.redisfront.commons.ui.AbstractDialog;
import com.redisfront.commons.util.PrefUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddKeyDialog extends AbstractDialog<String> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField keyNameField;
    private JComboBox<String> keyTypeComboBox;
    private JTextArea keyValueField;
    private JTextField zSetScoreField;
    private JTextField hashKeyField;
    private JTextField streamField;
    private JLabel scoreLabel;
    private JLabel hashKeyLabel;
    private JLabel streamLabel;
    private JSpinner ttlSpinner;

    private final String parentKey;

    private final ConnectInfo connectInfo;

    public static void showAddDialog(ConnectInfo connectInfo, String parent, ProcessHandler<String> addSuccessProcessHandler) {
        var addKeyDialog = new AddKeyDialog(connectInfo, parent, addSuccessProcessHandler);
        addKeyDialog.setResizable(false);
        addKeyDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        addKeyDialog.pack();
        addKeyDialog.setVisible(true);
    }

    public AddKeyDialog(ConnectInfo connectInfo, String parent, ProcessHandler<String> addSuccessProcessHandler) {
        super(RedisFrontApplication.frame);
        this.setModal(true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(500, 400));
        this.setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("添加");
        this.connectInfo = connectInfo;
        this.processHandler = addSuccessProcessHandler;
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        if (Fn.isNotEmpty(parent)) {
            var separatorLabel = new JLabel();
            var separator = PrefUtils.getState().get(Const.KEY_KEY_SEPARATOR, ":");
            parentKey = parent + separator;
            separatorLabel.setText(parentKey);
            separatorLabel.setOpaque(true);
            separatorLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
            keyNameField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, separatorLabel);
        } else {
            parentKey = "";
        }

        for (Enum.KeyTypeEnum typeEnum : Enum.KeyTypeEnum.values()) {
            keyTypeComboBox.addItem(typeEnum.typeName());
        }

        scoreLabel.setVisible(false);
        hashKeyLabel.setVisible(false);
        streamLabel.setVisible(false);
        hashKeyField.setVisible(false);
        zSetScoreField.setVisible(false);
        streamField.setVisible(false);
        ttlSpinner.setValue(-1);

        keyNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "请输入键名");

        initComponentListener();
    }

    private void initComponentListener() {

        ttlSpinner.addChangeListener(e -> {
            if (((Integer) ttlSpinner.getValue()) < 0) {
                ttlSpinner.setValue(-1);
            }
            if (((Integer) ttlSpinner.getValue()) == 0) {
                ttlSpinner.setValue(1);
            }
        });

        keyTypeComboBox.addActionListener(e -> {
            String selectItem = (String) keyTypeComboBox.getSelectedItem();
            if (Fn.equal(Enum.KeyTypeEnum.HASH.typeName(), selectItem)) {
                hashKeyField.setVisible(true);
                hashKeyLabel.setVisible(true);

                zSetScoreField.setVisible(false);
                scoreLabel.setVisible(false);

                streamField.setVisible(false);
                streamLabel.setVisible(false);

            } else if (Fn.equal(Enum.KeyTypeEnum.STREAM.typeName(), selectItem)) {
                hashKeyField.setVisible(false);
                hashKeyLabel.setVisible(false);

                zSetScoreField.setVisible(false);
                scoreLabel.setVisible(false);

                streamField.setVisible(true);
                streamLabel.setVisible(true);

            } else if (Fn.equal(Enum.KeyTypeEnum.ZSET.typeName(), selectItem)) {
                hashKeyField.setVisible(false);
                hashKeyLabel.setVisible(false);

                zSetScoreField.setVisible(true);
                scoreLabel.setVisible(true);

                streamLabel.setVisible(false);
                streamField.setVisible(false);
            } else {
                hashKeyField.setVisible(false);
                hashKeyLabel.setVisible(false);

                zSetScoreField.setVisible(false);
                scoreLabel.setVisible(false);

                streamField.setVisible(false);
                streamLabel.setVisible(false);
            }
        });

    }


    private void validParam() {
        var selectItem = (String) keyTypeComboBox.getSelectedItem();

        if (Fn.isEmpty(keyNameField.getText())) {
            keyNameField.requestFocus();
            throw new RedisFrontException("参数校验失败");
        }

        if (Fn.equal(Enum.KeyTypeEnum.HASH.typeName(), selectItem) && Fn.isEmpty(hashKeyField.getText())) {
            hashKeyField.requestFocus();
            throw new RedisFrontException("参数校验失败");
        }

        if (Fn.equal(Enum.KeyTypeEnum.STREAM.typeName(), selectItem) && Fn.isEmpty(streamField.getText())) {
            streamField.requestFocus();
            throw new RedisFrontException("参数校验失败");
        }

        if (Fn.equal(Enum.KeyTypeEnum.ZSET.typeName(), selectItem) && Fn.isEmpty(zSetScoreField.getText())) {
            zSetScoreField.requestFocus();
            throw new RedisFrontException("参数校验失败");
        }

        if (Fn.isEmpty(keyValueField.getText())) {
            keyValueField.requestFocus();
            throw new RedisFrontException("参数校验失败");
        }
    }

    private void onOK() {
        validParam();

        var key = parentKey + keyNameField.getText();
        var value = keyValueField.getText();
        var ttl = ((Integer) ttlSpinner.getValue());
        var selectItem = (String) keyTypeComboBox.getSelectedItem();

        if (Fn.equal(Enum.KeyTypeEnum.HASH.typeName(), selectItem)) {
            RedisHashService.service.hset(connectInfo, key, hashKeyField.getText(), keyValueField.getText());
        } else if (Fn.equal(Enum.KeyTypeEnum.STREAM.typeName(), selectItem)) {
            System.out.println("STREAM");
        } else if (Fn.equal(Enum.KeyTypeEnum.SET.typeName(), selectItem)) {
            RedisSetService.service.sadd(connectInfo, key, value);
        } else if (Fn.equal(Enum.KeyTypeEnum.LIST.typeName(), selectItem)) {
            RedisListService.service.lpush(connectInfo, key, value);
        } else if (Fn.equal(Enum.KeyTypeEnum.ZSET.typeName(), selectItem)) {
            RedisZSetService.service.zadd(connectInfo, key, Double.parseDouble(zSetScoreField.getText()), value);
        } else {
            RedisStringService.service.set(connectInfo, key, value);
        }

        if (ttl > 0) {
            RedisBasicService.service.ttl(connectInfo, key);
        }
        dispose();
        //添加成功回調
        processHandler.processHandler(key);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(7, 2, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("确认");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("取消");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        keyNameField = new JTextField();
        panel3.add(keyNameField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("TTL");
        panel3.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ttlSpinner = new JSpinner();
        panel3.add(ttlSpinner, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyTypeComboBox = new JComboBox();
        contentPane.add(keyTypeComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        zSetScoreField = new JTextField();
        contentPane.add(zSetScoreField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        hashKeyField = new JTextField();
        contentPane.add(hashKeyField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        streamField = new JTextField();
        contentPane.add(streamField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scoreLabel = new JLabel();
        scoreLabel.setText("分数");
        contentPane.add(scoreLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hashKeyLabel = new JLabel();
        hashKeyLabel.setText("键名");
        contentPane.add(hashKeyLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("键名");
        contentPane.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("类型");
        contentPane.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        streamLabel = new JLabel();
        streamLabel.setText("ID");
        contentPane.add(streamLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("值");
        contentPane.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        keyValueField = new JTextArea();
        scrollPane1.setViewportView(keyValueField);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
