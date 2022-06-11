package cn.devcms.redisfront.ui.form;

import cn.devcms.redisfront.ui.component.TabbedComponent;
import cn.devcms.redisfront.ui.dialog.AddConnectDialog;
import cn.devcms.redisfront.ui.dialog.OpenConnectDialog;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class MainContentForm {
    private JPanel contentPanel;
    private JTabbedPane tabPanel;
    private final JFrame frame;
    private final _NoneForm noneForm;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public MainContentForm(JFrame frame) {
        this.frame = frame;
        noneForm = new _NoneForm();
        contentPanel.add(noneForm.getContentPanel(), BorderLayout.CENTER);

    }

    public void addAction() {
        tabPanel.addTab("127.0.0.1", new FlatSVGIcon("icons/icon_db5.svg"), new TabbedComponent());
        tabPanel.setSelectedIndex(tabPanel.getTabCount() - 1);
        contentPanel.add(tabPanel, BorderLayout.CENTER, 0);

    }

    private void createUIComponents() {
        contentPanel = new JPanel();
        UIManager.put("TabbedPane.closeHoverForeground", Color.red);
        UIManager.put("TabbedPane.closePressedForeground", Color.red);
        UIManager.put("TabbedPane.closeHoverBackground", new Color(0, true));
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());
        tabPanel = new JTabbedPane();

        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        //SHOW LINE
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        //SHOW CLOSE BUTTON
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_CLOSABLE, true);
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
        //SHOW CLOSE BUTTON
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_UNDERLINED);

        //BUTTOM GROUP
        JLabel label = new JLabel(new FlatSVGIcon("icons/icon_home.svg"), JLabel.CENTER);
        label.setBorder(new EmptyBorder(10, 0, 10, 0));
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(new EmptyBorder(0, 10, 10, 10));
        toolBar.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());
        JButton newBtn = new JButton(null, new FlatSVGIcon("icons/new.svg"));
        //new add connect
        newBtn.setToolTipText("新建连接");
        newBtn.addActionListener(e -> AddConnectDialog.showAddConnectDialog(frame, (System.out::println)));
        jPanel.add(newBtn);

        //open connect
        JButton openBtn = new JButton(null, new FlatSVGIcon("icons/open.svg"));
        openBtn.setToolTipText("打开连接");
        openBtn.addActionListener(e -> OpenConnectDialog.showOpenConnectDialog(frame));
        jPanel.add(openBtn);

        toolBar.add(jPanel, BorderLayout.SOUTH);
        tabPanel.putClientProperty(TABBED_PANE_TRAILING_COMPONENT, toolBar);
    }

}
