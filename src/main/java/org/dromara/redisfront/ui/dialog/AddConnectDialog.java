package org.dromara.redisfront.ui.dialog;

import cn.hutool.extra.ssh.JschRuntimeException;
import com.formdev.flatlaf.util.StringUtils;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import io.lettuce.core.RedisConnectionException;
import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.dao.ConnectDetailDao;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.event.OpenRedisConnectEvent;
import org.dromara.redisfront.ui.event.RefreshConnectTreeEvent;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.dromara.redisfront.ui.widget.sidebar.tree.RedisConnectTreeNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddConnectDialog extends QSDialog<RedisFrontWidget> {
    private static final Logger log = LoggerFactory.getLogger(AddConnectDialog.class);
    private final RedisFrontContext context;
    private Integer detailId;
    private Integer groupId;
    private JPanel contentPane;
    private JButton openBtn;
    private JButton storageBtn;
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
    private JTabbedPane tabbedPane1;
    private JPanel redisPanel;
    private JTextField keySeparatorField;
    private JTextField sshTimeoutTextField;
    private JTextField redisTimeoutTextField;
    private JTextField keyMaxLoadNum;
    private JLabel keySeparatorLabel;
    private JLabel loadNumLabel;
    private JLabel redisTimeoutLabel;
    private JLabel sshTimeoutLabel;


    public static AddConnectDialog getInstance(RedisFrontWidget app) {
        return new AddConnectDialog(app);
    }

    public void showNewConnectDialog(RedisConnectTreeNode node) {
        if (null != node) {
            this.groupId = node.id();
            this.setTitle(this.getTitle() + "到【" + node + "】");
        } else {
            this.groupId = -1;
        }
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.pack();
    }

    public void showEditConnectDialog(RedisConnectTreeNode redisConnectTreeNode) {
        ConnectDetailEntity detail = redisConnectTreeNode.getDetail();
        if (null != detail.getGroupId()) {
            this.groupId = detail.getGroupId();
        } else {
            this.groupId = -1;
        }
        this.detailId = detail.getId();
        this.setTitle("编辑连接 【" + detail.getName() + "】");
        this.populateConnectInfo(detail.getConnectContext());
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.pack();
    }

    private AddConnectDialog(RedisFrontWidget app) {
        super(app, app.$tr("AddConnectDialog.Title"));
        this.context = (RedisFrontContext) app.getContext();
        $$$setupUI$$$();
        this.setModal(true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(400, 350));
        this.setContentPane(contentPane);
        this.getRootPane().setDefaultButton(openBtn);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        this.initializeComponents();
    }

    private void initializeComponents() {
        this.contentPane.registerKeyboardAction(_ -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.sshPrivateKeyFile.setVisible(enableSshPrivateKey.isSelected());
        this.sshPrivateKeyBtn.setVisible(enableSshPrivateKey.isSelected());

        openBtn.addActionListener(this::openActionPerformed);
        storageBtn.addActionListener(this::storageActionPerformed);

        showPasswordCheckBox.addActionListener(_ -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }
        });

        showShhPassword.addActionListener(_ -> {
            if (showShhPassword.isSelected()) {
                sshPasswordField.setEchoChar((char) 0);
            } else {
                sshPasswordField.setEchoChar('*');
            }
        });

        showSslPassword.addActionListener(_ -> {
            if (showPasswordCheckBox.isSelected()) {
                sslPasswordField.setEchoChar((char) 0);
            } else {
                sslPasswordField.setEchoChar('*');
            }
        });

        enableSshPrivateKey.addActionListener(_ -> {
            if (enableSshPrivateKey.isSelected()) {
                setSize(new Dimension(getWidth(), getHeight() + 20));
            } else {
                setSize(new Dimension(getWidth(), getHeight() - 20));
            }
            sshPrivateKeyFile.setVisible(enableSshPrivateKey.isSelected());
            sshPrivateKeyBtn.setVisible(enableSshPrivateKey.isSelected());
        });

        enableSSLBtn.addActionListener(_ -> {
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

        enableSSHBtn.addActionListener(_ -> {
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
                if (RedisFrontUtils.isNotNull(selectedFile)) {
                    publicKeyField.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        sshPrivateKeyBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter($tr("AddConnectDialog.FileChooser.sshPrivateKey.title"), "pem", "crt"));
                fileChooser.showDialog(AddConnectDialog.this, $tr("AddConnectDialog.FileChooser.sshPrivateKey.btn"));
                var selectedFile = fileChooser.getSelectedFile();
                if (RedisFrontUtils.isNotNull(selectedFile)) {
                    sshPrivateKeyFile.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        testBtn.addActionListener(_ -> testConnect());
    }

    private Boolean testConnect() {
        var connectSuccess = false;
        try {
            var connectInfo = validGetConnectInfo();
            if (RedisBasicService.service.ping(connectInfo)) {
                getOwner().displayMessage($tr("AddConnectDialog.test.success.title"), $tr("AddConnectDialog.test.success.message"));
                connectSuccess = true;
            } else {
                getOwner().displayMessage($tr("AddConnectDialog.test.fail.title"), $tr("AddConnectDialog.test.fail.message"));
            }
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            if (exception instanceof RedisFrontException) {
                if (exception.getCause() instanceof JschRuntimeException jschRuntimeException) {
                    getOwner().displayException($tr("AddConnectDialog.test.fail.message"), jschRuntimeException.getCause());
                } else {
                    getOwner().displayException($tr("AddConnectDialog.test.fail.message"), exception);
                }
            } else if (exception instanceof RedisConnectionException) {
                getOwner().displayException($tr("AddConnectDialog.test.fail.message"), exception);
            } else {
                getOwner().displayException($tr("AddConnectDialog.test.fail.message"), exception);
            }
        }
        return connectSuccess;
    }

    private void storageActionPerformed(ActionEvent actionEvent) {
        var connectContext = validGetConnectInfo();
        if (testConnect()) {
            try {
                ConnectDetailEntity connectDetailEntity = connectContext.toEntity();
                connectDetailEntity.setGroupId(groupId);
                if (null == detailId) {
                    ConnectDetailDao.newInstance(this.context.getDatabaseManager().getDatasource()).save(connectDetailEntity);
                } else {
                    ConnectDetailDao.newInstance(this.context.getDatabaseManager().getDatasource()).update(detailId, connectDetailEntity);
                }
                this.context.getEventBus().publish(new RefreshConnectTreeEvent(connectContext));
                dispose();
            } catch (SQLException e) {
                getOwner().displayException($tr("AddConnectDialog.save.fail.message"), e);
            }
        }
    }


    private void openActionPerformed(ActionEvent actionEvent) {
        var redisConnectContext = validGetConnectInfo();
        if (testConnect()) {
            try {
                ConnectDetailEntity connectDetailEntity = redisConnectContext.toEntity();
                connectDetailEntity.setGroupId(groupId);
                if (null == detailId) {
                    ConnectDetailDao.newInstance(this.context.getDatabaseManager().getDatasource()).save(connectDetailEntity);
                    redisConnectContext.setId(redisConnectContext.getId());
                } else {
                    ConnectDetailDao.newInstance(this.context.getDatabaseManager().getDatasource()).update(detailId, connectDetailEntity);
                    redisConnectContext.setId(detailId);
                }
                this.context.getEventBus().publish(new OpenRedisConnectEvent(redisConnectContext));
                this.context.getEventBus().publish(new RefreshConnectTreeEvent(redisConnectContext));
                dispose();
            } catch (SQLException e) {
                getOwner().displayException($tr("AddConnectDialog.save.fail.message"), e);
            }
        }
    }

    private RedisConnectContext validGetConnectInfo() {
        if (StringUtils.isEmpty(titleField.getText())) {
            titleField.requestFocus();
            throw new RedisFrontException($tr("AddConnectDialog.require.title.message"), false);
        }
        if (StringUtils.isEmpty(hostField.getText())) {
            titleField.requestFocus();
            throw new RedisFrontException($tr("AddConnectDialog.require.host.message"), false);
        }

        var title = titleField.getText();

        if (enableSSHBtn.isSelected()) {
            if (RedisFrontUtils.isEmpty(sshHostField.getText())) {
                sshHostField.requestFocus();
                throw new RedisFrontException($tr("AddConnectDialog.require.sshHost.message"), false);
            }
            if (RedisFrontUtils.isEmpty(sshUserField.getText())) {
                sshUserField.requestFocus();
                throw new RedisFrontException($tr("AddConnectDialog.require.sshUser.message"), false);
            }
            if (enableSshPrivateKey.isSelected()) {
                if (RedisFrontUtils.isEmpty(sshPrivateKeyFile.getText())) {
                    sshPrivateKeyFile.requestFocus();
                    throw new RedisFrontException($tr("AddConnectDialog.require.sshPrivateKey.message"), false);
                }
            }
            return getSshConnectInfo(title);
        } else if (enableSSLBtn.isSelected()) {
            return getSslConnectInfo(title);
        } else {
            return getDefaultConnectInfo(title);
        }
    }

    private @NotNull RedisConnectContext getDefaultConnectInfo(String title) {
        RedisConnectContext redisConnectContext = new RedisConnectContext();
        redisConnectContext.setTitle(title);
        redisConnectContext.setHost(hostField.getText());
        redisConnectContext.setPort((Integer) portField.getValue());
        redisConnectContext.setUsername(userField.getText());
        redisConnectContext.setPassword(String.valueOf(passwordField.getPassword()));
        redisConnectContext.setConnectTypeMode(ConnectType.NORMAL);
        RedisConnectContext.SettingInfo settingInfo = getSettingInfo();
        redisConnectContext.setSetting(settingInfo);
        return redisConnectContext;
    }

    private RedisConnectContext.@NotNull SettingInfo getSettingInfo() {
        return new RedisConnectContext.SettingInfo(
                Integer.parseInt(keyMaxLoadNum.getText()),
                keySeparatorField.getText(),
                Integer.parseInt(redisTimeoutTextField.getText()),
                Integer.parseInt(sshTimeoutTextField.getText())
        );
    }

    private @NotNull RedisConnectContext getSslConnectInfo(String title) {
        var sslInfo = new RedisConnectContext.SslInfo(
                null,
                publicKeyField.getText(),
                null,
                String.valueOf(sslPasswordField.getPassword())
        );
        RedisConnectContext redisConnectContext = new RedisConnectContext();
        redisConnectContext.setTitle(title);
        redisConnectContext.setHost(hostField.getText());
        redisConnectContext.setPort((Integer) portField.getValue());
        redisConnectContext.setUsername(userField.getText());
        redisConnectContext.setPassword(String.valueOf(passwordField.getPassword()));
        redisConnectContext.setSslInfo(sslInfo);
        redisConnectContext.setEnableSsl(enableSSLBtn.isSelected());
        redisConnectContext.setConnectTypeMode(ConnectType.NORMAL);
        RedisConnectContext.SettingInfo settingInfo = getSettingInfo();
        redisConnectContext.setSetting(settingInfo);
        return redisConnectContext;
    }

    private @NotNull RedisConnectContext getSshConnectInfo(String title) {
        var sshInfo = new RedisConnectContext.SshInfo(
                sshPrivateKeyFile.getText(),
                sshUserField.getText(),
                sshHostField.getText(),
                (Integer) sshPortField.getValue(),
                new String(sshPasswordField.getPassword()));

        RedisConnectContext redisConnectContext = new RedisConnectContext();
        redisConnectContext.setTitle(title);
        redisConnectContext.setHost(hostField.getText());
        redisConnectContext.setPort((Integer) portField.getValue());
        redisConnectContext.setUsername(userField.getText());
        redisConnectContext.setPassword(String.valueOf(passwordField.getPassword()));
        redisConnectContext.setSshInfo(sshInfo);
        redisConnectContext.setEnableSsl(enableSSLBtn.isSelected());
        redisConnectContext.setConnectTypeMode(ConnectType.SSH);
        RedisConnectContext.SettingInfo settingInfo = getSettingInfo();
        redisConnectContext.setSetting(settingInfo);
        return redisConnectContext;
    }

    private void populateConnectInfo(RedisConnectContext redisConnectContext) {
        this.titleField.setText(redisConnectContext.getTitle());
        this.hostField.setText(redisConnectContext.getHost());
        this.portField.setValue(redisConnectContext.getPort());
        this.userField.setText(redisConnectContext.getUsername());
        this.passwordField.setText(redisConnectContext.getPassword());
        this.enableSSLBtn.setSelected(redisConnectContext.getEnableSsl());
        this.enableSSHBtn.setSelected(ConnectType.SSH.equals(redisConnectContext.getConnectTypeMode()));
        if (enableSSLBtn.isSelected()) {
            setSize(new Dimension(getWidth(), getHeight() - 130));
            sslPanel.setVisible(true);
            sslPasswordField.setText(redisConnectContext.getSslInfo().getPassword());
            publicKeyField.setText(redisConnectContext.getSslInfo().getPublicKeyFilePath());
        }
        if (enableSSHBtn.isSelected()) {
            setSize(new Dimension(getWidth(), getHeight() + 140));
            sshPanel.setVisible(true);
            sshUserField.setText(redisConnectContext.getSshInfo().getUser());
            sshHostField.setText(redisConnectContext.getSshInfo().getHost());
            sshPasswordField.setText(redisConnectContext.getSshInfo().getPassword());
            sshPortField.setValue(redisConnectContext.getSshInfo().getPort());
            enableSshPrivateKey.setSelected(RedisFrontUtils.isNotEmpty(redisConnectContext.getSshInfo().getPrivateKeyPath()));
            sshPrivateKeyFile.setVisible(enableSshPrivateKey.isSelected());
            sshPrivateKeyBtn.setVisible(enableSshPrivateKey.isSelected());
            sshPrivateKeyFile.setText(redisConnectContext.getSshInfo().getPrivateKeyPath());
        }
        if (RedisFrontUtils.isNotEmpty(redisConnectContext.getSetting())) {
            keyMaxLoadNum.setText(String.valueOf(redisConnectContext.getSetting().getLoadKeyNum()));
            keySeparatorField.setText(redisConnectContext.getSetting().getKeySeparator());
            redisTimeoutTextField.setText(String.valueOf(redisConnectContext.getSetting().getRedisTimeout()));
            sshTimeoutTextField.setText(String.valueOf(redisConnectContext.getSetting().getSshTimeout()));
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
        contentPane.setLayout(new BorderLayout(0, 0));
        final JSeparator separator1 = new JSeparator();
        contentPane.add(separator1, BorderLayout.NORTH);
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setTabPlacement(2);
        contentPane.add(tabbedPane1, BorderLayout.CENTER);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(10, 20, 10, 20), -1, -1));
        tabbedPane1.addTab("连接", panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        sslPanel.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(sslPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("证书");
        sslPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        publicKeyField = new JTextField();
        sslPanel.add(publicKeyField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        publicKeyFileBtn = new JButton();
        publicKeyFileBtn.setText("选择文件");
        sslPanel.add(publicKeyFileBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        sslPanel.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("密码");
        sslPanel.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sslPanel.add(sslPasswordField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        showSslPassword = new JCheckBox();
        showSslPassword.setText("显示密码");
        sslPanel.add(showSslPassword, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPanel.setLayout(new GridLayoutManager(5, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(sshPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        sshHostLabel = new JLabel();
        this.$$$loadLabelText$$$(sshHostLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.sshHostLabel.Title"));
        sshPanel.add(sshHostLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshHostField = new JTextField();
        sshPanel.add(sshHostField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sshUserLabel = new JLabel();
        this.$$$loadLabelText$$$(sshUserLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.sshUserLabel.Title"));
        sshPanel.add(sshUserLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshUserField = new JTextField();
        sshPanel.add(sshUserField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        enableSshPrivateKey = new JCheckBox();
        this.$$$loadButtonText$$$(enableSshPrivateKey, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.enableSshPrivateKey.Title"));
        sshPanel.add(enableSshPrivateKey, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPortLabel = new JLabel();
        this.$$$loadLabelText$$$(sshPortLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.sshPortLabel.Title"));
        sshPanel.add(sshPortLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPrivateKeyFile = new JTextField();
        sshPanel.add(sshPrivateKeyFile, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sshPrivateKeyBtn = new JButton();
        this.$$$loadButtonText$$$(sshPrivateKeyBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.sshPrivateKeyBtn.Title"));
        sshPanel.add(sshPrivateKeyBtn, new GridConstraints(3, 2, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showShhPassword = new JCheckBox();
        this.$$$loadButtonText$$$(showShhPassword, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.showShhPassword.Title"));
        sshPanel.add(showShhPassword, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPasswordLabel = new JLabel();
        this.$$$loadLabelText$$$(sshPasswordLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.sshPasswordLabel.Title"));
        sshPanel.add(sshPasswordLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPanel.add(sshPortField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshPasswordField = new JPasswordField();
        sshPanel.add(sshPasswordField, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        basicPanel = new JPanel();
        basicPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(basicPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        titleLabel = new JLabel();
        this.$$$loadLabelText$$$(titleLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.titleLabel.Title"));
        titleLabel.setToolTipText(this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.titleLabel.Desc"));
        basicPanel.add(titleLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        titleField = new JTextField();
        basicPanel.add(titleField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        hostLabel = new JLabel();
        this.$$$loadLabelText$$$(hostLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.hostLabel.Title"));
        basicPanel.add(hostLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordLabel = new JLabel();
        this.$$$loadLabelText$$$(passwordLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.passwordLabel.Title"));
        basicPanel.add(passwordLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        userLabel = new JLabel();
        this.$$$loadLabelText$$$(userLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.userLabel.Title"));
        basicPanel.add(userLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(hostField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        userField = new JTextField();
        basicPanel.add(userField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        portLabel = new JLabel();
        this.$$$loadLabelText$$$(portLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.portLabel.Title"));
        basicPanel.add(portLabel, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        basicPanel.add(portField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showPasswordCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(showPasswordCheckBox, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.showPasswordCheckBox.Title"));
        basicPanel.add(showPasswordCheckBox, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enableSSLBtn.setEnabled(true);
        enableSSLBtn.setText("SSL/TLS");
        basicPanel.add(enableSSLBtn, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        basicPanel.add(spacer2, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        basicPanel.add(passwordField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        enableSSHBtn.setText("SSH");
        basicPanel.add(enableSSHBtn, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(10, 20, 10, 20), -1, -1));
        tabbedPane1.addTab("设置", panel3);
        redisPanel = new JPanel();
        redisPanel.setLayout(new GridLayoutManager(9, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(redisPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        keySeparatorField = new JTextField();
        keySeparatorField.setText(".");
        redisPanel.add(keySeparatorField, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        sshTimeoutTextField = new JTextField();
        sshTimeoutTextField.setText("1000");
        redisPanel.add(sshTimeoutTextField, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        redisPanel.add(spacer3, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        redisTimeoutTextField = new JTextField();
        redisTimeoutTextField.setText("1000");
        redisPanel.add(redisTimeoutTextField, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        keyMaxLoadNum = new JTextField();
        keyMaxLoadNum.setText("5000");
        redisPanel.add(keyMaxLoadNum, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        keySeparatorLabel = new JLabel();
        keySeparatorLabel.setText("分隔符");
        redisPanel.add(keySeparatorLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadNumLabel = new JLabel();
        loadNumLabel.setText("加载数");
        redisPanel.add(loadNumLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        redisTimeoutLabel = new JLabel();
        this.$$$loadLabelText$$$(redisTimeoutLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "SettingDialog.redisTimeoutLabel.Title"));
        redisPanel.add(redisTimeoutLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sshTimeoutLabel = new JLabel();
        this.$$$loadLabelText$$$(sshTimeoutLabel, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "SettingDialog.sshTimeoutLabel.Title"));
        redisPanel.add(sshTimeoutLabel, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        panel4.setVisible(true);
        contentPane.add(panel4, BorderLayout.SOUTH);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 1, new Insets(5, 10, 5, 10), -1, -1));
        panel5.setVisible(true);
        panel4.add(panel5, BorderLayout.CENTER);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        openBtn = new JButton();
        this.$$$loadButtonText$$$(openBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.buttonOK.Title"));
        panel6.add(openBtn, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        storageBtn = new JButton();
        this.$$$loadButtonText$$$(storageBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.storageBtn.Title"));
        panel6.add(storageBtn, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        this.$$$loadButtonText$$$(testBtn, this.$$$getMessageFromBundle$$$("org/dromara/redisfront/RedisFront", "AddConnectDialog.testBtn.Title"));
        panel6.add(testBtn, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel6.add(spacer4, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel5.add(spacer5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        panel5.add(separator2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JSeparator separator3 = new JSeparator();
        panel4.add(separator3, BorderLayout.NORTH);
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
