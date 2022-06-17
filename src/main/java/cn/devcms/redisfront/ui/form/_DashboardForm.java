package cn.devcms.redisfront.ui.form;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class _DashboardForm {
    private JPanel contentPanel;

    public JPanel getContentPanel() {
        return contentPanel;
    }

    private void createUIComponents() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        RSyntaxTextArea textArea = new RSyntaxTextArea(100, 100);
        textArea.setCaretColor(Color.GRAY);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        contentPanel.add(sp);

    }
}
