package cn.devcms.redisfront.ui.form;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;

/**
 * ConnectForm
 *
 * @author Jin
 */
public class _NoneForm {
    private JPanel contentPanel;
    private JLabel logoLabel;
    private JLabel newLabel;
    private JLabel openLabel;
    private JLabel newKeyLabel;
    private JLabel openKeyLabel;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public _NoneForm() {
        newLabel.putClientProperty("FlatLaf.styleClass", "h3");
        newKeyLabel.putClientProperty("FlatLaf.styleClass", "h3");
        openLabel.putClientProperty("FlatLaf.styleClass", "h3");
        openKeyLabel.putClientProperty("FlatLaf.styleClass", "h3");
    }

    private void createUIComponents() {
        logoLabel = new JLabel(new FlatSVGIcon("svg/redis_64.svg"));
    }
}
