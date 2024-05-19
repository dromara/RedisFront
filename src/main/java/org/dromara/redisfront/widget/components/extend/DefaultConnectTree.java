package org.dromara.redisfront.widget.components.extend;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import com.formdev.flatlaf.FlatClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.constant.Res;
import org.dromara.redisfront.model.RedisConnectTreeItem;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.event.RefreshConnectTreeEvent;
import org.dromara.redisfront.widget.event.DeleteConnectTreeEvent;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import raven.toast.Notifications;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class DefaultConnectTree extends JXTree {
    private final MainWidget owner;
    private JPopupMenu treePopupMenu;
    private JPopupMenu treeNodePopupMenu;
    private JPopupMenu treeNodeGroupPopupMenu;

    public DefaultConnectTree(MainWidget owner) {
        this.owner = owner;
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.setDragEnabled(true);
        this.putClientProperty(FlatClientProperties.STYLE,
                "selectionArc:10;" +
                        "rowHeight:25;" +
                        "background:$RedisFront.main.background;" +
                        "foreground:#ffffff;" +
                        "[light]selectionBackground:darken(#FFFFFF,20%);" +
                        "[light]selectionForeground:darken($Label.foreground,50%);" +
                        "[dark]selectionBackground:darken($Label.foreground,50%);" +
                        "showCellFocusIndicator:false;"

        );
        this.setCellRenderer(new ConnectTreeCellRenderer());
        this.initPopupMenus();
        this.addMouseListener(new MouseAdapter() {
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


        });
        RedisFrontContext context = (RedisFrontContext) owner.getContext();
        this.initTreeNodeItem(context);
        this.subscribeEvent(context);
    }

    private void initTreeNodeItem(RedisFrontContext context) {
        DataSource datasource = context.getDatabaseManager().getDatasource();
        context.taskExecute(() -> this.buildConnectTreeItem(datasource), (r, e) -> {
            if (e != null) {
                log.error(e.getMessage());
                Notifications.getInstance().show(Notifications.Type.ERROR, e.getMessage());
            } else {
                this.setModel(new DefaultTreeModel(r));
                this.updateUI();
            }
        });
    }

    private void subscribeEvent(RedisFrontContext context) {
        DataSource datasource = context.getDatabaseManager().getDatasource();
        context.getEventBus().subscribe(event -> {
            if (event instanceof RefreshConnectTreeEvent) {
                initTreeNodeItem(context);
            }
            if (event instanceof DeleteConnectTreeEvent deleteConnectTreeEvent) {
                Object message = deleteConnectTreeEvent.getMessage();
                if (message instanceof RedisConnectTreeItem RedisConnectTreeItem) {
                    context.taskExecute(() -> {
                        DbUtil.use(datasource).del(Entity.create("connect_group").set("group_id", RedisConnectTreeItem.id()));
                        return buildConnectTreeItem(datasource);
                    }, (r, e) -> {
                        if (e != null) {
                            log.error(e.getMessage());
                            Notifications.getInstance().show(Notifications.Type.ERROR, e.getMessage());
                        } else {
                            this.setModel(new DefaultTreeModel(r));
                            this.updateUI();
                        }
                    });
                }
            }
        });
    }

    public DefaultMutableTreeNode buildConnectTreeItem(DataSource dataSource) throws SQLException {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);
        List<Entity> connectGroup = DbUtil.use(dataSource).find(Entity.create("connect_group"));
        for (Entity entity : connectGroup) {
            RedisConnectTreeItem treeNodeInfo = new RedisConnectTreeItem(
                    true,
                    entity.clone()
            );
            root.add(treeNodeInfo);
        }
        return root;
    }

    private void showMenu(MouseEvent e) {
        this.setSelectionPath(this.getPathForLocation(e.getX(), e.getY()));
        if (this.getSelectionPath() != null) {
            Object component = this.getSelectionPath().getLastPathComponent();
            if (component instanceof RedisConnectTreeItem redisConnectTreeItem) {
                if (redisConnectTreeItem.getIsGroup()) {
                    treeNodeGroupPopupMenu.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    treeNodePopupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        } else {
            treePopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (treePopupMenu != null && treeNodePopupMenu != null && treeNodeGroupPopupMenu != null) {
            treePopupMenu.updateUI();
            treeNodePopupMenu.updateUI();
            treeNodeGroupPopupMenu.updateUI();
        }
    }

    private void initPopupMenus() {
        RedisFrontContext context = (RedisFrontContext) owner.getContext();
        treePopupMenu = new JPopupMenu();
        treePopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");
        JMenuItem addConnectGroupMenuItem = new JMenuItem("新建分组");
        addConnectGroupMenuItem.addActionListener(owner.getAction("addConnectGroupAction"));
        treePopupMenu.add(addConnectGroupMenuItem);

        treePopupMenu.add(new JMenuItem("添加连接"));
        treePopupMenu.addSeparator();
        treePopupMenu.add(new JMenuItem("导入连接"));
        treePopupMenu.add(new JMenuItem("导出连接"));


        treeNodePopupMenu = new JPopupMenu();
        treeNodePopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");
        treeNodePopupMenu.add(new JMenuItem("打开连接"));
        treeNodePopupMenu.addSeparator();
        treeNodePopupMenu.add(new JMenuItem("编辑连接"));
        treeNodePopupMenu.add(new JMenuItem("删除连接"));


        treeNodeGroupPopupMenu = new JPopupMenu();
        treeNodeGroupPopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");
        treeNodeGroupPopupMenu.add(new JMenuItem("添加连接"));
        treeNodeGroupPopupMenu.addSeparator();
        treeNodeGroupPopupMenu.add(new JMenuItem("编辑分组"));
        JMenuItem deleteConnectMenuItem = new JMenuItem("删除分组");
        deleteConnectMenuItem.addActionListener(e -> {
            TreePath selectionPath = this.getSelectionPath();
            if (selectionPath == null) {
                return;
            }
            Object lastPathComponent = selectionPath.getLastPathComponent();
            if (lastPathComponent instanceof RedisConnectTreeItem redisConnectTreeItem) {
                context.getEventBus().publish(new DeleteConnectTreeEvent(redisConnectTreeItem));
            }
        });
        treeNodeGroupPopupMenu.add(deleteConnectMenuItem);
    }


    public static class ConnectTreeCellRenderer extends DefaultXTreeCellRenderer {
        public ConnectTreeCellRenderer() {
            this.setTextNonSelectionColor(Color.WHITE);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            this.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            if (value instanceof RedisConnectTreeItem redisConnectTreeItem) {
                if (redisConnectTreeItem.getIsGroup()) {
                    this.setIcon(Res.FOLDER_ICON_14x14);
                } else {
                    this.setIcon(Res.LINK_ICON_14x14);
                }
            }
            return c;
        }
    }
}
