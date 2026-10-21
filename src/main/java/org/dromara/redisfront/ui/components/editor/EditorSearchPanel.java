package org.dromara.redisfront.ui.components.editor;

import com.formdev.flatlaf.FlatClientProperties;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Function;
import java.util.regex.PatternSyntaxException;

/** Small search UI backed by the editor's native search and atomic replacement support. */
final class EditorSearchPanel extends JPanel {
    private final RSyntaxTextArea editor;
    private final JTextField findField = new JTextField(16);
    private final JTextField replaceField = new JTextField(16);
    private final JCheckBox matchCase = new JCheckBox();
    private final JCheckBox regex = new JCheckBox();
    private final JLabel status = new JLabel();
    private final JToggleButton replacementToggle = new JToggleButton();
    private final JPanel replaceRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
    private final JButton replaceButton;
    private final JButton replaceAllButton;
    private final Function<String, String> translate;

    EditorSearchPanel(RSyntaxTextArea editor, Function<String, String> translate, Runnable close) {
        super(new BorderLayout(0, 4));
        this.editor = editor;
        this.translate = translate;
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        var findRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        findField.setToolTipText(tr("find"));
        findField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, tr("find"));
        findRow.add(findField);
        findRow.add(button("previous", () -> search(false, false, false)));
        findRow.add(button("next", () -> search(true, false, false)));
        matchCase.setText("Aa");
        matchCase.setToolTipText(tr("matchCase"));
        regex.setText(".*");
        regex.setToolTipText(tr("regex"));
        findRow.add(matchCase);
        findRow.add(regex);
        replacementToggle.setText(tr("replace"));
        replacementToggle.addActionListener(e -> {
            replaceRow.setVisible(replacementToggle.isSelected());
            revalidate();
        });
        findRow.add(replacementToggle);
        var closeButton = new JButton("×");
        closeButton.setToolTipText(tr("close"));
        closeButton.addActionListener(e -> close.run());
        findRow.add(closeButton);
        add(findRow, BorderLayout.NORTH);

        replaceField.setToolTipText(tr("replacement"));
        replaceField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, tr("replacement"));
        replaceRow.add(replaceField);
        replaceButton = button("replace", () -> search(true, true, false));
        replaceAllButton = button("replaceAll", () -> search(true, true, true));
        replaceRow.add(replaceButton);
        replaceRow.add(replaceAllButton);
        add(replaceRow, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        findField.addActionListener(e -> search(true, false, false));
        replaceField.addActionListener(e -> search(true, true, false));
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { close.run(); }
        });
    }

    private String tr(String key) {
        return translate.apply("DataViewForm.search." + key);
    }

    private JButton button(String key, Runnable action) {
        var button = new JButton(tr(key));
        button.addActionListener(e -> action.run());
        return button;
    }

    void open(boolean replace) {
        String selected = editor.getSelectedText();
        if (selected != null && !selected.contains("\n")) {
            findField.setText(selected);
        }
        replaceRow.setVisible(replace);
        replacementToggle.setSelected(replace);
        refreshEditableState();
        status.setText("");
        setVisible(true);
        findField.requestFocusInWindow();
        findField.selectAll();
    }

    void refreshEditableState() {
        replaceField.setEnabled(editor.isEditable());
        replaceButton.setEnabled(editor.isEditable());
        replaceAllButton.setEnabled(editor.isEditable());
    }

    private void search(boolean forward, boolean replace, boolean all) {
        if (findField.getText().isEmpty() || replace && !editor.isEditable()) {
            return;
        }
        var context = new SearchContext(findField.getText());
        context.setMatchCase(matchCase.isSelected());
        context.setRegularExpression(regex.isSelected());
        context.setSearchWrap(true);
        context.setSearchForward(forward);
        context.setMarkAll(true);
        context.setReplaceWith(replaceField.getText());
        try {
            SearchResult result = replace
                    ? (all ? SearchEngine.replaceAll(editor, context) : SearchEngine.replace(editor, context))
                    : SearchEngine.find(editor, context);
            status.setText(result.wasFound()
                    ? String.format(tr(replace ? "replaced" : "found"), replace ? result.getCount() : result.getMarkedCount())
                    : tr("notFound"));
        } catch (PatternSyntaxException | IndexOutOfBoundsException e) {
            status.setText(tr("invalidPattern"));
        } catch (IllegalArgumentException e) {
            status.setText(tr("invalidReplacement"));
        }
    }
}
