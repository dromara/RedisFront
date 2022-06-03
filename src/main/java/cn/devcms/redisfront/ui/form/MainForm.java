package cn.devcms.redisfront.ui.form;

import cn.devcms.redisfront.ui.dialog.AddConnectDialog;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.formdev.flatlaf.FlatClientProperties.*;

public class MainForm {
    private JPanel contentPanel;
    private JTabbedPane tabPanel;
    private JButton addBtn;
    private JLabel iconBtn;
    private JButton refreshBtn;
    private JFrame root;

    private HomeForm homeForm;

    public MainForm(JFrame root) {
        this.root = root;
        homeForm = new HomeForm();
        $$$setupUI$$$();
        contentPanel.add(homeForm.$$$getRootComponent$$$(), BorderLayout.CENTER);

    }

    public void addAction() {
        tabPanel.addTab("127.0.0.1", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
        contentPanel.add(tabPanel, BorderLayout.CENTER);
        contentPanel.updateUI();

    }

    private void createUIComponents() {
        iconBtn = new JLabel();
        iconBtn.setIcon(new FlatSVGIcon("icons/Project.svg"));
        addBtn = new JButton();
        addBtn.setIcon(new FlatSVGIcon("icons/add.svg"));
        refreshBtn = new JButton();
        refreshBtn.setIcon(new FlatSVGIcon("icons/refresh.svg"));
        tabPanel = new JTabbedPane();
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.LEFT);
        tabPanel.putClientProperty(TABBED_PANE_SHOW_TAB_SEPARATORS, true);
        tabPanel.putClientProperty(TABBED_PANE_TAB_CLOSABLE, true);
        tabPanel.putClientProperty(TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT, "");

        UIManager.put("TabbedPane.closeHoverForeground", Color.red);
        UIManager.put("TabbedPane.closePressedForeground", Color.red);
        UIManager.put("TabbedPane.closeHoverBackground", new Color(0, true));
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());

        tabPanel.putClientProperty(TABBED_PANE_TAB_ALIGNMENT, SwingConstants.LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, TABBED_PANE_ALIGN_LEADING);
        tabPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, TABBED_PANE_TAB_TYPE_UNDERLINED);

        JLabel label = new JLabel(new FlatSVGIcon("icons/icon_home.svg"), JLabel.CENTER);
        label.setBorder(new EmptyBorder(10, 0, 10, 0));
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(new EmptyBorder(0, 10, 10, 10));
        toolBar.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());
        JButton newBtn = new JButton(null, new FlatSVGIcon("icons/new.svg"));
        newBtn.setToolTipText("新建连接");
        newBtn.addActionListener(e -> {
            AddConnectDialog addConnectDialog = new AddConnectDialog(root);
            addConnectDialog.pack();
            addConnectDialog.setVisible(true);
        });
        jPanel.add(newBtn);
        JButton openBtn = new JButton(null, new FlatSVGIcon("icons/open.svg"));
        openBtn.setToolTipText("打开连接");
        openBtn.addActionListener(e -> {
            AddConnectDialog addConnectDialog = new AddConnectDialog(root);
            addConnectDialog.pack();
            addConnectDialog.setVisible(true);
        });
        jPanel.add(openBtn);
        toolBar.add(jPanel, BorderLayout.SOUTH);


        tabPanel.putClientProperty(TABBED_PANE_TRAILING_COMPONENT, toolBar);

//        tabPanel.addTab(null, new FlatSVGIcon("icons/icon_home.svg"), new MainConnectForm(tabPanel).$$$getRootComponent$$$());
//        tabPanel.addTab(null, new FlatSVGIcon("icons/icon_db.svg"), new JPanel());
//        tabPanel.addTab(null, new FlatSVGIcon("icons/icon_db.svg"), new JPanel());
//        tabPanel.addTab("127.0.0.1", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("8.22.35.5", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("8.22.35.5", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("8.22.35.5", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("8.22.35.5", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("8.22.35.5", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("47.201.112.20", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("47.201.112.20", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("47.201.112.20", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产456456456456)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab("10.0.1.5 (生产)      ", new FlatSVGIcon("icons/icon_db5.svg"), new JPanel());
//        tabPanel.addTab(null, new FlatSVGIcon("icons/icon_command.svg"), new JPanel());
//        tabPanel.addTab(null, new FlatSVGIcon("icons/icon_dashboard.svg"), new JPanel());

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
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(0, 0));
        contentPanel.setEnabled(true);
        tabPanel.setTabLayoutPolicy(1);
        tabPanel.setTabPlacement(2);
        contentPanel.add(tabPanel, BorderLayout.CENTER);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
