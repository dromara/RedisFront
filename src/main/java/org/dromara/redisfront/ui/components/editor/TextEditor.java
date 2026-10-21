package org.dromara.redisfront.ui.components.editor;


import cn.hutool.json.JSONUtil;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.formdev.flatlaf.util.SystemInfo;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * TextEditorComponent
 *
 * @author Jin
 */
public class TextEditor extends JPanel {

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final Timer highlightTimer;
    private HighlightMode highlightMode = HighlightMode.AUTO;
    private boolean syntaxHighlightingSupported = true;
    private JMenu highlightMenu;
    private final Action undoAction;
    private final Action redoAction;
    private final List<Runnable> textChangeListeners = new ArrayList<>();
    private boolean changeNotificationPending;
    private EditorSearchPanel searchPanel;
    private Function<String, String> translate;

    private enum HighlightMode {
        AUTO, TEXT, JSON
    }


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
        textArea.beginAtomicEdit();
        try {
            textArea.replaceRange(text == null ? "" : text, 0, textArea.getDocument().getLength());
        } finally {
            textArea.endAtomicEdit();
        }
        refreshSyntaxHighlighting();
        refreshEditActions();
    }

    public void loadText(String text) {
        setText(text);
        textArea.discardAllEdits();
        textArea.setCaretPosition(0);
        refreshEditActions();
    }

    public String getText() {
        return this.textArea.getText();
    }

    public void setEditable(boolean editable) {
        this.textArea.setEditable(editable);
        refreshEditActions();
        if (searchPanel != null) {
            searchPanel.refreshEditableState();
        }
    }

    public Action getUndoAction() {
        return undoAction;
    }

    public Action getRedoAction() {
        return redoAction;
    }

    public void addTextChangeListener(Runnable listener) {
        textChangeListeners.add(listener);
    }

    private void refreshEditActions() {
        undoAction.setEnabled(textArea.isEditable() && textArea.canUndo());
        redoAction.setEnabled(textArea.isEditable() && textArea.canRedo());
    }

    private void notifyTextChanged() {
        if (changeNotificationPending) {
            return;
        }
        changeNotificationPending = true;
        SwingUtilities.invokeLater(() -> {
            changeNotificationPending = false;
            refreshEditActions();
            textChangeListeners.forEach(Runnable::run);
        });
    }

    public void installEditingTools(Function<String, String> translate) {
        this.translate = translate;
        refreshActionLabels();
        int modifier = SystemInfo.isMacOS ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        var findAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearch(false);
            }
        };
        var replaceAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSearch(true);
            }
        };
        bindAction("editor.find", KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier), findAction);
        bindAction("editor.replace", KeyStroke.getKeyStroke(KeyEvent.VK_H, modifier), replaceAction);
        bindAction("editor.undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier), undoAction);
        bindAction("editor.redo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifier), redoAction);
        bindAction("editor.redoAlternate", KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier | InputEvent.SHIFT_DOWN_MASK), redoAction);
    }

    private void bindAction(String key, KeyStroke shortcut, Action action) {
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shortcut, key);
        getActionMap().put(key, action);
        textArea.getInputMap(WHEN_FOCUSED).put(shortcut, key);
        textArea.getActionMap().put(key, action);
    }

    private void refreshActionLabels() {
        if (translate != null) {
            undoAction.putValue(Action.NAME, translate.apply("DataViewForm.undo.title"));
            undoAction.putValue(Action.SHORT_DESCRIPTION, translate.apply("DataViewForm.undo.tooltip"));
            redoAction.putValue(Action.NAME, translate.apply("DataViewForm.redo.title"));
            redoAction.putValue(Action.SHORT_DESCRIPTION, translate.apply("DataViewForm.redo.tooltip"));
        }
    }

    public void showSearch(boolean replace) {
        if (searchPanel == null) {
            searchPanel = new EditorSearchPanel(textArea, translate, () -> {
                searchPanel.setVisible(false);
                textArea.clearMarkAllHighlights();
                revalidate();
                textArea.requestFocusInWindow();
            });
            add(searchPanel, BorderLayout.NORTH);
        }
        searchPanel.open(replace);
        revalidate();
    }

    public void setSyntaxHighlightingSupported(boolean supported) {
        syntaxHighlightingSupported = supported;
        if (highlightMenu != null) {
            highlightMenu.setEnabled(supported);
        }
        refreshSyntaxHighlighting();
    }

    public void installSyntaxHighlightingMenu(Function<String, String> translate) {
        highlightMenu = new JMenu() {
            @Override
            public void updateUI() {
                super.updateUI();
                setText(translate.apply("DataViewForm.highlightMenu.label"));
            }
        };
        var group = new ButtonGroup();
        for (HighlightMode mode : HighlightMode.values()) {
            var item = new JRadioButtonMenuItem() {
                @Override
                public void updateUI() {
                    super.updateUI();
                    setText(translate.apply("DataViewForm.highlightMenu." + mode.name().toLowerCase(Locale.ROOT)));
                }
            };
            item.setSelected(mode == highlightMode);
            item.addActionListener(ignore -> setHighlightMode(mode));
            group.add(item);
            highlightMenu.add(item);
        }
        highlightMenu.setEnabled(syntaxHighlightingSupported);
        var popup = textArea.getPopupMenu();
        popup.addSeparator();
        popup.add(highlightMenu);
    }

    private void setHighlightMode(HighlightMode mode) {
        highlightMode = mode;
        refreshSyntaxHighlighting();
    }

    private void refreshSyntaxHighlighting() {
        highlightTimer.stop();
        boolean json = syntaxHighlightingSupported && (highlightMode == HighlightMode.JSON
                || highlightMode == HighlightMode.AUTO && JSONUtil.isTypeJSON(textArea.getText()));
        String style = json ? SyntaxConstants.SYNTAX_STYLE_JSON : SyntaxConstants.SYNTAX_STYLE_NONE;
        if (!style.equals(textArea.getSyntaxEditingStyle())) {
            textArea.setSyntaxEditingStyle(style);
        }
    }

    /**
     * 切换自动换行。关闭时长行靠横向滚动查看，打开后按单词边界折行。
     */
    public void setLineWrap(boolean wrap) {
        this.textArea.setLineWrap(wrap);
        this.textArea.setWrapStyleWord(wrap);
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
        undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textArea.isEditable() && textArea.canUndo()) {
                    textArea.undoLastAction();
                    refreshEditActions();
                }
            }
        };
        redoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textArea.isEditable() && textArea.canRedo()) {
                    textArea.redoLastAction();
                    refreshEditActions();
                }
            }
        };
        refreshEditActions();
        this.setForeground(UIManager.getColor("foreground"));
        this.textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        this.highlightTimer = new Timer(150, ignore -> refreshSyntaxHighlighting());
        this.highlightTimer.setRepeats(false);
        this.textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                highlightTimer.restart();
                notifyTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                highlightTimer.restart();
                notifyTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
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
            refreshActionLabels();
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
