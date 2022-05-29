package cn.devcms.redisfront.ui.form;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;

public class MainLeftForm {
    private JPanel contentPanel;
    private JButton addBtn;
    private JLabel iconBtn;
    private JButton refreshBtn;
    private JTree tree1;


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
