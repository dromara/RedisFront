package cn.devcms.redisfront.ui.dialog;

import cn.devcms.redisfront.common.base.AbstractDialog;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class OpenConnectDialog extends AbstractDialog<Void> {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable connectTable;
    private JButton addConnectBtn;

    public static void showOpenConnectDialog(Frame owner) {
        var openConnectDialog = new OpenConnectDialog(owner);
        openConnectDialog.setSize(new Dimension(500, 280));
        openConnectDialog.setLocationRelativeTo(owner);
        openConnectDialog.pack();
        openConnectDialog.setVisible(true);
    }

    public OpenConnectDialog(Frame owner) {
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
    }


    private void connectTableInit() {
        connectTable.setShowHorizontalLines(true);
        connectTable.setShowVerticalLines(true);
        connectTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.CENTER);
            }
        });
        connectTable.setComponentPopupMenu(new JPopupMenu() {
            {
                var openConnectMenu = new JMenuItem("打开链接");
                add(openConnectMenu);
                var editConnectMenu = new JMenuItem("编辑链接");
                add(editConnectMenu);
                var deleteConnectMenu = new JMenuItem("删除链接");
                add(deleteConnectMenu);
            }
        });
        connectTable.setModel(new DefaultTableModel(new Object[][]{
                {"测试1", "127.0.0.1", "6379"},
                {"测试2", "192.168.1.1", "6379"},
                {"生产服务器", "47.22.55.128", "6379"}
        },
                new String[]{
                        "名称", "地址", "端口"
                }) {
            final Class<?>[] columnTypes = new Class<?>[]{
                    String.class, String.class, String.class
            };
            final boolean[] columnEditable = new boolean[]{
                    true, false, false
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnEditable[columnIndex];
            }
        });
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
            AddConnectDialog.showAddConnectDialog((Frame) getOwner(), (System.out::println));
        });
        addConnectBtn.setIcon(new FlatSVGIcon("icons/connection.svg"));
    }
}
