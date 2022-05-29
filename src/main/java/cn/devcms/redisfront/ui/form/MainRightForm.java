package cn.devcms.redisfront.ui.form;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class MainRightForm {

    private JPanel contentPanel;
    private JTabbedPane tabbedPane1;

    public JPanel getContentPanel() {
        return contentPanel;
    }


    private void createUIComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new MigLayout(
                "fill,insets panel,hidemode 3",
                // columns
                "[fill]",
                // rows
                "[fill]"));
        tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1);
    }
}
