package org.dromara.redisfront.ui.dialog;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.*;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;
import org.dromara.redisfront.ui.event.AddKeySuccessEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AddKeyDialog extends QSDialog<RedisFrontWidget> {
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
    private JLabel keyLabel;
    private JLabel typeLabel;
    private JLabel valueLabel;

    private final String parentKey;

    private final RedisConnectContext redisConnectContext;
    private final RedisFrontContext redisFrontContext;

    public static void showAddDialog(RedisFrontWidget redisFrontWidget, RedisConnectContext redisConnectContext, String parent) {
        var addKeyDialog = new AddKeyDialog(redisFrontWidget, redisConnectContext, parent);
        addKeyDialog.setResizable(false);
        addKeyDialog.setLocationRelativeTo(redisFrontWidget);
        addKeyDialog.pack();
        addKeyDialog.setVisible(true);
    }

    public AddKeyDialog(RedisFrontWidget redisFrontWidget, RedisConnectContext redisConnectContext, String parent) {
        super(redisFrontWidget, redisFrontWidget.$tr("AddKeyDialog.title"), true);
        this.setMinimumSize(new Dimension(500, 400));
        this.redisFrontContext = (RedisFrontContext) redisFrontWidget.getContext();
        this.redisConnectContext = redisConnectContext;
        this.buttonOK.addActionListener(_ -> onOK());
        this.buttonCancel.addActionListener(_ -> onCancel());
        this.setContentPane(contentPane);
        this.getRootPane().setDefaultButton(buttonOK);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        this.contentPane.registerKeyboardAction(_ -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        if (RedisFrontUtils.isNotEmpty(parent)) {
            var separatorLabel = new JLabel();
            var separator = redisConnectContext.getSetting().getKeySeparator();
            this.parentKey = parent + separator + " ";
            separatorLabel.setText(parentKey);
            separatorLabel.setOpaque(true);
            separatorLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
            this.keyNameField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, separatorLabel);
        } else {
            this.parentKey = "";
        }

        for (KeyTypeEnum typeEnum : KeyTypeEnum.values()) {
            this.keyTypeComboBox.addItem(typeEnum.typeName());
        }

        this.scoreLabel.setVisible(false);
        this.hashKeyLabel.setVisible(false);
        this.streamLabel.setVisible(false);
        this.hashKeyField.setVisible(false);
        this.zSetScoreField.setVisible(false);
        this.zSetScoreField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                return NumberUtil.isNumber(jTextField.getText());
            }
        });
        this.streamField.setVisible(false);
        this.streamField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField jTextField = (JTextField) input;
                if (RedisFrontUtils.notEqual("*", jTextField.getText()) && jTextField.getText().contains("-")) {
                    return Arrays.stream(jTextField.getText().split("-")).allMatch(NumberUtil::isNumber);
                }
                return NumberUtil.isNumber(jTextField.getText()) || RedisFrontUtils.equal("*", jTextField.getText());
            }
        });
        this.ttlSpinner.setValue(-1);

        this.keyNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, getOwner().$tr("AddKeyDialog.keyNameField.placeholder.text"));

        initComponentListener();
    }

    private void initComponentListener() {

        this.ttlSpinner.addChangeListener(_ -> {
            if (((Integer) ttlSpinner.getValue()) < 0) {
                this.ttlSpinner.setValue(-1);
            }
            if (((Integer) ttlSpinner.getValue()) == 0) {
                this.ttlSpinner.setValue(1);
            }
        });

        this.keyTypeComboBox.addActionListener(_ -> {
            String selectItem = (String) keyTypeComboBox.getSelectedItem();
            if (RedisFrontUtils.equal(KeyTypeEnum.HASH.typeName(), selectItem)) {
                this.hashKeyField.setVisible(true);
                this.hashKeyLabel.setVisible(true);

                this.zSetScoreField.setVisible(false);
                this.scoreLabel.setVisible(false);

                this.streamField.setVisible(false);
                this.streamLabel.setVisible(false);

            } else if (RedisFrontUtils.equal(KeyTypeEnum.STREAM.typeName(), selectItem)) {
                this.hashKeyField.setVisible(false);
                this.hashKeyLabel.setVisible(false);

                this.zSetScoreField.setVisible(false);
                this.scoreLabel.setVisible(false);

                this.streamField.setVisible(true);
                this.streamLabel.setVisible(true);

            } else if (RedisFrontUtils.equal(KeyTypeEnum.ZSET.typeName(), selectItem)) {
                this.hashKeyField.setVisible(false);
                this.hashKeyLabel.setVisible(false);

                this.zSetScoreField.setVisible(true);
                this.scoreLabel.setVisible(true);

                this.streamLabel.setVisible(false);
                this.streamField.setVisible(false);
            } else {
                this.hashKeyField.setVisible(false);
                this.hashKeyLabel.setVisible(false);

                this.zSetScoreField.setVisible(false);
                this.scoreLabel.setVisible(false);

                this.streamField.setVisible(false);
                this.streamLabel.setVisible(false);
            }
        });

    }


    private void validParam() {
        var selectItem = (String) keyTypeComboBox.getSelectedItem();

        if (RedisFrontUtils.isEmpty(keyNameField.getText())) {
            throw new RedisFrontException(getOwner().$tr("AddKeyDialog.require.text"), keyNameField);
        }

        if (RedisFrontUtils.startWith(keyNameField.getText(), redisConnectContext.getSetting().getKeySeparator()) || RedisFrontUtils.endsWith(keyNameField.getText(), redisConnectContext.getSetting().getKeySeparator())) {
            throw new RedisFrontException("key不能以 ’" + redisConnectContext.getSetting().getKeySeparator() + "' 开头或结尾！", keyNameField);
        }

        if (RedisFrontUtils.equal(KeyTypeEnum.HASH.typeName(), selectItem) && RedisFrontUtils.isEmpty(hashKeyField.getText())) {
            throw new RedisFrontException(getOwner().$tr("AddKeyDialog.require.text"), hashKeyField);
        }

        if (RedisFrontUtils.equal(KeyTypeEnum.STREAM.typeName(), selectItem) && RedisFrontUtils.isEmpty(streamField.getText())) {
            throw new RedisFrontException(getOwner().$tr("AddKeyDialog.require.text"), streamField);
        }

        if (RedisFrontUtils.equal(KeyTypeEnum.ZSET.typeName(), selectItem) && RedisFrontUtils.isEmpty(zSetScoreField.getText())) {
            throw new RedisFrontException(getOwner().$tr("AddKeyDialog.require.text"), zSetScoreField);
        }

        if (RedisFrontUtils.isEmpty(keyValueField.getText())) {
            throw new RedisFrontException(getOwner().$tr("AddKeyDialog.require.text"), keyValueField);
        }
    }

    private void onOK() {
        SyncLoadingDialog.builder(getOwner()).showSyncLoadingDialog(() -> {
            validParam();
            var key = parentKey + keyNameField.getText();
            var value = keyValueField.getText();
            var ttl = ((Integer) ttlSpinner.getValue());
            var selectItem = (String) keyTypeComboBox.getSelectedItem();

            if (RedisFrontUtils.equal(KeyTypeEnum.HASH.typeName(), selectItem)) {
                RedisHashService.service.hset(redisConnectContext, key, hashKeyField.getText(), keyValueField.getText());
            } else if (RedisFrontUtils.equal(KeyTypeEnum.STREAM.typeName(), selectItem)) {
                var serverInfo = RedisBasicService.service.getServerInfo(redisConnectContext);
                var redisVersion = serverInfo.get("redis_version");
                var x = redisVersion.toString().split("\\.")[0];
                if (Integer.parseInt(x) < 5) {
                    throw new RedisFrontException("Redis版本过低，不支持Stream - [ 当前版本：" + redisVersion + " ]");
                } else if (JSONUtil.isTypeJSON(value)) {
                    HashMap<String, String> bodyMap = new HashMap<>();
                    JSONUtil.parseObj(value).forEach((key1, value1) -> bodyMap.put(key1, value1.toString()));
                    if (RedisFrontUtils.equal(streamField.getText(), "*")) {
                        RedisStreamService.service.xadd(redisConnectContext, key, bodyMap);
                    } else {
                        RedisStreamService.service.xadd(redisConnectContext, streamField.getText(), key, bodyMap);
                    }
                } else {
                    RedisFrontException redisFrontException = new RedisFrontException("stream 请输入JSON格式数据！");
                    redisFrontException.setComponent(this.keyValueField);
                    throw redisFrontException;
                }

            } else if (RedisFrontUtils.equal(KeyTypeEnum.SET.typeName(), selectItem)) {
                RedisSetService.service.sadd(redisConnectContext, key, value);
            } else if (RedisFrontUtils.equal(KeyTypeEnum.LIST.typeName(), selectItem)) {
                RedisListService.service.lpush(redisConnectContext, key, value);
            } else if (RedisFrontUtils.equal(KeyTypeEnum.ZSET.typeName(), selectItem)) {
                RedisZSetService.service.zadd(redisConnectContext, key, Double.parseDouble(zSetScoreField.getText()), value);
            } else {
                RedisStringService.service.set(redisConnectContext, key, value);
            }
            if (ttl > 0) {
                RedisBasicService.service.expire(redisConnectContext, key, ttl.longValue());
            }
            return key;
        }, (key, ex) -> {
            if (ex != null) {
                if (ex instanceof RedisFrontException redisFrontException) {
                    getOwner().displayException(redisFrontException);
                    redisFrontException.getComponent().requestFocus();
                } else {
                    getOwner().displayException(ex);
                }
            } else {
                this.dispose();
                this.redisFrontContext.getEventBus().publish(new AddKeySuccessEvent(key));
            }
        });
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
        streamField.setEditable(true);
        streamField.setEnabled(true);
        streamField.setText("*");
        contentPane.add(streamField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        scoreLabel = new JLabel();
        this.$$$loadLabelText$$$(scoreLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddKeyDialog.scoreLabel.title"));
        contentPane.add(scoreLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hashKeyLabel = new JLabel();
        this.$$$loadLabelText$$$(hashKeyLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddKeyDialog.hashKeyLabel.title"));
        contentPane.add(hashKeyLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        keyLabel = new JLabel();
        this.$$$loadLabelText$$$(keyLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddKeyDialog.keyLabel.title"));
        contentPane.add(keyLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeLabel = new JLabel();
        this.$$$loadLabelText$$$(typeLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddKeyDialog.typeLabel.title"));
        contentPane.add(typeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        streamLabel = new JLabel();
        streamLabel.setText("ID");
        contentPane.add(streamLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        valueLabel = new JLabel();
        this.$$$loadLabelText$$$(valueLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddKeyDialog.valueLabel.title"));
        contentPane.add(valueLabel, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        contentPane.add(scrollPane1, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        keyValueField = new JTextArea();
        scrollPane1.setViewportView(keyValueField);
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
    private void $$$loadLabelText$$$(JLabel component, String text) {
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
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
