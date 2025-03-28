package org.dromara.redisfront.ui.dialog;

import cn.hutool.json.JSONUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.*;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class AddOrUpdateValueDialog extends QSDialog<RedisFrontWidget> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField nameField;
    private JTextPane valueTextArea;
    private JLabel nameLabel;
    private JLabel valueLabel;
    private final KeyTypeEnum typeEnum;
    private final String fieldOrScore;
    private final String value;
    private final String key;
    private final RedisConnectContext redisConnectContext;
    private final Runnable runnable;

    public static void showDialog(RedisFrontWidget redisFrontWidget, String title, String key, String fieldOrScore, String value, RedisConnectContext redisConnectContext, KeyTypeEnum typeEnum, Runnable runnable) {
        var addOrUpdateItemDialog = new AddOrUpdateValueDialog(redisFrontWidget, title, key, fieldOrScore, value, redisConnectContext, typeEnum, runnable);
        addOrUpdateItemDialog.setResizable(false);
        addOrUpdateItemDialog.setLocationRelativeTo(redisFrontWidget);
        addOrUpdateItemDialog.pack();
        addOrUpdateItemDialog.setVisible(true);
    }


    public AddOrUpdateValueDialog(RedisFrontWidget redisFrontWidget, String title, String key, String fieldOrScore, String value, RedisConnectContext redisConnectContext, KeyTypeEnum typeEnum, Runnable runnable) {
        super(redisFrontWidget, title, true);
        this.runnable = runnable;
        this.setContentPane(contentPane);
        this.setMinimumSize(new Dimension(400, 300));
        this.typeEnum = typeEnum;
        this.key = key;
        this.redisConnectContext = redisConnectContext;
        this.value = value;
        this.fieldOrScore = fieldOrScore;

        if (RedisFrontUtils.isNotEmpty(value)) {
            this.valueTextArea.setText(value);
        }

        if (typeEnum.equals(KeyTypeEnum.ZSET) || typeEnum.equals(KeyTypeEnum.HASH)) {
            this.nameField.setText(fieldOrScore);
            this.nameLabel.setVisible(true);
            this.nameLabel.setText(typeEnum.equals(KeyTypeEnum.ZSET) ? "分数" : "键");
            this.nameField.setVisible(true);
        } else if (typeEnum.equals(KeyTypeEnum.STREAM)) {
            this.nameField.setText(fieldOrScore);
            this.nameLabel.setVisible(true);
            this.nameLabel.setText("ID");
            this.nameField.setVisible(true);
            this.nameField.setText("*");
            this.nameField.setEnabled(false);
        } else {
            this.nameLabel.setVisible(false);
            this.nameField.setVisible(false);
        }

        this.initComponentListener();
    }

    private void initComponentListener() {
        this.getRootPane().setDefaultButton(buttonOK);
        this.buttonOK.addActionListener(_ -> onOK());
        this.buttonCancel.addActionListener(_ -> onCancel());
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.valueLabel.setText("值");
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        this.contentPane.registerKeyboardAction(_ -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if (typeEnum.equals(KeyTypeEnum.ZSET) || typeEnum.equals(KeyTypeEnum.HASH)) {
            if (RedisFrontUtils.isEmpty(nameField.getText())) {
                nameField.requestFocus();
            }
        }

        if (RedisFrontUtils.isEmpty(valueTextArea.getText())) {
            valueTextArea.requestFocus();
        }

        SyncLoadingDialog.builder(getOwner()).showSyncLoadingDialog(() -> {
            if (typeEnum.equals(KeyTypeEnum.ZSET)) {
                if (RedisFrontUtils.isNotEmpty(value)) {
                    RedisZSetService.service.zrem(redisConnectContext, key, value);
                }
                RedisZSetService.service.zadd(redisConnectContext, key, Double.parseDouble(nameField.getText()), valueTextArea.getText());
            }

            if (typeEnum.equals(KeyTypeEnum.HASH)) {
                if (RedisFrontUtils.isNotEmpty(fieldOrScore)) {
                    RedisHashService.service.hdel(redisConnectContext, key, fieldOrScore);
                }
                RedisHashService.service.hset(redisConnectContext, key, nameField.getText(), valueTextArea.getText());
            }

            if (typeEnum.equals(KeyTypeEnum.LIST)) {
                if (RedisFrontUtils.isNotEmpty(value)) {
                    RedisListService.service.lrem(redisConnectContext, key, 1, value);
                }
                RedisListService.service.lpush(redisConnectContext, key, valueTextArea.getText());
            }

            if (typeEnum.equals(KeyTypeEnum.SET)) {
                if (RedisFrontUtils.isNotEmpty(value)) {
                    RedisSetService.service.srem(redisConnectContext, key, value);
                }
                RedisSetService.service.sadd(redisConnectContext, key, valueTextArea.getText());
            }

            if (typeEnum.equals(KeyTypeEnum.STREAM)) {
                if (JSONUtil.isTypeJSON(valueTextArea.getText())) {
                    HashMap<String, String> bodyMap = new HashMap<>();
                    JSONUtil.parseObj(valueTextArea.getText()).forEach((k, v) -> bodyMap.put(k, v.toString()));
                    RedisStreamService.service.xadd(redisConnectContext, key, bodyMap);
                } else {
                    throw new RedisFrontException("stream 请输入JSON - {key:value} 格式数据！", valueTextArea);
                }
            }
            return 0;
        }, (_, e) -> {
            if (e == null) {
                this.runnable.run();
                this.setVisible(false);
                this.dispose();
            } else {
                if (e instanceof RedisFrontException redisFrontException) {
                    redisFrontException.getComponent().requestFocus();
                }
                this.getOwner().displayException(e);
            }
        });
    }

    private void onCancel() {
        this.dispose();
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
