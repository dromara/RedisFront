package com.redisfront.ui.dialog;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.constant.UI;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.handler.ProcessHandler;
import com.redisfront.commons.ui.AbstractDialog;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.FutureUtils;
import com.redisfront.commons.util.LoadingUtils;
import com.redisfront.commons.util.LocaleUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.ConnectTableModel;
import com.redisfront.service.ConnectService;
import com.redisfront.service.RedisBasicService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ResourceBundle;

public class OpenConnectDialog extends AbstractDialog<ConnectInfo> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable connectTable;
    private JButton addConnectBtn;
    private JScrollPane scrollPanel;

    protected ProcessHandler<ConnectInfo> openProcessHandler;

    protected ProcessHandler<ConnectInfo> editProcessHandler;

    protected ProcessHandler<ConnectInfo> delProcessHandler;

    public static void showOpenConnectDialog(ProcessHandler<ConnectInfo> openProcessHandler, ProcessHandler<ConnectInfo> editProcessHandler, ProcessHandler<ConnectInfo> delProcessHandler) {
        var openConnectDialog = new OpenConnectDialog(openProcessHandler, editProcessHandler, delProcessHandler);
        openConnectDialog.setSize(new Dimension(500, 280));
        openConnectDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        openConnectDialog.pack();
        openConnectDialog.setVisible(true);
    }

    public OpenConnectDialog(ProcessHandler<ConnectInfo> openProcessHandler, ProcessHandler<ConnectInfo> editProcessHandler, ProcessHandler<ConnectInfo> delProcessHandler) {
        super(RedisFrontApplication.frame);
        $$$setupUI$$$();
        setTitle(LocaleUtils.getMessageFromBundle("OpenConnectDialog.title"));
        setModal(true);
        setMinimumSize(new Dimension(500, 400));
        setResizable(false);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        buttonOK.setEnabled(false);
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        connectTableInit();
        this.openProcessHandler = openProcessHandler;
        this.editProcessHandler = editProcessHandler;
        this.delProcessHandler = delProcessHandler;
    }


    private void connectTableInit() {
        scrollPanel.setPreferredSize(new Dimension(500, 300));
        connectTable.setShowHorizontalLines(true);
        connectTable.setShowVerticalLines(true);
        connectTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        connectTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        var popupMenu = new JPopupMenu() {
            {
                //表格打开链接操作
                var openConnectMenu = new JMenuItem(LocaleUtils.getMessageFromBundle("OpenConnectDialog.openConnectMenu.title"));
                openConnectMenu.addActionListener(e -> onOK());
                add(openConnectMenu);
                //表格编辑链接操作
                var editConnectMenu = new JMenuItem(LocaleUtils.getMessageFromBundle("OpenConnectDialog.editConnectMenu.title"));
                editConnectMenu.addActionListener(e -> {
                    var row = connectTable.getSelectedRow();
                    if (row == -1) {
                        AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("OpenConnectDialog.editConnectMenu.noSelected.message"));
                        return;
                    }
                    var id = connectTable.getValueAt(row, 0);
                    var connectInfo = ConnectService.service.getConnect(id);
                    onCancel();
                    FutureUtils.runAsync(() -> editProcessHandler.processHandler(connectInfo));
                });
                add(editConnectMenu);
                //表格删除操作
                var deleteConnectMenu = new JMenuItem(LocaleUtils.getMessageFromBundle("OpenConnectDialog.deleteConnectMenu.title"));
                deleteConnectMenu.addActionListener(e -> {
                    int row = connectTable.getSelectedRow();
                    if (row == -1) {
                        AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("OpenConnectDialog.deleteConnectMenu.noSelected.message"));
                        return;
                    }
                    var id = connectTable.getValueAt(row, 0);
                    var connectInfo = ConnectService.service.getConnect(id);
                    if (Fn.isNotNull(connectInfo)) {
                        FutureUtils.runAsync(() -> {
                            delProcessHandler.processHandler(connectInfo);
                            ((ConnectTableModel) connectTable.getModel()).removeRow(row);
                            connectTable.revalidate();
                        });
                    }
                });
                add(deleteConnectMenu);
            }
        };

        connectTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    buttonOK.setEnabled(connectTable.getSelectedRow() != -1);
                }
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    onOK();
                }
                if (e.getButton() == MouseEvent.BUTTON3 && connectTable.getSelectedRow() != -1) {
                    popupMenu.show(connectTable, e.getX(), e.getY());
                    popupMenu.setVisible(true);
                    popupMenu.pack();
                }
            }
        });

        //查询数据连接列表
        var connectInfoList = ConnectService.service.getAllConnectList();
        connectTable.setModel(new ConnectTableModel(connectInfoList));
    }

    private void onOK() {
        var row = connectTable.getSelectedRow();
        if (row == -1) {
            AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("OpenConnectDialog.editConnectMenu.noSelected.message"));
            return;
        }
        var id = connectTable.getValueAt(row, 0);
        var connectInfo = ConnectService.service.getConnect(id);
        FutureUtils.runAsync(() -> {
            LoadingUtils.showDialog(String.format(LocaleUtils.getMessageFromBundle("OpenConnectDialog.openConnection.message"), connectInfo.host(), connectInfo.port()));
            var redisMode = RedisBasicService.service.getRedisModeEnum(connectInfo);
            openProcessHandler.processHandler(connectInfo.setRedisModeEnum(redisMode));
        });
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        addConnectBtn = new JButton();
        addConnectBtn.setFocusable(false);
        addConnectBtn.addActionListener(e -> {
            dispose();
            AddConnectDialog.showAddConnectDialog(openProcessHandler);
        });
        addConnectBtn.setIcon(UI.CONNECTION_ICON);
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
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        this.$$$loadButtonText$$$(buttonOK, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "OpenConnectDialog.buttonOK.title"));
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "OpenConnectDialog.buttonCancel.title"));
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        contentPane.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scrollPanel = new JScrollPane();
        panel3.add(scrollPanel, BorderLayout.CENTER);
        connectTable = new JTable();
        connectTable.setAutoResizeMode(1);
        scrollPanel.setViewportView(connectTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new BorderLayout(0, 0));
        contentPane.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        this.$$$loadButtonText$$$(addConnectBtn, this.$$$getMessageFromBundle$$$("com/redisfront/RedisFront", "OpenConnectDialog.addConnectBtn.title"));
        panel4.add(addConnectBtn, BorderLayout.WEST);
        final Spacer spacer2 = new Spacer();
        panel4.add(spacer2, BorderLayout.CENTER);
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
