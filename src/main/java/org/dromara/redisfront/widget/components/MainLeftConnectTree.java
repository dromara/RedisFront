package org.dromara.redisfront.widget.components;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import com.formdev.flatlaf.FlatClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventListener;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.model.RedisConnectTreeItem;
import org.dromara.redisfront.widget.MainWidget;
import org.dromara.redisfront.widget.components.extend.ConnectTreeCellRenderer;
import org.dromara.redisfront.widget.dialog.AddConnectDialog;
import org.dromara.redisfront.widget.event.DeleteConnectTreeEvent;
import org.dromara.redisfront.widget.event.RefreshConnectTreeEvent;
import org.jdesktop.swingx.JXTree;
import raven.toast.Notifications;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class MainLeftConnectTree extends JXTree {
    private final MainWidget owner;
    private JPopupMenu treePopupMenu;
    private JPopupMenu treeNodePopupMenu;
    private JPopupMenu treeNodeGroupPopupMenu;

    public MainLeftConnectTree(MainWidget owner) {
        this.owner = owner;
        Notifications.getInstance().setJFrame(owner);
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
        RedisFrontContext context = (RedisFrontContext) owner.getContext();
        this.initPopupMenus(context);
        this.registerEventListener(context);
        this.loadTreeNodeData(context);
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

    private void loadTreeNodeData(RedisFrontContext context) {
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

    private void registerEventListener(RedisFrontContext context) {
        DataSource datasource = context.getDatabaseManager().getDatasource();
        context.getEventBus().subscribe(new QSEventListener<>() {
            @Override
            protected void onEvent(QSEvent event) {
                if (event instanceof RefreshConnectTreeEvent) {
                    loadTreeNodeData(context);
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
                                setModel(new DefaultTreeModel(r));
                                updateUI();
                            }
                        });
                    }
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

    private void initPopupMenus(RedisFrontContext context) {
        DataSource datasource = context.getDatabaseManager().getDatasource();
        this.initTreePopupMenu(context, datasource);
        this.initTreeNodePopupMenu(context, datasource);
        this.initTreeNodeGroupPopupMenu(context, datasource);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    setSelectionPath(getPathForLocation(e.getX(), e.getY()));
                    if (getSelectionPath() != null) {
                        Object component = getSelectionPath().getLastPathComponent();
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
            }
        });
    }

    private void initTreeNodeGroupPopupMenu(RedisFrontContext context, DataSource datasource) {
        treeNodeGroupPopupMenu = new JPopupMenu();
        treeNodeGroupPopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");
        treeNodeGroupPopupMenu.add(new JMenuItem("添加连接"));
        treeNodeGroupPopupMenu.addSeparator();

        JMenuItem updateConnectMenuItem = new JMenuItem("编辑分组") {
            {
                addActionListener(e -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeItem redisConnectTreeItem) {
                        String groupName = redisConnectTreeItem.getOrigin().getStr("group_name");
                        String value = (String) JOptionPane.showInputDialog(owner, "分组名称", "修改分组", JOptionPane.PLAIN_MESSAGE, null, null, groupName);
                        if (StrUtil.isEmpty(value) || StrUtil.equals(value, groupName)) {
                            return;
                        }
                        context.taskExecute(() -> {
                            DbUtil.use(datasource).update(Entity.create("connect_group").set("group_name", value), Entity.create("connect_group").set("group_id", redisConnectTreeItem.id()));
                            context.getEventBus().publish(new RefreshConnectTreeEvent(null));
                            return null;
                        }, (result, exception) -> {
                            if (exception != null) {
                                log.error(exception.getMessage());
                                Notifications.getInstance().show(Notifications.Type.ERROR, exception.getMessage());
                            } else {
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, "修改成功！");
                            }
                        });
                    }
                });
            }
        };
        treeNodeGroupPopupMenu.add(updateConnectMenuItem);
        JMenuItem deleteConnectMenuItem = new JMenuItem("删除分组") {
            {
                addActionListener(e -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeItem redisConnectTreeItem) {
                        context.getEventBus().publish(new DeleteConnectTreeEvent(redisConnectTreeItem));
                    }
                });
            }
        };
        treeNodeGroupPopupMenu.add(deleteConnectMenuItem);
    }

    private void initTreeNodePopupMenu(RedisFrontContext context, DataSource datasource) {
        treeNodePopupMenu = new JPopupMenu();
        treeNodePopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");

        JMenuItem openConnectMenuItem = new JMenuItem("打开连接");
        treeNodePopupMenu.add(openConnectMenuItem);

        treeNodePopupMenu.addSeparator();

        JMenuItem editConnectMenuItem = new JMenuItem("编辑连接");
        treeNodePopupMenu.add(editConnectMenuItem);

        JMenuItem deleteConnectMenuItem = new JMenuItem("删除连接");
        treeNodePopupMenu.add(deleteConnectMenuItem);
    }

    private void initTreePopupMenu(RedisFrontContext context, DataSource datasource) {
        treePopupMenu = new JPopupMenu();
        treePopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");
        JMenuItem addConnectGroupMenuItem = new JMenuItem("新建分组") {
            {
                addActionListener(e -> {
                    context.taskExecute(() -> {
                        Entity connectGroup = Entity.create("connect_group");
                        return DbUtil.use(datasource).count(connectGroup);
                    }, (count, exp) -> {
                        if (exp != null) {
                            log.error(exp.getMessage());
                            Notifications.getInstance().show(Notifications.Type.ERROR, exp.getMessage());
                            return;
                        }
                        String groupName = "新建分组";
                        if (count > 0) {
                            groupName += "(" + count + ")";
                        }
                        String value = (String) JOptionPane.showInputDialog(owner, "分组名称", "添加分组", JOptionPane.QUESTION_MESSAGE, null, null, groupName);
                        if (StrUtil.isEmpty(value)) {
                            return;
                        }
                        context.taskExecute(() -> {
                            Entity connectGroup = Entity.create("connect_group");
                            connectGroup.set("group_name", value);
                            DbUtil.use(datasource).insert(connectGroup);
                            context.getEventBus().publish(new RefreshConnectTreeEvent(null));
                            return null;
                        }, (result, exception) -> {
                            if (exception != null) {
                                log.error(exception.getMessage());
                                Notifications.getInstance().show(Notifications.Type.ERROR, exception.getMessage());
                            } else {
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, "添加成功！");
                            }
                        });
                    });
                });
            }
        };
        treePopupMenu.add(addConnectGroupMenuItem);

        JMenuItem addConnectMenuItem = new JMenuItem("添加连接") {
            {
                addActionListener(e -> {
                    AddConnectDialog addConnectDialog = new AddConnectDialog(owner, "添加连接");
                    addConnectDialog.setLocationRelativeTo(null);
                    addConnectDialog.setVisible(true);
                    addConnectDialog.pack();
                });
            }
        };
        treePopupMenu.add(addConnectMenuItem);
        treePopupMenu.addSeparator();

        JMenuItem importConnectMenuItem = new JMenuItem("导入连接");
        treePopupMenu.add(importConnectMenuItem);

        JMenuItem exportConnectMenuItem = new JMenuItem("导出连接");
        treePopupMenu.add(exportConnectMenuItem);
    }


}
