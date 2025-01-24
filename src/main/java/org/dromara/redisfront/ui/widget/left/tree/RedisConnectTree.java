package org.dromara.redisfront.ui.widget.left.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.quickswing.events.QSEventListener;
import org.dromara.quickswing.ui.app.QSAction;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.dao.ConnectDetailDao;
import org.dromara.redisfront.dao.ConnectGroupDao;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.dromara.redisfront.model.entity.ConnectGroupEntity;
import org.dromara.redisfront.ui.dialog.AddConnectDialog;
import org.dromara.redisfront.ui.event.DeleteConnectTreeEvent;
import org.dromara.redisfront.ui.event.RefreshConnectTreeEvent;
import org.dromara.redisfront.ui.support.extend.ConnectTreeCellRenderer;
import org.dromara.redisfront.ui.widget.MainWidget;
import org.jdesktop.swingx.JXTree;
import raven.drawer.component.menu.MenuEvent;
import raven.toast.Notifications;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class RedisConnectTree extends JXTree {
    private final MainWidget owner;
    private final MenuEvent menuEvent;
    private JPopupMenu treePopupMenu;
    private JPopupMenu treeNodePopupMenu;
    private JPopupMenu treeNodeGroupPopupMenu;

    public RedisConnectTree(MainWidget owner, MenuEvent menuEvent) {
        this.owner = owner;
        this.menuEvent = menuEvent;
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.setDragEnabled(true);
        this.setFocusable(false);
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
        this.loadTreeNodeData(context);
        this.registerEventListener(context);
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
        context.getEventBus().subscribe(new QSEventListener<>(owner) {
            @Override
            protected void onEvent(QSEvent event) {
                if (event instanceof RefreshConnectTreeEvent) {
                    loadTreeNodeData(context);
                }
                if (event instanceof DeleteConnectTreeEvent deleteConnectTreeEvent) {
                    Object message = deleteConnectTreeEvent.getMessage();
                    if (message instanceof RedisConnectTreeNode RedisConnectTreeNode) {
                        context.taskExecute(() -> {
                            ConnectGroupDao.newInstance(datasource).delete(RedisConnectTreeNode.id());
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
        List<ConnectGroupEntity> connectGroupEntityList = ConnectGroupDao.newInstance(dataSource).loadAll();
        for (ConnectGroupEntity connectGroupEntity : connectGroupEntityList) {
            RedisConnectTreeNode treeGroupNodeInfo = new RedisConnectTreeNode(connectGroupEntity);
            List<ConnectDetailEntity> connectDetailEntities = ConnectDetailDao.newInstance(dataSource).loadAll(connectGroupEntity.getGroupId());
            if (CollUtil.isNotEmpty(connectDetailEntities)) {
                for (ConnectDetailEntity connectDetailEntity : connectDetailEntities) {
                    RedisConnectTreeNode treeDetailNodeInfo = new RedisConnectTreeNode(connectDetailEntity);
                    treeGroupNodeInfo.add(treeDetailNodeInfo);
                }
            }
            root.add(treeGroupNodeInfo);
        }
        List<ConnectDetailEntity> connectDetailEntityList = ConnectDetailDao.newInstance(dataSource).loadAll();
        for (ConnectDetailEntity connectDetailEntity : connectDetailEntityList) {
            RedisConnectTreeNode treeDetailNodeInfo = new RedisConnectTreeNode(connectDetailEntity);
            root.add(treeDetailNodeInfo);
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
            public void mousePressed(MouseEvent e) {
                mouseReleased(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    setSelectionPath(getPathForLocation(e.getX(), e.getY()));
                    if (getSelectionPath() != null) {
                        Object component = getSelectionPath().getLastPathComponent();
                        if (component instanceof RedisConnectTreeNode redisConnectTreeNode) {
                            if (redisConnectTreeNode.getIsGroup()) {
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
        JMenuItem addConnectMenuItem = new JMenuItem("添加连接") {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object pathComponent = selectionPath.getLastPathComponent();
                    if (pathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        AddConnectDialog.getInstance(owner).showNewConnectDialog(redisConnectTreeItem.id());

                    }
                });
            }
        };
        treeNodeGroupPopupMenu.add(addConnectMenuItem);
        treeNodeGroupPopupMenu.addSeparator();

        JMenuItem updateConnectMenuItem = new JMenuItem("编辑分组") {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        String groupName = redisConnectTreeItem.toString();
                        String value = (String) JOptionPane.showInputDialog(owner, "分组名称", "修改分组", JOptionPane.PLAIN_MESSAGE, null, null, groupName);
                        if (StrUtil.isEmpty(value) || StrUtil.equals(value, groupName)) {
                            return;
                        }
                        context.taskExecute(() -> {
                            ConnectGroupDao.newInstance(datasource).update(redisConnectTreeItem.id(), value);
                            context.getEventBus().publish(new RefreshConnectTreeEvent(null));
                            return null;
                        }, (_, exception) -> {
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
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
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
                addActionListener(_ -> {
                    context.taskExecute(() -> ConnectGroupDao.newInstance(datasource).count(), (count, exp) -> {
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
                            ConnectGroupDao.newInstance(datasource).save(value);
                            context.getEventBus().publish(new RefreshConnectTreeEvent(null));
                            return null;
                        }, (_, exception) -> {
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
                addActionListener(_ -> {
                    AddConnectDialog.getInstance(owner).showNewConnectDialog(null);
                });
            }
        };
        treePopupMenu.add(addConnectMenuItem);
        treePopupMenu.addSeparator();

        JMenuItem importConnectMenuItem = new JMenuItem("导入连接");
        treePopupMenu.add(importConnectMenuItem);

        JMenuItem exportConnectMenuItem = new JMenuItem("导出连接");
        treePopupMenu.add(exportConnectMenuItem);

        owner.registerAction(this, new QSAction<>(owner) {
            @Override
            public void handleAction(ActionEvent actionEvent) {
                TreePath selectionPath = getSelectionPath();
                if (selectionPath == null) {
                    Notifications.getInstance().show(Notifications.Type.INFO, "请选择要打开的连接！");
                }
            }

            @Override
            public KeyStroke getKeyStroke() {
                return SystemInfo.isMacOS ?
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) :
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
            }
        });
    }


}
