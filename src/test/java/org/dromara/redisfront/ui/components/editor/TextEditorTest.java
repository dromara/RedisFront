package org.dromara.redisfront.ui.components.editor;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.dromara.redisfront.commons.codec.RedisValueCodec;
import org.dromara.redisfront.commons.codec.ValueViewType;
import org.dromara.redisfront.commons.constant.Constants;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TextEditorTest {

    @BeforeAll
    static void setupTheme() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            FlatLaf.registerCustomDefaultsSource(Constants.APP_THEME_PACKAGE);
            FlatMacLightLaf.setup();
        });
    }

    private static RSyntaxTextArea textArea(TextEditor editor) {
        return (RSyntaxTextArea) ((RTextScrollPane) editor.getComponent(0)).getTextArea();
    }

    private static JMenu highlightMenu(TextEditor editor) {
        var popup = textArea(editor).getPopupMenu();
        return (JMenu) popup.getComponent(popup.getComponentCount() - 1);
    }

    private static void installMenu(TextEditor editor) {
        var bundle = ResourceBundle.getBundle("org.dromara.redisfront.RedisFront", Locale.ROOT);
        editor.installSyntaxHighlightingMenu(bundle::getString);
    }

    @Test
    void changingHighlightingPreservesTextAndSavedBytes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = new TextEditor();
            int originalPopupItems = textArea(editor).getPopupMenu().getComponentCount();
            installMenu(editor);
            assertEquals(originalPopupItems + 2, textArea(editor).getPopupMenu().getComponentCount());

            for (String value : new String[]{"", "111111", " { \"a\": 1 } \n", "[1, 2]", "\"{\\\"a\\\":1}\""}) {
                editor.setText(value);
                var menu = highlightMenu(editor);
                for (int index : new int[]{2, 1, 0}) {
                    menu.getItem(index).doClick(0);
                    assertEquals(value, editor.getText());
                    assertArrayEquals(value.getBytes(StandardCharsets.UTF_8),
                            RedisValueCodec.decode(editor.getText(), ValueViewType.UTF8));
                }
            }
        });
    }

    @Test
    void autoHighlightingFollowsTheCurrentValueWithoutReformatting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = new TextEditor();
            editor.setText("{\"a\":1}");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_JSON, textArea(editor).getSyntaxEditingStyle());
            assertEquals("{\"a\":1}", editor.getText());
            editor.setText("1111");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE, textArea(editor).getSyntaxEditingStyle());
            editor.setText("\"{\\\"a\\\":1}\"");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE, textArea(editor).getSyntaxEditingStyle());
            editor.setText("[1, 2]");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_JSON, textArea(editor).getSyntaxEditingStyle());
        });
    }

    @Test
    void typingUpdatesAutoHighlighting() throws Exception {
        var highlighted = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            var editor = new TextEditor();
            var area = textArea(editor);
            area.addPropertyChangeListener(RSyntaxTextArea.SYNTAX_STYLE_PROPERTY, event -> {
                if (SyntaxConstants.SYNTAX_STYLE_JSON.equals(event.getNewValue())) {
                    highlighted.countDown();
                }
            });
            area.setText("{\"typed\":true}");
        });
        assertTrue(highlighted.await(3, TimeUnit.SECONDS), "Typing should update highlighting automatically");
    }

    @Test
    void binaryViewsDisableHighlightingAndRetainTheManualPreference() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = new TextEditor();
            installMenu(editor);
            var menu = highlightMenu(editor);
            editor.setText("{\"a\":1}");
            menu.getItem(1).doClick(0);
            assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE, textArea(editor).getSyntaxEditingStyle());
            menu.getItem(2).doClick(0);
            editor.setSyntaxHighlightingSupported(false);
            assertFalse(menu.isEnabled());
            assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE, textArea(editor).getSyntaxEditingStyle());
            editor.setSyntaxHighlightingSupported(true);
            assertTrue(menu.isEnabled());
            assertTrue(menu.getItem(2).isSelected());
            assertEquals(SyntaxConstants.SYNTAX_STYLE_JSON, textArea(editor).getSyntaxEditingStyle());
            assertEquals("{\"a\":1}", editor.getText());
        });
    }

    @Test
    void formatIsOneNativeUndoStepAndLoadingAnotherValueClearsHistory() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = new TextEditor();
            editor.loadText("{\"a\":1}");
            assertFalse(editor.getUndoAction().isEnabled());
            String formatted = "{\n  \"a\": 1\n}";
            editor.setText(formatted);
            assertTrue(editor.getUndoAction().isEnabled());
            editor.getUndoAction().actionPerformed(new ActionEvent(editor, 0, "undo"));
            assertEquals("{\"a\":1}", editor.getText());
            assertTrue(editor.getRedoAction().isEnabled());
            editor.getRedoAction().actionPerformed(new ActionEvent(editor, 0, "redo"));
            assertEquals(formatted, editor.getText());
            editor.loadText("different-key");
            assertFalse(editor.getUndoAction().isEnabled());
            assertFalse(editor.getRedoAction().isEnabled());
            editor.getUndoAction().actionPerformed(new ActionEvent(editor, 0, "undo"));
            assertEquals("different-key", editor.getText());
        });
    }

    private static <T> List<T> descendants(Container parent, Class<T> type) {
        var matches = new ArrayList<T>();
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) matches.add(type.cast(child));
            if (child instanceof Container container) matches.addAll(descendants(container, type));
        }
        return matches;
    }

    private static JButton button(Container parent, String label) {
        return descendants(parent, JButton.class).stream()
                .filter(b -> label.equals(b.getText())).findFirst().orElseThrow();
    }

    private static JPanel searchPanel(TextEditor editor) {
        return (JPanel) ((BorderLayout) editor.getLayout()).getLayoutComponent(BorderLayout.NORTH);
    }

    private static TextEditor searchableEditor(String value) {
        var editor = new TextEditor();
        var bundle = ResourceBundle.getBundle("org.dromara.redisfront.RedisFront", Locale.ROOT);
        editor.installEditingTools(bundle::getString);
        editor.loadText(value);
        return editor;
    }

    @Test
    void nativeReplaceAllIsUndoableAndInvalidRegexPreservesTheValue() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = searchableEditor("red blue red");
            editor.showSearch(true);
            var panel = searchPanel(editor);
            var fields = descendants(panel, JTextField.class);
            fields.get(0).setText("red");
            fields.get(1).setText("green");
            button(panel, "Replace all").doClick(0);
            assertEquals("green blue green", editor.getText());
            editor.getUndoAction().actionPerformed(new ActionEvent(editor, 0, "undo"));
            assertEquals("red blue red", editor.getText());
            fields.get(0).setText("[");
            descendants(panel, JCheckBox.class).stream().filter(c -> ".*".equals(c.getText())).findFirst().orElseThrow().setSelected(true);
            assertDoesNotThrow(() -> button(panel, "Replace all").doClick(0));
            assertEquals("red blue red", editor.getText());
        });
    }

    @Test
    void readOnlyViewAllowsFindButDisablesReplacementAndUndo() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = searchableEditor("red blue red");
            editor.setEditable(false);
            editor.showSearch(true);
            var panel = searchPanel(editor);
            descendants(panel, JTextField.class).getFirst().setText("blue");
            button(panel, "Next").doClick(0);
            assertEquals("blue", textArea(editor).getSelectedText());
            assertFalse(button(panel, "Replace").isEnabled());
            assertFalse(button(panel, "Replace all").isEnabled());
            assertFalse(editor.getUndoAction().isEnabled());
            assertFalse(editor.getRedoAction().isEnabled());
            assertEquals("red blue red", editor.getText());
        });
    }

    @Test
    void searchShortcutsAreInstalledOnTheActualTextArea() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var editor = searchableEditor("sample");
            var area = textArea(editor);
            int modifier = SystemInfo.isMacOS ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
            Object findKey = area.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier));
            area.getActionMap().get(findKey).actionPerformed(new ActionEvent(area, 0, "find"));
            assertTrue(searchPanel(editor).isVisible());
            Object replaceKey = area.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_H, modifier));
            area.getActionMap().get(replaceKey).actionPerformed(new ActionEvent(area, 0, "replace"));
            assertTrue(button(searchPanel(editor), "Replace all").getParent().isVisible());
        });
    }
}
