package cn.devcms.redisfront.ui.form;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

public class MainLeftForm {
    private JPanel contentPanel;
    private JTabbedPane tabbedPane1;
    private JButton button1;
    private JButton button2;
    private JTree tree1;
    private JButton addBtn;
    private JLabel iconBtn;
    private JButton refreshBtn;


    public JPanel getContentPanel() {
        return contentPanel;
    }

    private void createUIComponents() {
        contentPanel = new JPanel();
        iconBtn = new JLabel();
        iconBtn.setIcon(new FlatSVGIcon("icons/Project.svg"));
        addBtn = new JButton();
        addBtn.setIcon(new FlatSVGIcon("icons/add.svg"));
        refreshBtn = new JButton();
        refreshBtn.setIcon(new FlatSVGIcon("icons/refresh.svg"));
    }

}
