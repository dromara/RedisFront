package com.redisfront.ui.dialog;

import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ActionHandler;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisHashService;
import com.redisfront.service.RedisListService;
import com.redisfront.service.RedisSetService;
import com.redisfront.service.RedisZSetService;

import javax.swing.*;
import java.awt.event.*;

public class AddOrUpdateItemDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextArea valueTextArea;
    private JLabel nameLabel;
    private JLabel valueLabel;
    private final ActionHandler addSuccessHandler;
    private final Enum.KeyTypeEnum typeEnum;
    private final String key;
    private final ConnectInfo connectInfo;


    public AddOrUpdateItemDialog(String key, Object name, Object vale, ConnectInfo connectInfo, Enum.KeyTypeEnum typeEnum, ActionHandler addSuccessHandler) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.addSuccessHandler = addSuccessHandler;
        this.typeEnum = typeEnum;
        this.key = key;
        this.connectInfo = connectInfo;
        var show = typeEnum.equals(Enum.KeyTypeEnum.ZSET) || typeEnum.equals(Enum.KeyTypeEnum.HASH);
        nameLabel.setVisible(show);
        nameLabel.setText(typeEnum.equals(Enum.KeyTypeEnum.ZSET) ? "分数" : "键");
        nameField.setVisible(show);

        valueLabel.setText("值");

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
            RedisZSetService.service.zadd(connectInfo, key, Double.parseDouble(nameField.getText()), valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.HASH)) {
            RedisHashService.service.hset(connectInfo, key, nameField.getText(), valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.LIST)) {
            RedisListService.service.lpush(connectInfo, key, valueTextArea.getText());
        }

        if (typeEnum.equals(Enum.KeyTypeEnum.SET)) {
            RedisSetService.service.sadd(connectInfo, key, valueTextArea.getText());
        }

        dispose();
        addSuccessHandler.handle();
    }

    private void onCancel() {
        dispose();
    }
}
