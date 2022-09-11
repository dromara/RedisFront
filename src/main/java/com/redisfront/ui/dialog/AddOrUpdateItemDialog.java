package com.redisfront.ui.dialog;

import cn.hutool.json.JSONUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ActionHandler;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class AddOrUpdateItemDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextPane valueTextArea;
    private JLabel nameLabel;
    private JLabel valueLabel;
    private final ActionHandler addSuccessHandler;
    private final Enum.KeyTypeEnum typeEnum;
    private final String fieldOrScore;
    private final String value;
    private final String key;
    private final ConnectInfo connectInfo;

    public static void showAddOrUpdateItemDialog(String title, String key, String fieldOrScore, String value, ConnectInfo connectInfo, Enum.KeyTypeEnum typeEnum, ActionHandler addSuccessHandler) {
        var addOrUpdateItemDialog = new AddOrUpdateItemDialog(title, key, fieldOrScore, value, connectInfo, typeEnum, addSuccessHandler);
        addOrUpdateItemDialog.setResizable(false);
        addOrUpdateItemDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        addOrUpdateItemDialog.pack();
        addOrUpdateItemDialog.setVisible(true);
    }


    public AddOrUpdateItemDialog(String title, String key, String fieldOrScore, String value, ConnectInfo connectInfo, Enum.KeyTypeEnum typeEnum, ActionHandler addSuccessHandler) {
        super(RedisFrontApplication.frame);
        setContentPane(contentPane);
        setTitle(title);
        setModal(true);
        setMinimumSize(new Dimension(400, 300));
        this.addSuccessHandler = addSuccessHandler;
        this.typeEnum = typeEnum;
        this.key = key;
        this.connectInfo = connectInfo;
        this.value = value;
        this.fieldOrScore = fieldOrScore;

        if (Fn.isNotEmpty(value)) {
            valueTextArea.setText(value);
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.ZSET) || typeEnum.equals(Enum.KeyTypeEnum.HASH)) {
            this.nameField.setText(fieldOrScore);
            nameLabel.setVisible(true);
            nameLabel.setText(typeEnum.equals(Enum.KeyTypeEnum.ZSET) ? "分数" : "键");
            nameField.setVisible(true);
        } else if (typeEnum.equals(Enum.KeyTypeEnum.STREAM)) {
            this.nameField.setText(fieldOrScore);
            nameLabel.setVisible(true);
            nameLabel.setText("ID");
            nameField.setVisible(true);
            nameField.setText("*");
            nameField.setEnabled(false);
        } else {
            nameLabel.setVisible(false);
            nameField.setVisible(false);
        }

        initComponentListener();
    }

    private void initComponentListener() {
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        valueLabel.setText("值");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (typeEnum.equals(Enum.KeyTypeEnum.ZSET) || typeEnum.equals(Enum.KeyTypeEnum.HASH)) {
            if (Fn.isEmpty(nameField.getText())) {
                nameField.requestFocus();
            }
        }

        if (Fn.isEmpty(valueTextArea.getText())) {
            valueTextArea.requestFocus();
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.ZSET)) {
            if (Fn.isNotEmpty(value)) {
                RedisZSetService.service.zrem(connectInfo, key, value);
            }
            RedisZSetService.service.zadd(connectInfo, key, Double.parseDouble(nameField.getText()), valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.HASH)) {
            if (Fn.isNotEmpty(fieldOrScore)) {
                RedisHashService.service.hdel(connectInfo, key, fieldOrScore);
            }
            RedisHashService.service.hset(connectInfo, key, nameField.getText(), valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.LIST)) {
            if (Fn.isNotEmpty(value)) {
                RedisListService.service.lrem(connectInfo, key, 1, value);
            }
            RedisListService.service.lpush(connectInfo, key, valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.SET)) {
            if (Fn.isNotEmpty(value)) {
                RedisSetService.service.srem(connectInfo, key, value);
            }
            RedisSetService.service.sadd(connectInfo, key, valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.STREAM)) {
            if (JSONUtil.isTypeJSON(valueTextArea.getText())) {
                HashMap<String, String> bodyMap = new HashMap<>();
                JSONUtil.parseObj(valueTextArea.getText()).forEach((k, v) -> bodyMap.put(k, v.toString()));
                RedisStreamService.service.xadd(connectInfo, key, bodyMap);
            } else {
                AlertUtils.showInformationDialog("stream 请输入JSON - {key:value} 格式数据！");
                valueTextArea.requestFocus();
                return;
            }
        }

        dispose();
        addSuccessHandler.handle();
    }

    private void onCancel() {
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
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
        panel3.setLayout(new GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        valueLabel = new JLabel();
        valueLabel.setText("值");
        panel3.add(valueLabel, new GridConstraints(5, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameField = new JTextField();
        panel3.add(nameField, new GridConstraints(0, 1, 5, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        nameLabel = new JLabel();
        nameLabel.setText("Label");
        panel3.add(nameLabel, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(5, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(400, -1), new Dimension(400, 400), new Dimension(400, -1), 0, false));
        valueTextArea = new JTextPane();
        scrollPane1.setViewportView(valueTextArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
