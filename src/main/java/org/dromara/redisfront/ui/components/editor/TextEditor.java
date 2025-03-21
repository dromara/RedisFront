package org.dromara.redisfront.ui.components.editor;


import com.formdev.flatlaf.ui.FlatLineBorder;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.List;

/**
 * TextEditorComponent
 *
 * @author Jin
 */
public class TextEditor extends JPanel {

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;


    public static TextEditor newInstance() {
        return new TextEditor();
    }

    private class AsyncWriter extends SwingWorker<Void, String> {
        private final String text;

        private AsyncWriter(String text) {
            this.text = text;
        }

        @Override
        protected Void doInBackground() {
            if (text.length() < 500) {
                process(List.of(text));
            } else {
                for (int i = 0; i < text.length(); i += 500) {
                    String chunk = text.substring(i, Math.min(i + 500, text.length()));
                    publish(chunk);
                }
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String chunk : chunks) {
                textArea.append(chunk);
            }
        }
    }

    public void clear() {
        textArea.append("");
    }

    public void asyncAppendText(String text) {
        AsyncWriter asyncWriter = new AsyncWriter(text);
        asyncWriter.execute();
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public String getText() {
        return this.textArea.getText();
    }

    public TextEditor() {
        this.textArea = new RSyntaxTextArea() {
            {
                setRTextAreaUI(new RSyntaxTextAreaUI(this) {
                    @Override
                    protected void installDefaults() {
                        super.installDefaults();
                        JTextComponent editor = getComponent();
                        editor.setFont(UIManager.getFont("defaultFont"));
                    }
                });
            }
        };
        this.setForeground(UIManager.getColor("foreground"));
        this.textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        this.textArea.setLineWrap(false);
        this.scrollPane = new RTextScrollPane(this.textArea);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.scrollPane.setLineNumbersEnabled(false);
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
        this.updateTheme();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (textArea != null) {
            updateTheme();
        }
    }

    public void updateTheme() {
        var flatLineBorder = new FlatLineBorder(new Insets(1, 1, 1, 1), UIManager.getColor("Component.borderColor"));
        this.setBorder(flatLineBorder);
        var font = UIManager.getFont("defaultFont");
        this.textArea.setFont(font);
        this.textArea.setSyntaxScheme(new SyntaxScheme(font) {
            private void init(String key, int token) {
                var prefix = "FlatEditorPane.style.";
                var fg = UIManager.getColor(prefix + key);
                var bg = UIManager.getColor(prefix + key + ".background");
                this.getStyles()[token] = new Style(fg, bg, font);
            }

            {
                var styles = this.getStyles();
                for (int i = 0; i < styles.length; ++i) {
                    styles[i] = new Style(Color.red);
                }
                this.init("property", 20);
                this.init("variable", 17);
                this.init("number", 10);
                this.init("color", 12);
                this.init("string", 13);
                this.init("function", 8);
                this.init("type", 16);
                this.init("reservedWord", 6);
                this.init("literalBoolean", 9);
                this.init("operator", 23);
                this.init("separator", 22);
                this.init("whitespace", 21);
                this.init("comment", 1);
            }
        });
        this.textArea.setBackground(UIManager.getColor("FlatEditorPane.background"));
        this.textArea.setCaretColor(UIManager.getColor("FlatEditorPane.caretColor"));
        this.textArea.setSelectionColor(UIManager.getColor("FlatEditorPane.selectionBackground"));
        this.textArea.setCurrentLineHighlightColor(UIManager.getColor("FlatEditorPane.currentLineHighlight"));
        this.textArea.setMarkAllHighlightColor(UIManager.getColor("FlatEditorPane.markAllHighlightColor"));
        this.textArea.setMarkOccurrencesColor(UIManager.getColor("FlatEditorPane.markOccurrencesColor"));
        this.textArea.setMatchedBracketBGColor(UIManager.getColor("FlatEditorPane.matchedBracketBackground"));
        this.textArea.setMatchedBracketBorderColor(UIManager.getColor("FlatEditorPane.matchedBracketBorderColor"));
        this.textArea.setPaintMatchedBracketPair(true);
        this.textArea.setAnimateBracketMatching(false);
        var gutter = this.scrollPane.getGutter();
        gutter.setLineNumberFont(font);
        gutter.setBackground(UIManager.getColor("FlatEditorPane.gutter.background"));
        gutter.setBorderColor(UIManager.getColor("FlatEditorPane.gutter.borderColor"));
        gutter.setLineNumberColor(UIManager.getColor("FlatEditorPane.gutter.lineNumberColor"));
    }


}
