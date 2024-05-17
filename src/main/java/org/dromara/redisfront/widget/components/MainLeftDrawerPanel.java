package org.dromara.redisfront.widget.components;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import org.dromara.redisfront.commons.constant.Res;
import org.dromara.redisfront.model.ConnectionTreeNodeInfo;
import org.dromara.redisfront.widget.MainComponent;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.action.DrawerAnimationAction;
import org.dromara.redisfront.widget.ui.*;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import org.jetbrains.annotations.NotNull;
import raven.drawer.component.DrawerPanel;
import raven.drawer.component.SimpleDrawerBuilder;
import raven.drawer.component.footer.SimpleFooterData;
import raven.drawer.component.header.SimpleHeaderData;
import raven.drawer.component.menu.MenuEvent;
import raven.drawer.component.menu.SimpleMenuOption;
import raven.drawer.component.menu.data.Item;
import raven.drawer.component.menu.data.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Getter
public class MainLeftDrawerPanel extends SimpleDrawerBuilder {

    MenuItem[] items = new MenuItem[]{
            new Item("阿里云服务器", "folder.svg")
                    .subMenu(new Item("2.127.231.79", "link.svg"))
                    .subMenu(new Item("47.22.5.98", "link.svg"))
                    .subMenu(new Item("5.48.77.19", "link.svg")),
            new Item("127.0.0.1", "link.svg"),
            new Item("阿里云主机", "link.svg"),
    };

    private final MainWidget owner;
    private final MenuEvent menuEvent;
    private final DrawerAnimationAction drawerAnimationAction;
    private final DrawerMenuItemEvent drawerMenuItemEvent;


    public DrawerPanel buildDrawerPanel() {
        return new DrawerPanel(this);
    }

    public MainLeftDrawerPanel(MainWidget owner, MenuEvent menuEvent, DrawerAnimationAction drawerAnimationAction, DrawerMenuItemEvent drawerMenuItemEvent) {
        this.owner = owner;
        this.menuEvent = menuEvent;
        this.drawerAnimationAction = drawerAnimationAction;
        this.drawerMenuItemEvent = drawerMenuItemEvent;
        initializeUI();
    }

    private void initializeUI() {
        var simpleMenuOption = this.getSimpleMenuOption();
        simpleMenuOption.addMenuEvent(menuEvent);
    }

    @Override
    public Component getFooter() {
        var footerPanel = new JPanel();
        footerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        footerPanel.setLayout(new MigLayout("al center", "[fill,fill]", "fill"));
        footerPanel.add(new ThemesChangePanel());
        return footerPanel;
    }


    @Override
    public Component getHeader() {
        var headerPanel = new JPanel();
        headerPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        headerPanel.setLayout(new BorderLayout());
        if (SystemInfo.isMacOS) {
            headerPanel.setBorder(new EmptyBorder(35, 15, 5, 15));
        } else {
            headerPanel.setBorder(new EmptyBorder(15, 15, 5, 15));
        }
        headerPanel.add(DefaultLogoPanel.getInstance());
        return headerPanel;
    }

    @Override
    public SimpleHeaderData getSimpleHeaderData() {
        return new SimpleHeaderData();
    }

    @Override
    public SimpleMenuOption getSimpleMenuOption() {
        return new SimpleMenuOption();
    }

    @Override
    public SimpleFooterData getSimpleFooterData() {
        return new SimpleFooterData();
    }

    @Override
    public Component getMenu() {
        JTree tree = createConnectTree();
        tree.setCellRenderer(new DefaultXTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                this.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
                if (!leaf) {
                    this.setIcon(Res.FOLDER_ICON_14x14);
                } else {
                    this.setIcon(Res.LINK_ICON_14x14);
                }
                return c;
            }

            {
                setTextNonSelectionColor(Color.WHITE);
            }
        });

        JPopupMenu treePopupMenu = new JPopupMenu();
        treePopupMenu.add(new JMenuItem("新建分组"));
        treePopupMenu.add(new JMenuItem("添加连接"));
        treePopupMenu.addSeparator();
        treePopupMenu.add(new JMenuItem("导入连接"));
        treePopupMenu.add(new JMenuItem("导出连接"));

        JPopupMenu treeNodePopupMenu = new JPopupMenu();
        treeNodePopupMenu.add(new JMenuItem("打开连接"));
        treeNodePopupMenu.addSeparator();
        treeNodePopupMenu.add(new JMenuItem("编辑连接"));
        treeNodePopupMenu.add(new JMenuItem("删除连接"));

        JPopupMenu treeNodeGroupPopupMenu = new JPopupMenu();
        treeNodeGroupPopupMenu.add(new JMenuItem("编辑分组"));
        treeNodeGroupPopupMenu.add(new JMenuItem("删除分组"));

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
                if (tree.getSelectionPath() != null) {
                    Object component = tree.getSelectionPath().getLastPathComponent();
                    if (component instanceof ConnectionTreeNodeInfo connectionTreeNodeInfo) {
                        if (connectionTreeNodeInfo.getIsFolder()) {
                            treeNodeGroupPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                        } else {
                            treeNodePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                } else {
                    treePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);

        ConnectionTreeNodeInfo connectionTreeNodeInfo = new ConnectionTreeNodeInfo("阿里云主机", null, true);

        ConnectionTreeNodeInfo connectionTreeNodeInfo1 = new ConnectionTreeNodeInfo("47.10.25.37", null, false);

        connectionTreeNodeInfo.add(connectionTreeNodeInfo1);

        ConnectionTreeNodeInfo connectionTreeNodeInfo2 = new ConnectionTreeNodeInfo("47.10.25.38", null, false);

        connectionTreeNodeInfo.add(connectionTreeNodeInfo2);

        root.add(connectionTreeNodeInfo);
        tree.setModel(new DefaultTreeModel(root));

        JScrollPane scrollPane = createScroll(tree);
        scrollPane.setBorder(new EmptyBorder(0, 10, 0, 10));
        return scrollPane;
    }

    private static @NotNull JTree createConnectTree() {
        JTree tree = new JTree();
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setDragEnabled(true);
        tree.putClientProperty(FlatClientProperties.STYLE,
                "selectionArc:10;" +
                        "rowHeight:25;" +
                        "background:$RedisFront.main.background;" +
                        "foreground:#ffffff;" +
                        "[light]selectionBackground:darken(#FFFFFF,20%);" +
                        "[light]selectionForeground:darken($Label.foreground,50%);" +
                        "[dark]selectionBackground:darken($Label.foreground,50%);" +
                        "showCellFocusIndicator:false;"

        );
        return tree;
    }

    @Override
    public void build(DrawerPanel drawerPanel) {
        drawerPanel.putClientProperty(FlatClientProperties.STYLE, "background:$RedisFront.main.background");
    }

    @Override
    public int getDrawerWidth() {
        return MainComponent.DEFAULT_DRAWER_WIDTH;
    }
}
