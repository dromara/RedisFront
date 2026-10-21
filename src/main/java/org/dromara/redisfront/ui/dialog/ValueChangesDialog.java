package org.dromara.redisfront.ui.dialog;

import org.dromara.redisfront.ui.components.editor.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public final class ValueChangesDialog {
    private ValueChangesDialog() {
    }

    public static void showDialog(Component owner, String key, String before, String after,
                                  Function<String, String> translate) {
        var split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                valuePanel(translate.apply("DataViewForm.changes.before"), before),
                valuePanel(translate.apply("DataViewForm.changes.after"), after));
        split.setResizeWeight(0.5);
        split.setDividerLocation(0.5);
        split.setPreferredSize(new Dimension(880, 440));
        var content = new JPanel(new BorderLayout(0, 8));
        content.add(new JLabel(translate.apply("DataViewForm.changes.note")), BorderLayout.NORTH);
        content.add(split, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.5));
        JOptionPane.showMessageDialog(owner, content,
                String.format(translate.apply("DataViewForm.changes.title"), key), JOptionPane.PLAIN_MESSAGE);
    }

    private static JPanel valuePanel(String title, String value) {
        var panel = new JPanel(new BorderLayout(0, 6));
        panel.add(new JLabel(title), BorderLayout.NORTH);
        var editor = new TextEditor();
        editor.loadText(value);
        editor.setEditable(false);
        panel.add(editor, BorderLayout.CENTER);
        return panel;
    }
}
