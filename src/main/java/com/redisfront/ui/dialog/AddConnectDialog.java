package com.redisfront.ui.dialog;

import com.formdev.flatlaf.util.StringUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.Handler.ProcessHandler;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.commons.ui.AbstractDialog;
import com.redisfront.commons.util.ExecutorUtil;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LoadingUtil;
import com.redisfront.commons.util.AlertUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;

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
    private JTextField privateKeyField;
    private JButton privateKeyFileBtn;
    private JTextField grantField;
    private JButton grantFileBtn;
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
        this.setTitle("新建连接");
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
                setSize(new Dimension(getWidth(), getHeight() + 130));
            } else {
                setSize(new Dimension(getWidth(), getHeight() - 130));
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
                setSize(new Dimension(getWidth(), getHeight() + 120));
            } else {
                setSize(new Dimension(getWidth(), getHeight() - 120));
            }
        });


        privateKeyFileBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("密钥文件", "pem", "crt"));
                fileChooser.showDialog(AddConnectDialog.this, "选择私钥文件");
                var selectedFile = fileChooser.getSelectedFile();
                if (Fn.isNotNull(selectedFile)) {
                    privateKeyField.setText(selectedFile.getAbsolutePath());
                }
            }
        });
        publicKeyFileBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("公钥文件", "pem", "crt"));
                fileChooser.showDialog(AddConnectDialog.this, "选择公钥文件");
                var selectedFile = fileChooser.getSelectedFile();
                if (Fn.isNotNull(selectedFile)) {
                    publicKeyField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        grantFileBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("授权文件", "pem", "crt"));
                fileChooser.showDialog(AddConnectDialog.this, "选择授权文件");
                var selectedFile = fileChooser.getSelectedFile();
                if (Fn.isNotNull(selectedFile)) {
                    grantField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        sshPrivateKeyBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("私钥文件", "pem", "crt"));
                fileChooser.showDialog(AddConnectDialog.this, "选择私钥文件");
                var selectedFile = fileChooser.getSelectedFile();
                if (Fn.isNotNull(selectedFile)) {
                    sshPrivateKeyFile.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        testBtn.addActionListener(e -> {
            try {
                var connectInfo = validGetConnectInfo();
                if (RedisBasicService.service.ping(connectInfo)) {
                    AlertUtil.showInformationDialog("连接成功！");
                } else {
                    AlertUtil.showInformationDialog("连接失败！");
                }
            } catch (Exception exception) {
                AlertUtil.showErrorDialog("连接失败！", exception);
            }
        });
    }

    private void cancelActionPerformed(ActionEvent actionEvent) {
        dispose();
    }


    private void submitActionPerformed(ActionEvent actionEvent) {
        var connectInfo = validGetConnectInfo();
        ExecutorUtil.runAsync(() -> {
            SwingUtilities.invokeLater(LoadingUtil::showDialog);
            var redisMode = RedisBasicService.service.getRedisModeEnum(connectInfo);
            processHandler.processHandler(connectInfo.setRedisModeEnum(redisMode));
        });
        dispose();
    }

    private ConnectInfo validGetConnectInfo() {
        if (StringUtils.isEmpty(titleField.getText())) {
            titleField.requestFocus();
            throw new RedisFrontException("名称不能为空！", false);
        }
        if (StringUtils.isEmpty(hostField.getText())) {
            titleField.requestFocus();
            throw new RedisFrontException("主机不能为空", false);
        }

        //SSH Connection
        if (enableSSHBtn.isSelected()) {
            //valid sshHostField
            if (Fn.isEmpty(sshHostField.getText())) {
                sshHostField.requestFocus();
                throw new RedisFrontException("SSH主机不能为空", false);
            }
            //valid sshUserField
            if (Fn.isEmpty(sshUserField.getText())) {
                sshUserField.requestFocus();
                throw new RedisFrontException("SSH用户不能为空", false);
            }
            //valid enableSshPrivateKey
            if (enableSshPrivateKey.isSelected()) {
                if (Fn.isEmpty(sshPrivateKeyFile.getText())) {
                    sshPrivateKeyFile.requestFocus();
                    throw new RedisFrontException("SSH私钥不能为空", false);
                }
            }
            //sshConfig
            var sshConfig = new ConnectInfo.SSHConfig(
                    sshPrivateKeyFile.getText(),
                    sshUserField.getText(),
                    (Integer) sshPortField.getValue(),
                    new String(sshPasswordField.getPassword()));

            return new ConnectInfo(
                    titleField.getText(),
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
            var sshConfig = new ConnectInfo.SSLConfig(
                    privateKeyField.getText(),
                    publicKeyField.getText(),
                    grantField.getText(),
                    String.valueOf(sslPasswordField.getPassword())
            );
            return new ConnectInfo(titleField.getText(),
                    hostField.getText(),
                    (Integer) portField.getValue(),
                    userField.getText(),
                    String.valueOf(passwordField.getPassword()),
                    0,
                    enableSSLBtn.isSelected(),
                    Enum.Connect.NORMAL,
                    sshConfig)
                    .setId(id);

        } else {

            return new ConnectInfo(titleField.getText(),
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
        buttonOK.setText("确认");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("取消");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        testBtn.setText("测试连接");
        panel1.add(testBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        sslPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(sslPanel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("公钥");
        sslPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        publicKeyField = new JTextField();
        sslPanel.add(publicKeyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("私钥");
        sslPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        privateKeyField = new JTextField();
        sslPanel.add(privateKeyField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("授权");
        sslPanel.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        grantField = new JTextField();
        sslPanel.add(grantField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        publicKeyFileBtn = new JButton();
        publicKeyFileBtn.setText("选择文件");
        sslPanel.add(publicKeyFileBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        privateKeyFileBtn = new JButton();
        privateKeyFileBtn.setText("选择文件");
        sslPanel.add(privateKeyFileBtn, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        grantFileBtn = new JButton();
        grantFileBtn.setText("选择文件");
        sslPanel.add(grantFileBtn, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        sslPanel.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("密码");
        sslPanel.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sslPanel.add(sslPasswordField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        showSslPassword = new JCheckBox();
        showSslPassword.setText("显示密码");
        sslPanel.add(showSslPassword, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPanel.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(sshPanel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("主机");
        sshPanel.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshHostField = new JTextField();
        sshPanel.add(sshHostField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("用户");
        sshPanel.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshUserField = new JTextField();
        sshPanel.add(sshUserField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        enableSshPrivateKey = new JCheckBox();
        enableSshPrivateKey.setText("私钥");
        sshPanel.add(enableSshPrivateKey, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("端口");
        sshPanel.add(label7, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPrivateKeyFile = new JTextField();
        sshPanel.add(sshPrivateKeyFile, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sshPrivateKeyBtn = new JButton();
        sshPrivateKeyBtn.setText("选择文件");
        sshPanel.add(sshPrivateKeyBtn, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showShhPassword = new JCheckBox();
        showShhPassword.setText("显示密码");
        sshPanel.add(showShhPassword, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("密码");
        sshPanel.add(label8, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPanel.add(sshPortField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPasswordField = new JPasswordField();
        sshPanel.add(sshPasswordField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        basicPanel = new JPanel();
        basicPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(basicPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("名称");
        basicPanel.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleField = new JTextField();
        basicPanel.add(titleField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        hostLabel = new JLabel();
        hostLabel.setText("主机");
        basicPanel.add(hostLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordLabel = new JLabel();
        passwordLabel.setText("密码");
        basicPanel.add(passwordLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userLabel = new JLabel();
        userLabel.setText("用户");
        basicPanel.add(userLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(hostField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        userField = new JTextField();
        basicPanel.add(userField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        portLabel = new JLabel();
        portLabel.setText("端口");
        basicPanel.add(portLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(portField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showPasswordCheckBox = new JCheckBox();
        showPasswordCheckBox.setText("显示密码");
        basicPanel.add(showPasswordCheckBox, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
