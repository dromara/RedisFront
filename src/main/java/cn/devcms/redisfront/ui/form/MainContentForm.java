package cn.devcms.redisfront.ui.form;

import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.service.ConnectService;
import cn.devcms.redisfront.ui.component.DatabaseTabbedComponent;
import cn.devcms.redisfront.ui.dialog.AddConnectDialog;
import cn.devcms.redisfront.ui.dialog.OpenConnectDialog;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;

public class MainContentForm {
    private JPanel contentPanel;
    private JTabbedPane tabPanel;
    private final JFrame owner;
    private final _NoneForm noneForm;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public MainContentForm(JFrame frame) {
        owner = frame;
        noneForm = new _NoneForm();
        contentPanel.add(noneForm.getContentPanel(), BorderLayout.CENTER);

    }

    public void addActionPerformed(ConnectInfo connectInfo) {
        //存数据库
        if (Fn.equal(connectInfo.id(), 0)) {
            ConnectService.service.save(connectInfo);
        } else {
            ConnectService.service.update(connectInfo);
        }
        //添加到tab面板
        tabPanel.addTab(connectInfo.title(), new FlatSVGIcon("icons/icon_db5.svg"), new DatabaseTabbedComponent(connectInfo));
        tabPanel.setSelectedIndex(tabPanel.getTabCount() - 1);
        contentPanel.add(tabPanel, BorderLayout.CENTER, 0);
    }

    private void createUIComponents() {
        UIManager.put("TabbedPane.closeHoverForeground", Color.red);
        UIManager.put("TabbedPane.selectHighlight", Color.red);
        UIManager.put("*.selectionBackground", Color.red);
        UIManager.put("TabbedPane.closePressedForeground", Color.red);
        UIManager.put("TabbedPane.closeHoverBackground", new Color(0, true));
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());
        tabPanel = new JTabbedPane();
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        //SHOW LINE
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        //SHOW CLOSE BUTTON
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);

        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_PLACEMENT, FlatClientProperties.TABBED_PANE_PLACEMENT_BOTH);
        //SHOW CLOSE BUTTON
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "关闭连接");
        //SHOW CLOSE BUTTON Callback
        tabPanel.putClientProperty("JTabbedPane.tabCloseCallback", (BiConsumer<JTabbedPane, Integer>) (tabbedPane, tabIndex) -> {
            tabPanel.remove(tabIndex);
            if (tabbedPane.getTabCount() == 0) {
                contentPanel.add(noneForm.getContentPanel(), BorderLayout.CENTER, 0);
            }
        });
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, SwingConstants.LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        var toolBar = new JToolBar();
        toolBar.setBorder(new EmptyBorder(0, 10, 10, 10));
        toolBar.setLayout(new BorderLayout());
        var jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());

        var newBtn = new JButton(null, new FlatSVGIcon("icons/new_conn.svg"));
        newBtn.setToolTipText("新建连接");
        newBtn.addActionListener(e -> AddConnectDialog.showAddConnectDialog(owner,
                //打开连接回调
                (this::addActionPerformed)
        ));
        jPanel.add(newBtn);

        var openBtn = new JButton(null, new FlatSVGIcon("icons/open_conn.svg"));
        openBtn.setToolTipText("打开连接");
        openBtn.addActionListener(e -> OpenConnectDialog.showOpenConnectDialog(
                owner,
                //打开连接回调
                (this::addActionPerformed),
                //编辑连接回调
                (connectInfo -> AddConnectDialog.showEditConnectDialog(
                        owner,
                        connectInfo,
                        (this::addActionPerformed)
                )),
                //删除连接回调
                (connectInfo -> ConnectService.service.delete(connectInfo.id()))));
        jPanel.add(openBtn);
        toolBar.add(jPanel, BorderLayout.SOUTH);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, toolBar);
    }

}
