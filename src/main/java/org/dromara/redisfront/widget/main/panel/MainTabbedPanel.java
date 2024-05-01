package org.dromara.redisfront.widget.main.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatToolBar;
import com.formdev.flatlaf.ui.FlatLineBorder;
import org.dromara.redisfront.commons.constant.Const;
import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.ui.form.fragment.DataChartsForm;
import org.dromara.redisfront.widget.main.action.DrawerAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.Cursor.HAND_CURSOR;

public class MainTabbedPanel extends JPanel {

    private final JTabbedPane contentPanel;

    public MainTabbedPanel(DrawerAction action) {
        setLayout(new BorderLayout());
        {
            Box horizontalBox = Box.createHorizontalBox();
            horizontalBox.setBorder(new EmptyBorder(0, 10, 0, 0));
            {
                var leftToolBar = new FlatToolBar();
                var leftToolBarLayout = new FlowLayout();
                leftToolBarLayout.setAlignment(FlowLayout.RIGHT);
                leftToolBar.setLayout(leftToolBarLayout);
                //host info
                var hostInfo = new JLabel("127.0.0.1:6379 - 单机模式");

                leftToolBar.add(hostInfo);
                horizontalBox.add(hostInfo);
            }

            horizontalBox.add(Box.createHorizontalGlue());

            {
                var rightToolBar = new FlatToolBar();
                var rightToolBarLayout = new FlowLayout();
                rightToolBarLayout.setAlignment(FlowLayout.RIGHT);
                rightToolBar.setLayout(rightToolBarLayout);
                var info = new FlatLabel();
                info.setText("V " + Const.APP_VERSION);
                info.setToolTipText("Version ".concat(Const.APP_VERSION));
                info.setIcon(UI.INFO_ICON);
                rightToolBar.add(info);
                horizontalBox.add(rightToolBar);
            }


            var topPanel = new JPanel(new BorderLayout());

            topPanel.add(horizontalBox, BorderLayout.CENTER);
            add(topPanel, BorderLayout.SOUTH);
        }


        {
            {
                contentPanel = new JTabbedPane() {
                    @Override
                    public void updateUI() {
                        super.updateUI();
                        var flatLineBorder = new FlatLineBorder(new Insets(2, 0, 0, 0), UIManager.getColor("Component.borderColor"));
                        setBorder(flatLineBorder);
                    }
                };
                contentPanel.setTabPlacement(JTabbedPane.TOP);
                contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT, SwingConstants.RIGHT);
                contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS, true);
                contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
                contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT, FlatClientProperties.TABBED_PANE_ALIGN_CENTER);
                contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TAB_TYPE, FlatClientProperties.TABBED_PANE_TAB_TYPE_CARD);
                {
                    var leftToolBar = new FlatToolBar();
                    leftToolBar.setLayout(new BorderLayout());
                    leftToolBar.setPreferredSize(new Dimension(50, -1));

                    //host info
                    var closeDrawerBtn = new JButton(UI.DRAWER_SHOW_OR_CLOSE_ICON);
                    closeDrawerBtn.addActionListener(action);
                    action.setBeforeProcess(state -> closeDrawerBtn.setVisible(false));
                    action.setAfterProcess(state ->{
                        if(state){
                            leftToolBar.setMargin(new Insets(0,65,0,0));
                        }else {
                            leftToolBar.setMargin(new Insets(0,0,0,0));
                        }
                        closeDrawerBtn.setVisible(true);
                    });
                    leftToolBar.add(closeDrawerBtn, BorderLayout.WEST);
                    contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_LEADING_COMPONENT, leftToolBar);
                }

                {
                    //host info
                    var middleToolBar = new FlatToolBar();
                    var rightToolBarLayout = new FlowLayout();
                    rightToolBarLayout.setAlignment(FlowLayout.CENTER);
                    middleToolBar.setLayout(rightToolBarLayout);

                    //cupInfo
                    var cupInfo = new FlatLabel();
                    cupInfo.setText("0");
                    cupInfo.setIcon(UI.CONTENT_TAB_CPU_ICON);
                    middleToolBar.add(cupInfo);
                    middleToolBar.add(new JToolBar.Separator());
                    //memoryInfo
                    var memoryInfo = new FlatLabel();
                    memoryInfo.setText("0.0");
                    memoryInfo.setIcon(UI.CONTENT_TAB_MEMORY_ICON);
                    middleToolBar.add(memoryInfo);
                    middleToolBar.add(new JToolBar.Separator());


//                    contentPanel.putClientProperty(FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT, middleToolBar);
                }

            }
            {
                //主窗口
                contentPanel.addTab("主页", UI.CONTENT_TAB_DATA_ICON, new JPanel());
                //命令窗口
                contentPanel.addTab("命令", UI.CONTENT_TAB_COMMAND_ICON, new JPanel());
                contentPanel.addTab("订阅", UI.MQ_ICON, new JPanel());
                //数据窗口
                contentPanel.addTab("数据", UI.CONTENT_TAB_INFO_ICON, new JPanel());



            }

            add(contentPanel, BorderLayout.CENTER);
        }

    }

}
