package cn.devcms.redisfront.ui.dialog;

import cn.devcms.redisfront.common.base.AbstractDialog;
import cn.devcms.redisfront.common.enums.ConnectEnum;
import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.model.ConnectInfo;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

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
    public static void showAddConnectDialog(Frame owner, Consumer<ConnectInfo> callback) {
        var addConnectDialog = new AddConnectDialog(owner, callback);
        addConnectDialog.setResizable(false);
        addConnectDialog.setLocationRelativeTo(owner);
        addConnectDialog.pack();
        addConnectDialog.setVisible(true);
    }

    /**
     * 打开编辑连接窗口
     *
     */
    public static void showEditConnectDialog(Frame owner, ConnectInfo connectInfo, Consumer<ConnectInfo> callback) {
        var addConnectDialog = new AddConnectDialog(owner, callback);
        //数据初始化
        addConnectDialog.componentsDataInit(connectInfo);
        addConnectDialog.setResizable(false);
        addConnectDialog.setLocationRelativeTo(owner);
        addConnectDialog.pack();
        addConnectDialog.setVisible(true);
    }

    public static void updateTableData() {

    }


    public AddConnectDialog(Frame owner, Consumer<ConnectInfo> callback) {
        super(owner, callback);

        setTitle("新建连接");
        setModal(true);
        setResizable(false);
        setMinimumSize(new Dimension(400, 260));
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.id = 0;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        initComponentListener();
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
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });

        enableSSLBtn.addActionListener(e -> {
            enableSSHBtn.setSelected(false);
            sshPanel.setVisible(false);
            sslPanel.setVisible(enableSSLBtn.isSelected());
            if (enableSSLBtn.isSelected()) {
                setSize(new Dimension(400, 400));
            } else {
                setSize(new Dimension(400, 280));
            }
        });

        enableSSHBtn.addActionListener(e -> {
            enableSSLBtn.setSelected(false);
            sslPanel.setVisible(false);
            sshPanel.setVisible(enableSSHBtn.isSelected());
            if (enableSSHBtn.isSelected()) {
                setSize(new Dimension(400, 420));
            } else {
                setSize(new Dimension(400, 280));
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
    }

    private void cancelActionPerformed(ActionEvent actionEvent) {
        dispose();
    }


    private void submitActionPerformed(ActionEvent actionEvent) {

        if (StringUtils.isEmpty(titleField.getText())) {
            titleField.requestFocus();
            return;
        }
        if (StringUtils.isEmpty(hostField.getText())) {
            titleField.requestFocus();
            return;
        }

        //SSH Connection
        if (enableSSHBtn.isSelected()) {
            //valid sshHostField
            if (Fn.isEmpty(sshHostField.getText())) {
                sshHostField.requestFocus();
                return;
            }
            //valid sshUserField
            if (Fn.isEmpty(sshUserField.getText())) {
                sshUserField.requestFocus();
                return;
            }
            //valid enableSshPrivateKey
            if (enableSshPrivateKey.isSelected()) {
                if (Fn.isEmpty(sshPrivateKeyFile.getText())) {
                    sshPrivateKeyFile.requestFocus();
                    return;
                }
            }
            //sshConfig
            var sshConfig = new ConnectInfo.SSHConfig(
                    sshPrivateKeyFile.getText(),
                    sshUserField.getText(),
                    (Integer) sshPortField.getValue(),
                    new String(sshPasswordField.getPassword()));

            callback.accept(
                    new ConnectInfo(
                            titleField.getText(),
                            hostField.getText(),
                            (Integer) portField.getValue(),
                            userField.getText(),
                            String.valueOf(passwordField.getPassword()),
                            0,
                            enableSSLBtn.isSelected(),
                            ConnectEnum.SSH,
                            sshConfig)
                            .setId(id)
            );

        } else if (enableSSLBtn.isSelected()) {
            var sshConfig = new ConnectInfo.SSLConfig(
                    privateKeyField.getText(),
                    publicKeyField.getText(),
                    grantField.getText(),
                    String.valueOf(sslPasswordField.getPassword())
            );
            callback.accept(
                    new ConnectInfo(titleField.getText(),
                            hostField.getText(),
                            (Integer) portField.getValue(),
                            userField.getText(),
                            String.valueOf(passwordField.getPassword()),
                            0,
                            enableSSLBtn.isSelected(),
                            ConnectEnum.NORMAL,
                            sshConfig)
                            .setId(id)
            );
        } else {
            callback.accept(
                    new ConnectInfo(titleField.getText(),
                            hostField.getText(),
                            (Integer) portField.getValue(),
                            userField.getText(),
                            String.valueOf(passwordField.getPassword()),
                            0,
                            enableSSLBtn.isSelected(),
                            ConnectEnum.NORMAL)
                            .setId(id)
            );
        }
        dispose();
    }

    /**
     * 数据初始化
     *
     * @param connectInfo
     */
    public void componentsDataInit(ConnectInfo connectInfo) {
        this.id = connectInfo.id();
        this.titleField.setText(connectInfo.title());
        this.hostField.setText(connectInfo.host());
        this.portField.setValue(connectInfo.port());
        this.userField.setText(connectInfo.user());
        this.passwordField.setText(connectInfo.password());
        this.enableSSLBtn.setSelected(connectInfo.ssl());
        this.enableSSHBtn.setSelected(ConnectEnum.SSH.equals(connectInfo.connectMode()));
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
        testBtn.setIcon(new FlatSVGIcon("icons/lan-connect.svg"));
        sshPortField = new JSpinner();
        sshPortField.setValue(22);
    }
}
