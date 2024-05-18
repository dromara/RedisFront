package org.dromara.redisfront.widget.components.extend;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import com.formdev.flatlaf.FlatClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.database.DatabaseManager;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.constant.Res;
import org.dromara.redisfront.model.ConnectionTreeNodeInfo;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.event.ConnectTreeEvent;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;
import raven.toast.Notifications;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
        DatabaseManager databaseManager = context.getDatabaseManager();
        context.getEventBus().subscribe(event -> {
            if (event instanceof ConnectTreeEvent) {
                context.taskExecute(() -> {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("root", true);
                    List<Entity> connectGroup = DbUtil.use(databaseManager.getDatasource()).find(Entity.create("connect_group"));
                    for (Entity entity : connectGroup) {
                        ConnectionTreeNodeInfo connectionTreeNodeInfo = new ConnectionTreeNodeInfo(entity.getStr("group_name"), null, true);
                        root.add(connectionTreeNodeInfo);
                    }
                    return root;
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
        });

    }

    private void showMenu(MouseEvent e) {
        this.setSelectionPath(this.getPathForLocation(e.getX(), e.getY()));
        if (this.getSelectionPath() != null) {
            Object component = this.getSelectionPath().getLastPathComponent();
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
        treeNodeGroupPopupMenu.add(new JMenuItem("删除分组"));
    }


    public static class ConnectTreeCellRenderer extends DefaultXTreeCellRenderer {
        public ConnectTreeCellRenderer() {
            this.setTextNonSelectionColor(Color.WHITE);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            this.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            if (value instanceof ConnectionTreeNodeInfo connectionTreeNodeInfo) {
                if (connectionTreeNodeInfo.getIsFolder()) {
                    this.setIcon(Res.FOLDER_ICON_14x14);
                } else {
                    this.setIcon(Res.LINK_ICON_14x14);
                }
            }
            return c;
        }
    }
}
