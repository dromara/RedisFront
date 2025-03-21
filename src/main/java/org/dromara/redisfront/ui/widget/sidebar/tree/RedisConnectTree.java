package org.dromara.redisfront.ui.widget.sidebar.tree;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.SystemInfo;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.QSAction;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.commons.resources.Icons;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.dao.ConnectDetailDao;
import org.dromara.redisfront.dao.ConnectGroupDao;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.dromara.redisfront.model.entity.ConnectGroupEntity;
import org.dromara.redisfront.model.tree.TreeNodeInfo;
import org.dromara.redisfront.ui.dialog.AddConnectDialog;
import org.dromara.redisfront.ui.event.RefreshConnectTreeEvent;
import org.dromara.redisfront.ui.handler.ConnectHandler;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import org.jdesktop.swingx.JXTree;
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
    private final RedisFrontWidget owner;
    private final ConnectHandler connectHandler;
    private final RedisFrontContext context;
    private JPopupMenu treePopupMenu;
    private JPopupMenu treeNodePopupMenu;
    private JPopupMenu treeNodeGroupPopupMenu;

    public RedisConnectTree(RedisFrontWidget owner, ConnectHandler connectHandler) {
        this.owner = owner;
        this.connectHandler = connectHandler;
        this.context = (RedisFrontContext) owner.getContext();
        this.initializeUI();
        this.initializeActions();
        this.initializeComponents();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getModel().getChildCount(getModel().getRoot()) == 0) {
            Graphics2D g2d = (Graphics2D) g;
            Image noneImage = Icons.NONE.getImage();
            g2d.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    0.2f
            ));
            int x = (getWidth() - noneImage.getWidth(null)) / 2;
            int y = (getHeight() - noneImage.getHeight(null)-90) / 2;
            g2d.drawImage(noneImage, x, y, null);
            g2d.setFont(getFont().deriveFont(Font.BOLD, 15.5f));
            g2d.drawString(owner.$tr("DataSearchForm.tree.none.text"), x, y + 80);
        }
    }

    private void initializeUI() {
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
        this.setModel(new DefaultTreeModel(new TreeNodeInfo()));
        this.setCellRenderer(new RedisConnectTreeCellRenderer());
    }

    private void initializeComponents() {
        this.initPopupMenus();
        this.loadTreeNodeData();
        this.registerEventListener();
    }

    private void initializeActions() {
        this.owner.registerAction(this, new QSAction<>(owner) {
            @Override
            public void handleAction(ActionEvent actionEvent) {
                TreePath selectionPath = getSelectionPath();
                if (selectionPath == null) {
                    AddConnectDialog.getInstance(owner).showNewConnectDialog(null);
                } else {
                    Object pathComponent = selectionPath.getLastPathComponent();
                    if (pathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        if (redisConnectTreeItem.getIsGroup()) {
                            AddConnectDialog.getInstance(owner).showNewConnectDialog(redisConnectTreeItem);
                        } else {
                            AddConnectDialog.getInstance(owner).showNewConnectDialog(null);
                        }
                    }
                }
            }

            @Override
            public KeyStroke getKeyStroke() {
                return SystemInfo.isMacOS ?
                        KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) :
                        KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
            }
        });
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

    private void loadTreeNodeData() {
        context.taskExecute(() -> this.buildConnectTreeItem(context.getDatabaseManager().getDatasource()), (result, exception) -> {
            if (exception != null) {
                log.error(exception.getMessage(), exception);
                Notifications.getInstance().show(Notifications.Type.ERROR, exception.getMessage());
            } else {
                List<Object> expandedPaths = RedisFrontUtils.saveExpandedPaths(this);
                DefaultTreeModel model = new DefaultTreeModel(result);
                this.setModel(model);
                RedisFrontUtils.restoreExpandedPaths(this, model, expandedPaths);
                this.updateUI();
            }
        });
    }

    private void registerEventListener() {
        owner.getEventListener().bind(-1, RefreshConnectTreeEvent.class, event -> {
            if (event instanceof RefreshConnectTreeEvent) {
                loadTreeNodeData();
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

    private void initPopupMenus() {
        DataSource datasource = context.getDatabaseManager().getDatasource();
        this.initTreePopupMenu(context, datasource);
        this.initTreeNodePopupMenu(context, datasource);
        this.initTreeNodeGroupPopupMenu(context, datasource);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath != null) {
                        openConnectHandler(selectionPath);
                    }
                }
            }

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
        JMenuItem addConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.addConnect")) {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object pathComponent = selectionPath.getLastPathComponent();
                    if (pathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        AddConnectDialog.getInstance(owner).showNewConnectDialog(redisConnectTreeItem);
                    }
                });
            }
        };
        treeNodeGroupPopupMenu.add(addConnectMenuItem);
        treeNodeGroupPopupMenu.addSeparator();

        JMenuItem updateConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.editConnectGroup")) {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        String groupName = redisConnectTreeItem.toString();
                        String value = (String) JOptionPane.showInputDialog(owner, owner.$tr("RedisConnectTree.popupMenu.connectGroupName"), owner.$tr("RedisConnectTree.popupMenu.editConnectGroup"), JOptionPane.PLAIN_MESSAGE, null, null, groupName);
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
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("RedisConnectTree.popupMenu.editConnectSuccess"));
                            }
                        });
                    }
                });
            }
        };
        treeNodeGroupPopupMenu.add(updateConnectMenuItem);
        JMenuItem deleteConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.deleteConnectGroup")) {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        context.taskExecute(() -> {
                            ConnectDetailDao.newInstance(datasource).deleteByGroupId(redisConnectTreeItem.id());
                            ConnectGroupDao.newInstance(datasource).delete(redisConnectTreeItem.id());
                            context.getEventBus().publish(new RefreshConnectTreeEvent(redisConnectTreeItem.id()));
                            return null;
                        }, (_, exception) -> {
                            if (exception != null) {
                                log.error(exception.getMessage());
                                Notifications.getInstance().show(Notifications.Type.ERROR, exception.getMessage());
                            } else {
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("RedisConnectTree.popupMenu.deleteConnectGroupSuccess"));
                            }
                        });
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

        JMenuItem openConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.openConnect")) {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    openConnectHandler(selectionPath);
                });
            }
        };
        treeNodePopupMenu.add(openConnectMenuItem);

        treeNodePopupMenu.addSeparator();

        JMenuItem editConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.editConnect")) {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object pathComponent = selectionPath.getLastPathComponent();
                    if (pathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        AddConnectDialog.getInstance(owner).showEditConnectDialog(redisConnectTreeItem);
                    }
                });
            }
        };
        treeNodePopupMenu.add(editConnectMenuItem);

        JMenuItem deleteConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.deleteConnect")) {
            {
                addActionListener(_ -> {
                    TreePath selectionPath = getSelectionPath();
                    if (selectionPath == null) {
                        return;
                    }
                    Object lastPathComponent = selectionPath.getLastPathComponent();
                    if (lastPathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
                        context.taskExecute(() -> {
                            ConnectDetailDao.newInstance(datasource).delete(redisConnectTreeItem.id());
                            context.getEventBus().publish(new RefreshConnectTreeEvent(redisConnectTreeItem));
                            return null;
                        }, (_, exception) -> {
                            if (exception != null) {
                                log.error(exception.getMessage());
                                Notifications.getInstance().show(Notifications.Type.ERROR, exception.getMessage());
                            } else {
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("RedisConnectTree.popupMenu.deleteConnectSuccess"));
                            }
                        });
                    }
                });
            }
        };
        treeNodePopupMenu.add(deleteConnectMenuItem);
    }

    private void initTreePopupMenu(RedisFrontContext context, DataSource datasource) {
        treePopupMenu = new JPopupMenu();
        treePopupMenu.putClientProperty(FlatClientProperties.STYLE,
                "[dark]background:darken(#FFFFFF,30%);");
        JMenuItem addConnectGroupMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.addConnectGroup")) {
            {
                addActionListener(_ -> {
                    context.taskExecute(() -> ConnectGroupDao.newInstance(datasource).count(), (count, exp) -> {
                        if (exp != null) {
                            log.error(exp.getMessage());
                            Notifications.getInstance().show(Notifications.Type.ERROR, exp.getMessage());
                            return;
                        }
                        String groupName = owner.$tr("RedisConnectTree.popupMenu.addConnectGroup");
                        if (count > 0) {
                            groupName += "(" + count + ")";
                        }
                        String value = (String) JOptionPane.showInputDialog(owner, owner.$tr("RedisConnectTree.popupMenu.connectGroupName"), owner.$tr("RedisConnectTree.popupMenu.addConnectGroup"), JOptionPane.QUESTION_MESSAGE, null, null, groupName);
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
                                Notifications.getInstance().show(Notifications.Type.SUCCESS, owner.$tr("RedisConnectTree.popupMenu.addConnectGroupSuccess"));
                            }
                        });
                    });
                });
            }
        };
        treePopupMenu.add(addConnectGroupMenuItem);

        JMenuItem addConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.addConnect")) {
            {
                addActionListener(_ -> {
                    AddConnectDialog.getInstance(owner).showNewConnectDialog(null);
                });
            }
        };
        treePopupMenu.add(addConnectMenuItem);
        treePopupMenu.addSeparator();

        JMenuItem importConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.importConnect"));
        treePopupMenu.add(importConnectMenuItem);

        JMenuItem exportConnectMenuItem = new JMenuItem(owner.$tr("RedisConnectTree.popupMenu.exportConnect"));
        treePopupMenu.add(exportConnectMenuItem);

        owner.registerAction(this, new QSAction<>(owner) {
            @Override
            public void handleAction(ActionEvent actionEvent) {
                TreePath selectionPath = getSelectionPath();
                if (selectionPath == null) {
                    Notifications.getInstance().show(Notifications.Type.INFO, owner.$tr("RedisConnectTree.popupMenu.selectConnect"));
                    return;
                }
                openConnectHandler(selectionPath);
            }

            @Override
            public KeyStroke getKeyStroke() {
                return SystemInfo.isMacOS ?
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) :
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
            }
        });
    }

    private void openConnectHandler(TreePath selectionPath) {
        Object pathComponent = selectionPath.getLastPathComponent();
        if (pathComponent instanceof RedisConnectTreeNode redisConnectTreeItem) {
            if (redisConnectTreeItem.getIsGroup()) {
                return;
            }
            connectHandler.accept(redisConnectTreeItem.getDetail().getConnectContext());
        }
    }


}
