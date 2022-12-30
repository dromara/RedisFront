package com.redisfront.ui.dialog;

import cn.hutool.extra.ssh.JschRuntimeException;
import com.formdev.flatlaf.util.StringUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ProcessHandler;
import com.redisfront.commons.ui.AbstractDialog;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LoadingUtils;
import com.redisfront.commons.util.LocaleUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import io.lettuce.core.RedisConnectionException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

public class AddConnectDialog extends AbstractDialog<ConnectInfo> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField titleField;
    private JTextField hostField;
    private JTextField userField;
    private JSpinner portField;
    private JCheckBox showPasswordCheckBox;
    private JRadioButton enableSSLBtn;
    private JRadioButton enableSSHBtn;
    private JTextField publicKeyField;
    private JButton publicKeyFileBtn;
    private JPanel sslPanel;
    private JLabel passwordLabel;
    private JLabel hostLabel;
    private JLabel userLabel;
    private JLabel portLabel;
    private JPasswordField passwordField;
    private JPanel sshPanel;
    private JTextField sshHostField;
    private JSpinner sshPortField;
    private JTextField sshUserField;
    private JCheckBox enableSshPrivateKey;
    private JPasswordField sshPasswordField;
    private JTextField sshPrivateKeyFile;
    private JButton sshPrivateKeyBtn;
    private JCheckBox showShhPassword;
    private JPanel basicPanel;
    private JButton testBtn;
    private JPasswordField sslPasswordField;
    private JCheckBox showSslPassword;
    private JLabel titleLabel;
    private JLabel sshHostLabel;
    private JLabel sshUserLabel;
    private JLabel sshPortLabel;
    private JLabel sshPasswordLabel;
    private Integer id;

    /***
     * 打开添加连接窗口
     */
    public static void showAddConnectDialog(ProcessHandler<ConnectInfo> callback) {
        var addConnectDialog = new AddConnectDialog(RedisFrontApplication.frame, callback);
        addConnectDialog.setResizable(false);
        addConnectDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        addConnectDialog.pack();
        addConnectDialog.setVisible(true);
    }

    /**
     * 打开编辑连接窗口
     */
    public static void showEditConnectDialog(ConnectInfo connectInfo, ProcessHandler<ConnectInfo> callback) {
        var addConnectDialog = new AddConnectDialog(RedisFrontApplication.frame, callback);
        //数据初始化
        addConnectDialog.componentsDataInit(connectInfo);
        addConnectDialog.setResizable(false);
        addConnectDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        addConnectDialog.pack();
        addConnectDialog.setVisible(true);
    }


    public AddConnectDialog(Frame owner, ProcessHandler<ConnectInfo> callback) {
        super(owner, callback);
        $$$setupUI$$$();
        this.setTitle(LocaleUtils.getMessageFromBundle("AddConnectDialog.Title"));
        this.setModal(true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(400, 280));
        this.setContentPane(contentPane);
        this.getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.id = 0;
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        this.contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.sshPrivateKeyFile.setVisible(enableSshPrivateKey.isSelected());
        this.sshPrivateKeyBtn.setVisible(enableSshPrivateKey.isSelected());
        this.initComponentListener();
    }

    private void initComponentListener() {

        buttonOK.addActionListener(this::submitActionPerformed);
        buttonCancel.addActionListener(this::cancelActionPerformed);

        showPasswordCheckBox.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });

        showShhPassword.addActionListener(e -> {
            if (showShhPassword.isSelected()) {
                sshPasswordField.setEchoChar((char) 0);
            } else {
                sshPasswordField.setEchoChar('*');
            }
        });

        showSslPassword.addActionListener(e -> {
            if (showPasswordCheckBox.isSelected()) {
                sslPasswordField.setEchoChar((char) 0);
            } else {
                sslPasswordField.setEchoChar('*');
            }
        });

        enableSshPrivateKey.addActionListener(e -> {
            if (enableSshPrivateKey.isSelected()) {
                setSize(new Dimension(getWidth(), getHeight() + 20));
            } else {
                setSize(new Dimension(getWidth(), getHeight() - 20));
            }
            sshPrivateKeyFile.setVisible(enableSshPrivateKey.isSelected());
            sshPrivateKeyBtn.setVisible(enableSshPrivateKey.isSelected());
        });

        enableSSLBtn.addActionListener(e -> {
            if (enableSSHBtn.isSelected()) {
                enableSSHBtn.setSelected(false);
                setSize(new Dimension(getWidth(), getHeight() - 120));
            }
            sshPanel.setVisible(false);
            sslPanel.setVisible(enableSSLBtn.isSelected());
            if (enableSSLBtn.isSelected()) {
                setSize(new Dimension(getWidth(), getHeight() + 80));
            } else {
                setSize(new Dimension(getWidth(), getHeight() - 80));
            }
        });

        enableSSHBtn.addActionListener(e -> {
            if (enableSSLBtn.isSelected()) {
                enableSSLBtn.setSelected(false);
                setSize(new Dimension(getWidth(), getHeight() - 130));
            }
            sslPanel.setVisible(false);
            sshPanel.setVisible(enableSSHBtn.isSelected());
            if (enableSSHBtn.isSelected()) {
                if (enableSshPrivateKey.isSelected()) {
                    setSize(new Dimension(getWidth(), getHeight() + 140));
                } else {
                    setSize(new Dimension(getWidth(), getHeight() + 120));
                }
            } else {
                setSize(new Dimension(getWidth(), getHeight() - 120));
            }
        });


        publicKeyFileBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("*.jks", "pem", "jks"));
                fileChooser.showDialog(AddConnectDialog.this, "选择公钥文件");
                var selectedFile = fileChooser.getSelectedFile();
                if (Fn.isNotNull(selectedFile)) {
                    publicKeyField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        sshPrivateKeyBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter(LocaleUtils.getMessageFromBundle("AddConnectDialog.FileChooser.sshPrivateKey.title"), "pem", "crt"));
                fileChooser.showDialog(AddConnectDialog.this, LocaleUtils.getMessageFromBundle("AddConnectDialog.FileChooser.sshPrivateKey.btn"));
                var selectedFile = fileChooser.getSelectedFile();
                if (Fn.isNotNull(selectedFile)) {
                    sshPrivateKeyFile.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        testBtn.addActionListener(e -> testConnect());
    }

    private Boolean testConnect() {
        var connectSuccess = false;
        try {
            var connectInfo = validGetConnectInfo();
            if (RedisBasicService.service.ping(connectInfo)) {
                AlertUtils.showInformationDialog(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.test.success.message"));
                connectSuccess = true;
            } else {
                AlertUtils.showInformationDialog(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.test.fail.message"));
            }
        } catch (Exception exception) {
            if (exception instanceof RedisFrontException) {
                if (exception.getCause() instanceof JschRuntimeException jschRuntimeException) {
                    AlertUtils.showErrorDialog(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.test.fail.message"), jschRuntimeException.getCause());
                } else {
                    AlertUtils.showErrorDialog(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.test.fail.message"), exception);
                }
            } else if (exception instanceof RedisConnectionException) {
                AlertUtils.showErrorDialog(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.test.fail.message"), exception.getCause());
            } else {
                AlertUtils.showErrorDialog(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.test.fail.message"), exception);
            }
        }
        return connectSuccess;
    }

    private void cancelActionPerformed(ActionEvent actionEvent) {
        dispose();
    }


    private void submitActionPerformed(ActionEvent actionEvent) {
        var connectInfo = validGetConnectInfo();
        var connectSuccess = testConnect();
        if (connectSuccess) {
            FutureUtils.runAsync(() -> {
                LoadingUtils.showDialog();
                var redisMode = RedisBasicService.service.getRedisModeEnum(connectInfo);
                processHandler.processHandler(connectInfo.setRedisModeEnum(redisMode));
            });
            dispose();
        }
    }

    private ConnectInfo validGetConnectInfo() {
        if (StringUtils.isEmpty(titleField.getText())) {
            titleField.requestFocus();
            throw new RedisFrontException(LocaleUtils.getMessageFromBundle("AddConnectDialog.require.title.message"), false);
        }
        if (StringUtils.isEmpty(hostField.getText())) {
            titleField.requestFocus();
            throw new RedisFrontException(LocaleUtils.getMessageFromBundle("AddConnectDialog.require.host.message"), false);
        }

        var title = titleField.getText();
        if (title.length() < 4) {
            title = title + "(" + hostField.getText() + ")";
        }
        //SSH Connection
        if (enableSSHBtn.isSelected()) {
            //valid sshHostField
            if (Fn.isEmpty(sshHostField.getText())) {
                sshHostField.requestFocus();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("AddConnectDialog.require.sshHost.message"), false);
            }
            //valid sshUserField
            if (Fn.isEmpty(sshUserField.getText())) {
                sshUserField.requestFocus();
                throw new RedisFrontException(LocaleUtils.getMessageFromBundle("AddConnectDialog.require.sshUser.message"), false);
            }
            //valid enableSshPrivateKey
            if (enableSshPrivateKey.isSelected()) {
                if (Fn.isEmpty(sshPrivateKeyFile.getText())) {
                    sshPrivateKeyFile.requestFocus();
                    throw new RedisFrontException(LocaleUtils.getMessageFromBundle("AddConnectDialog.require.sshPrivateKey.message"), false);
                }
            }
            //sshConfig
            var sshConfig = new ConnectInfo.SSHConfig(
                    sshPrivateKeyFile.getText(),
                    sshUserField.getText(),
                    sshHostField.getText(),
                    (Integer) sshPortField.getValue(),
                    new String(sshPasswordField.getPassword()));

            return new ConnectInfo(
                    title,
                    hostField.getText(),
                    (Integer) portField.getValue(),
                    userField.getText(),
                    String.valueOf(passwordField.getPassword()),
                    0,
                    enableSSLBtn.isSelected(),
                    Enum.Connect.SSH,
                    sshConfig)
                    .setId(id);


        } else if (enableSSLBtn.isSelected()) {
            var sslConfig = new ConnectInfo.SSLConfig(
                    null,
                    publicKeyField.getText(),
                    null,
                    String.valueOf(sslPasswordField.getPassword())
            );
            return new ConnectInfo(title,
                    hostField.getText(),
                    (Integer) portField.getValue(),
                    userField.getText(),
                    String.valueOf(passwordField.getPassword()),
                    0,
                    enableSSLBtn.isSelected(),
                    Enum.Connect.NORMAL,
                    sslConfig)
                    .setId(id);

        } else {

            return new ConnectInfo(title,
                    hostField.getText(),
                    (Integer) portField.getValue(),
                    userField.getText(),
                    String.valueOf(passwordField.getPassword()),
                    0,
                    enableSSLBtn.isSelected(),
                    Enum.Connect.NORMAL)
                    .setId(id);

        }
    }

    /**
     * 数据初始化
     */
    public void componentsDataInit(ConnectInfo connectInfo) {
        this.id = connectInfo.id();
        this.titleField.setText(connectInfo.title());
        this.hostField.setText(connectInfo.host());
        this.portField.setValue(connectInfo.port());
        this.userField.setText(connectInfo.user());
        this.passwordField.setText(connectInfo.password());
        this.enableSSLBtn.setSelected(connectInfo.ssl());
        this.enableSSHBtn.setSelected(Enum.Connect.SSH.equals(connectInfo.connectMode()));
        if (enableSSLBtn.isSelected()) {
            setSize(new Dimension(getWidth(), getHeight() - 130));
            sslPanel.setVisible(true);
            sslPasswordField.setText(connectInfo.sslConfig().getPassword());
            publicKeyField.setText(connectInfo.sslConfig().getPublicKeyFilePath());
        }
        if (enableSSHBtn.isSelected()) {
            setSize(new Dimension(getWidth(), getHeight() + 140));
            sshPanel.setVisible(true);
            sshUserField.setText(connectInfo.sshConfig().getUser());
            sshHostField.setText(connectInfo.sshConfig().getHost());
            sshPasswordField.setText(connectInfo.sshConfig().getPassword());
            sshPortField.setValue(connectInfo.sshConfig().getPort());

            enableSshPrivateKey.setSelected(Fn.isNotEmpty(connectInfo.sshConfig().getPrivateKeyPath()));
            sshPrivateKeyFile.setVisible(enableSshPrivateKey.isSelected());
            sshPrivateKeyBtn.setVisible(enableSshPrivateKey.isSelected());

            sshPrivateKeyFile.setText(connectInfo.sshConfig().getPrivateKeyPath());
        }
    }

    private void createUIComponents() {
        sslPanel = new JPanel();
        sslPanel.setVisible(false);
        sshPanel = new JPanel();
        sshPanel.setVisible(false);
        enableSSLBtn = new JRadioButton();
        enableSSLBtn.setSelected(false);
        enableSSHBtn = new JRadioButton();
        enableSSHBtn.setSelected(false);
        passwordField = new JPasswordField();
        passwordField.setEchoChar('*');
        sslPasswordField = new JPasswordField();
        sslPasswordField.setEchoChar('*');
        portField = new JSpinner();
        portField.setEditor(new JSpinner.NumberEditor(portField, "####"));
        portField.setValue(6379);
        hostField = new JTextField();
        hostField.setText("127.0.0.1");
        testBtn = new JButton();
        testBtn.setText("测试连接");
        testBtn.setIcon(UI.TEST_CONNECTION_ICON);
        sshPortField = new JSpinner();
        sshPortField.setEditor(new JSpinner.NumberEditor(sshPortField, "####"));
        sshPortField.setValue(22);
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
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 3, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 10, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        this.$$$loadButtonText$$$(buttonOK, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.buttonOK.Title"));
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.buttonCancel.Title"));
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        this.$$$loadButtonText$$$(testBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.testBtn.Title"));
        panel1.add(testBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        sslPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(sslPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("证书");
        sslPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        publicKeyField = new JTextField();
        sslPanel.add(publicKeyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        publicKeyFileBtn = new JButton();
        publicKeyFileBtn.setText("选择文件");
        sslPanel.add(publicKeyFileBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        sslPanel.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("密码");
        sslPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sslPanel.add(sslPasswordField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        showSslPassword = new JCheckBox();
        showSslPassword.setText("显示密码");
        sslPanel.add(showSslPassword, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPanel.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(sshPanel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        sshHostLabel = new JLabel();
        this.$$$loadLabelText$$$(sshHostLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.sshHostLabel.Title"));
        sshPanel.add(sshHostLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshHostField = new JTextField();
        sshPanel.add(sshHostField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sshUserLabel = new JLabel();
        this.$$$loadLabelText$$$(sshUserLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.sshUserLabel.Title"));
        sshPanel.add(sshUserLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshUserField = new JTextField();
        sshPanel.add(sshUserField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        enableSshPrivateKey = new JCheckBox();
        this.$$$loadButtonText$$$(enableSshPrivateKey, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.enableSshPrivateKey.Title"));
        sshPanel.add(enableSshPrivateKey, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPortLabel = new JLabel();
        this.$$$loadLabelText$$$(sshPortLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.sshPortLabel.Title"));
        sshPanel.add(sshPortLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPrivateKeyFile = new JTextField();
        sshPanel.add(sshPrivateKeyFile, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sshPrivateKeyBtn = new JButton();
        this.$$$loadButtonText$$$(sshPrivateKeyBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.sshPrivateKeyBtn.Title"));
        sshPanel.add(sshPrivateKeyBtn, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showShhPassword = new JCheckBox();
        this.$$$loadButtonText$$$(showShhPassword, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.showShhPassword.Title"));
        sshPanel.add(showShhPassword, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPasswordLabel = new JLabel();
        this.$$$loadLabelText$$$(sshPasswordLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.sshPasswordLabel.Title"));
        sshPanel.add(sshPasswordLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPanel.add(sshPortField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPasswordField = new JPasswordField();
        sshPanel.add(sshPasswordField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        basicPanel = new JPanel();
        basicPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(basicPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        titleLabel = new JLabel();
        this.$$$loadLabelText$$$(titleLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.titleLabel.Title"));
        titleLabel.setToolTipText(this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.titleLabel.Desc"));
        basicPanel.add(titleLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleField = new JTextField();
        basicPanel.add(titleField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        hostLabel = new JLabel();
        this.$$$loadLabelText$$$(hostLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.hostLabel.Title"));
        basicPanel.add(hostLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordLabel = new JLabel();
        this.$$$loadLabelText$$$(passwordLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.passwordLabel.Title"));
        basicPanel.add(passwordLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userLabel = new JLabel();
        this.$$$loadLabelText$$$(userLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.userLabel.Title"));
        basicPanel.add(userLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(hostField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        userField = new JTextField();
        basicPanel.add(userField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        portLabel = new JLabel();
        this.$$$loadLabelText$$$(portLabel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.portLabel.Title"));
        basicPanel.add(portLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(portField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showPasswordCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(showPasswordCheckBox, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "AddConnectDialog.showPasswordCheckBox.Title"));
        basicPanel.add(showPasswordCheckBox, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enableSSLBtn.setEnabled(true);
        enableSSLBtn.setText("SSL/TLS");
        basicPanel.add(enableSSLBtn, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        basicPanel.add(spacer3, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        basicPanel.add(passwordField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        enableSSHBtn.setText("SSH");
        basicPanel.add(enableSSHBtn, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel3.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel3.add(spacer5, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel3.add(spacer6, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        return contentPane;
    }

}
