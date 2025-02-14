package org.dromara.redisfront.ui.dialog;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.dromara.redisfront.RedisFrontMain;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.resources.AbstractDialog;
import org.dromara.redisfront.commons.utils.AlertUtils;
import org.dromara.redisfront.commons.utils.LocaleUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

public class ImportConfigDialog extends AbstractDialog<Void> {

    private JComboBox<String> importModel;

    private JPanel contentPane;

    private JButton buttonOK;

    private JButton buttonCancel;

    private JPanel modelPanel;

    private JLabel modelLabel;

    public static void showImportDialog() {
        var importConfigDialog = new ImportConfigDialog(RedisFrontMain.frame);
        importConfigDialog.setMinimumSize(new Dimension(400, 200));
        importConfigDialog.setLocationRelativeTo(RedisFrontMain.frame);
        importConfigDialog.pack();
        importConfigDialog.setVisible(true);
    }

    private void initImportModel() {
        importModel.addItem("导入RedisFront配置");
        importModel.addItem("导入RDM配置");
        importModel.setSelectedItem("导入RedisFront配置");
    }

    public ImportConfigDialog(Frame owner) {
        super(owner);
        $$$setupUI$$$();
        initImportModel();
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOk());
        buttonCancel.addActionListener(e -> onCancel());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void onOk() {
        if (!FileUtil.exist(Constants.CONFIG_DATA_PATH)) {
            FileUtil.mkdir(Constants.CONFIG_DATA_PATH);
        }
        var selectedItem = (String) importModel.getSelectedItem();
        // 创建一个默认的文件选取器
        var fileChooser = new JFileChooser(Constants.CONFIG_DATA_PATH);
        // 仅支持选择文件
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 设置文件过滤，仅支持json配置导入
        fileChooser.setFileFilter(new FileNameExtensionFilter(".json", "json"));
        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        var result = fileChooser.showDialog(RedisFrontMain.frame, LocaleUtils.getMessageFromBundle("ConfigImport.saveBtn.title"));
        if (result == JFileChooser.APPROVE_OPTION) {
            var configFile = fileChooser.getSelectedFile();
            var fileReader = new FileReader(configFile);
            if (!StrUtil.isBlankIfStr(fileReader.readString()) && JSONUtil.isTypeJSON(fileReader.readString())) {
                if ("导入RedisFront配置".equals(selectedItem)) {
                    try {
                        if (JSONUtil.isTypeJSONObject(fileReader.readString())) {
                            var connectInfo = JSONUtil.toBean(fileReader.readString(), RedisConnectContext.class);
//                            ConnectDetailDao.DAO.save(connectInfo);
                        } else {
                            var connectInfos = JSONUtil.toList(fileReader.readString(), RedisConnectContext.class);
                            for (RedisConnectContext redisConnectContext : connectInfos) {
//                                ConnectDetailDao.DAO.save(connectInfo);
                            }
                        }
                        AlertUtils.showInformationDialog("导入完成");
                    } catch (IORuntimeException e) {
                        AlertUtils.showErrorDialog("配置读取异常", e);
                    } catch (JSONException je) {
                        AlertUtils.showErrorDialog("配置文件转换出现异常", je);
                    }
                } else if ("导入RDM配置".equals(selectedItem)) {
                    try {
                        if (JSONUtil.isTypeJSONArray(fileReader.readString())) {
                            var array = JSONUtil.parseArray(fileReader.readString());
                            for (Object o : array) {
                                var data = (JSONObject) o;
                                if (RedisFrontUtils.isNotNull(data.getRaw().get("type"))) {
                                    // 如果类型存在，那就是分组的redis配置
                                    var connections = JSONUtil.parseArray(JSONUtil.toJsonStr(data.get("connections")));
                                    for (Object connection : connections) {
                                        var groupConnection = (JSONObject) connection;
                                        var connectInfo = genConnectInfo(groupConnection.getRaw());
//                                        ConnectDetailDao.DAO.save(connectInfo);
                                    }
                                } else {
                                    var connectInfo = genConnectInfo(data.getRaw());
//                                    ConnectDetailDao.DAO.save(connectInfo);
                                }
                            }
                        }
                    } catch (IORuntimeException e) {
                        AlertUtils.showInformationDialog("RDM配置读取异常");
                        throw new RuntimeException(e);
                    }
                }
            } else {
                AlertUtils.showInformationDialog("配置无法正常读取");
            }
        }
        dispose();
    }

    private RedisConnectContext genConnectInfo(Map<String, Object> raw) {
        var connectInfo = new RedisConnectContext();
        if (RedisFrontUtils.isNotNull(raw.get("host"))) {
            connectInfo.setHost((String) raw.get("host"));
        }
        if (RedisFrontUtils.isNotNull(raw.get("port"))) {
            connectInfo.setPort((Integer) raw.get("port"));
        }
        if (RedisFrontUtils.isNotNull(raw.get("name"))) {
            connectInfo.setTitle((String) raw.get("name"));
        }
        if (RedisFrontUtils.isNotNull(raw.get("auth"))) {
            connectInfo.setPassword((String) raw.get("auth"));
        }
        if (RedisFrontUtils.isNotNull(raw.get("username"))) {
            connectInfo.setUsername((String) raw.get("username"));
        }
        connectInfo.setConnectTypeMode(ConnectType.NORMAL);
        connectInfo.setSshInfo(new RedisConnectContext.SshInfo("", "", "", null, ""));
        if (!StrUtil.isBlankIfStr(raw.get("ssh_host"))) {
            connectInfo.getSshInfo().setHost((String) raw.get("ssh_host"));
            connectInfo.setConnectTypeMode(ConnectType.SSH);
        }
        if (RedisFrontUtils.isNotNull(raw.get("ssh_port"))) {
            connectInfo.getSshInfo().setPort((Integer) raw.get("ssh_port"));
            ///connectInfo.setConnectMode(Enum.Connect.SSH);
        }
        if (!StrUtil.isBlankIfStr(raw.get("ssh_user"))) {
            connectInfo.getSshInfo().setUser((String) raw.get("ssh_user"));
            connectInfo.setConnectTypeMode(ConnectType.SSH);
        }
        if (!StrUtil.isBlankIfStr(raw.get("ssh_password"))) {
            connectInfo.getSshInfo().setPassword((String) raw.get("ssh_password"));
            connectInfo.setConnectTypeMode(ConnectType.SSH);
        }
        if (!StrUtil.isBlankIfStr(raw.get("ssh_private_key_path"))) {
            connectInfo.getSshInfo().setPrivateKeyPath((String) raw.get("ssh_private_key_path"));
            connectInfo.setConnectTypeMode(ConnectType.SSH);
        }
        return connectInfo;
    }

    private void onCancel() {
        dispose();
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
        modelPanel = new JPanel();
        modelPanel.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(modelPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modelLabel = new JLabel();
        modelLabel.setText("导入自:");
        modelPanel.add(modelLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        importModel = new JComboBox();
        modelPanel.add(importModel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(380, -1), null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        modelPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        modelPanel.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("取消");
        panel1.add(buttonCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("确定");
        panel1.add(buttonOK, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
