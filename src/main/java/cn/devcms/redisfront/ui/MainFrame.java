package cn.devcms.redisfront.ui;

import cn.devcms.redisfront.ui.form.ControlBar;
import cn.devcms.redisfront.ui.form.MainLeftForm;
import cn.devcms.redisfront.ui.form.MainRightForm;
import com.formdev.flatlaf.extras.FlatDesktop;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;
import com.formdev.flatlaf.extras.components.FlatButton;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXFrame;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JXFrame {
    JSplitPane mainSplitPane;
    MainRightForm mainRightForm;
    MainLeftForm mainLeftForm;
    ControlBar controlBar;

    public MainFrame() {
        super(" RedisFront ", true);
        setIconImages(FlatSVGUtils.createWindowIconImages("/svg/redis.svg"));
        menuBarInit();
        initComponents();
        FlatDesktop.setQuitHandler(FlatDesktop.QuitResponse::performQuit);
    }

    private void initComponents() {
        Container container = getContentPane();
        container.setLayout(new MigLayout(
                "fill,insets panel,hidemode 3",
                // columns
                "[fill]",
                // rows
                "[fill]"));
        mainSplitPane = new JSplitPane();
        mainLeftForm = new MainLeftForm();
        mainSplitPane.setLeftComponent(mainLeftForm.getContentPanel());
        mainRightForm = new MainRightForm();
        mainSplitPane.setRightComponent(mainRightForm.getContentPanel());
        container.add(mainSplitPane);
    }


    private void menuBarInit() {
        setJMenuBar(new JMenuBar() {
            {
                JMenu menuA = new JMenu("文件");
                menuA.setMnemonic('F');
                add(menuA);
                JMenu menuB = new JMenu("编辑");
                add(menuB);
                JMenu menuC = new JMenu("设置");
                add(menuC);
                JMenu menuD = new JMenu("关于");
                add(menuD);
                add(Box.createGlue());
                FlatButton giteeBtn = new FlatButton();

                giteeBtn.setIcon(new FlatSVGIcon("icons/gitee.svg"));
                giteeBtn.setButtonType(FlatButton.ButtonType.toolBarButton);
                giteeBtn.setFocusable(false);
                giteeBtn.setToolTipText(" 去码云给个star吧 :) ");
                giteeBtn.setRolloverIcon(new FlatSVGIcon("icons/gitee_red.svg"));
                giteeBtn.addActionListener(e -> JOptionPane.showMessageDialog(null, "Hello User! How are you?", "User", JOptionPane.INFORMATION_MESSAGE));
                add(giteeBtn);
            }
        });

    }


}
