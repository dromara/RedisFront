package cn.devcms.redisfront.ui.form;

import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.UnitValue;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ControlBar {
    private JPanel contentPanel;
    private JButton button1;


    public JPanel getPanel1() {
        return contentPanel;
    }

    private void createUIComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new MigLayout(
                "insets dialog",
                // columns
                "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[grow,fill]" +
                        "[button,fill]",
                // rows
                "[bottom]" +
                        "[]"));
        MigLayout layout = (MigLayout)contentPanel.getLayout();
        LC lc = ConstraintParser.parseLayoutConstraint( (String) layout.getLayoutConstraints() );
        UnitValue[] insets = lc.getInsets();
        lc.setInsets( new UnitValue[] {
                new UnitValue( 0, UnitValue.PIXEL, null ),
                insets[1],
                insets[2],
                insets[3]
        } );
        layout.setLayoutConstraints( lc );
    }
}
