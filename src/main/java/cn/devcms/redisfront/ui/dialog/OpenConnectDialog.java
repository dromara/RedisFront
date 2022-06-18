package cn.devcms.redisfront.ui.dialog;

import cn.devcms.redisfront.common.base.AbstractDialog;
import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.common.util.MsgUtil;
import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.service.ConnectService;
import cn.devcms.redisfront.ui.component.ConnectTableModelComponent;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Consumer;

public class OpenConnectDialog extends AbstractDialog<ConnectInfo> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable connectTable;
    private JButton addConnectBtn;

    protected Consumer<ConnectInfo> openActionCallback;

    protected Consumer<ConnectInfo> editActionCallback;

    protected Consumer<ConnectInfo> delActionCallback;

    public static void showOpenConnectDialog(Frame owner, Consumer<ConnectInfo> openActionCallback, Consumer<ConnectInfo> editActionCallback, Consumer<ConnectInfo> delActionCallback) {
        var openConnectDialog = new OpenConnectDialog(owner, openActionCallback, editActionCallback, delActionCallback);
        openConnectDialog.setSize(new Dimension(500, 280));
        openConnectDialog.setLocationRelativeTo(owner);
        openConnectDialog.pack();
        openConnectDialog.setVisible(true);
    }

    public OpenConnectDialog(Frame owner, Consumer<ConnectInfo> openActionCallback, Consumer<ConnectInfo> editActionCallback, Consumer<ConnectInfo> delActionCallback) {
        super(owner);
        setTitle("打开连接");
        setModal(true);
        setMinimumSize(new Dimension(400, 500));
        setContentPane(contentPane);
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
        connectTableInit();
        this.openActionCallback = openActionCallback;
        this.editActionCallback = editActionCallback;
        this.delActionCallback = delActionCallback;
    }


    private void connectTableInit() {
        connectTable.setShowHorizontalLines(true);
        connectTable.setShowVerticalLines(true);
        connectTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        connectTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectTable.setComponentPopupMenu(new JPopupMenu() {
            {
                //表格打开链接操作
                var openConnectMenu = new JMenuItem("打开链接");
                openConnectMenu.addActionListener(e -> {
                    var row = connectTable.getSelectedRow();
                    if (row == -1) {
                        MsgUtil.showInformationDialog(this, "未选中打开行或列");
                        return;
                    }
                    var id = connectTable.getValueAt(row, 0);
                    var connectInfo = ConnectService.service.getConnect(id);
                    onCancel();
                    openActionCallback.accept(connectInfo);
                });
                add(openConnectMenu);
                //表格编辑链接操作
                var editConnectMenu = new JMenuItem("编辑链接");
                editConnectMenu.addActionListener(e -> {
                    var row = connectTable.getSelectedRow();
                    if (row == -1) {
                        MsgUtil.showInformationDialog(this, "未选中编辑行或列");
                        return;
                    }
                    var id = connectTable.getValueAt(row, 0);
                    var connectInfo = ConnectService.service.getConnect(id);
                    onCancel();
                    editActionCallback.accept(connectInfo);
                });
                add(editConnectMenu);
                //表格删除操作
                var deleteConnectMenu = new JMenuItem("删除链接");
                deleteConnectMenu.addActionListener(e -> {
                    int row = connectTable.getSelectedRow();
                    if (row == -1) {
                        MsgUtil.showInformationDialog(this, "未选中删除行或列");
                        return;
                    }
                    var id = connectTable.getValueAt(row, 0);
                    var connectInfo = ConnectService.service.getConnect(id);
                    if (Fn.isNotNull(connectInfo)) {
                        delActionCallback.accept(connectInfo);
                    }
                    ((ConnectTableModelComponent) connectTable.getModel()).removeRow(row);
                    connectTable.revalidate();
                });
                add(deleteConnectMenu);
            }
        });
        //查询数据连接列表
        List<ConnectInfo> connectInfoList = ConnectService.service.getAllConnectList();
        connectTable.setModel(new ConnectTableModelComponent(connectInfoList, "编号", "名称", "地址", "端口", "SSL", "连接模式"));
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void createUIComponents() {
        addConnectBtn = new JButton();
        addConnectBtn.addActionListener(e -> {
            dispose();
            AddConnectDialog.showAddConnectDialog((Frame) getOwner(), openActionCallback);
        });
        addConnectBtn.setIcon(new FlatSVGIcon("icons/connection.svg"));
    }
}
