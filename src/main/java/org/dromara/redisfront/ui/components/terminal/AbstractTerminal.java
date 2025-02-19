
package org.dromara.redisfront.ui.components.terminal;

import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.jdesktop.swingx.JXList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

public abstract class AbstractTerminal extends JPanel implements KeyListener, CaretListener {
    protected final JTextArea terminal;
    private int lastSelectionStart = 0;
    private int currentCaretPosition = -1;
    private int currentKeyCode = 0;
    private boolean allowInputFlag = false;
    private boolean shouldConsumeEvent = false;
    private final ArrayList<String> commandHistory;
    private int commandHistoryIndex;

    private JWindow suggestionWindow;
    private JXList<String> suggestionList;
    private DefaultListModel<String> suggestionModel;
    private final String[] redisCommands = {
            "SET", "GET", "DEL", "EXISTS", "EXPIRE",
            "HGETALL", "HSET", "LPUSH", "RPOP", "SADD"
    };

    public AbstractTerminal() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        terminal = new JTextArea();
        terminal.setBackground(UIManager.getColor("TextArea.background"));
        terminal.addKeyListener(this);
        terminal.addCaretListener(this);
        commandHistory = new ArrayList<>();
        commandHistoryIndex = 0;
        add(new JScrollPane(terminal), BorderLayout.CENTER);
        initSuggestionComponents();
    }

    protected abstract void inputProcessHandler(String input);

    protected abstract RedisConnectContext connectInfo();

    protected abstract String databaseName();

    private String buildPrompt() {
        RedisConnectContext context = connectInfo();
        return context.getHost().concat(":")
                .concat(String.valueOf(context.getPort()))
                .concat(RedisFrontUtils.equal("0", databaseName()) ? "" : " [" + databaseName() + "] ")
                .concat(">");
    }

    protected void println(String message) {
        terminal.append(message);
        terminal.append("\n");
        lastSelectionStart = terminal.getText().length();
        terminal.setCaretPosition(lastSelectionStart);
    }

    protected void print(String message) {
        terminal.append(message);
        lastSelectionStart = terminal.getText().length();
        terminal.setCaretPosition(lastSelectionStart);
    }

    protected void printConnectedSuccessMessage() {
        this.println("");
        this.println("connection " + connectInfo().getHost() + ":" + connectInfo().getPort() + " redis server success...");
        this.println("");
        this.print(buildPrompt());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (shouldConsumeEvent) {
            e.consume();
            return;
        }
        if (currentKeyCode == KeyEvent.VK_ENTER && e.getKeyChar() == '\n') {
            int subStartLength = lastSelectionStart;
            int subEndLength = terminal.getText().length();

            if (subStartLength < subEndLength) {
                String input = terminal.getText().substring(subStartLength, subEndLength);
                if (RedisFrontUtils.isNotEmpty(input)) {
                    if (input.contains("\n") && !input.endsWith("\n")) {
                        String oldText = input;
                        input = input.replace("\n", "").concat("\n");
                        terminal.setText(terminal.getText().replace(oldText, input));
                    } else {
                        input = input.concat("\n");
                    }

                    String text = input.replace("\n", "");
                    if (RedisFrontUtils.isNotEmpty(text)) {
                        commandHistory.add(text);
                        commandHistoryIndex = commandHistory.size();
                    }

                    this.inputProcessHandler(input.trim());
                }
            }
            this.print("\n");
            this.print(buildPrompt());
        } else if (currentKeyCode == KeyEvent.VK_ENTER) {
            e.consume();
            terminal.setText(terminal.getText().concat(String.valueOf(e.getKeyChar())));
        }
    }

    private void updateCommandFromHistory(int indexOffset) {
        if (commandHistory.isEmpty()) return;

        int newIndex = commandHistoryIndex + indexOffset;
        newIndex = Math.max(newIndex, 0); // 防止负数索引
        newIndex = Math.min(newIndex, commandHistory.size());

        String input = terminal.getText().substring(0, lastSelectionStart);
        if (newIndex < commandHistory.size()) {
            commandHistoryIndex = newIndex;
            terminal.setText(input.concat(commandHistory.get(commandHistoryIndex)));
        } else {
            commandHistoryIndex = commandHistory.size();
            terminal.setText(input);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        currentKeyCode = e.getKeyCode();
        shouldConsumeEvent = (currentKeyCode == KeyEvent.VK_BACK_SPACE
                || currentKeyCode == KeyEvent.VK_DOWN
                || currentKeyCode == KeyEvent.VK_LEFT)
                && currentCaretPosition <= lastSelectionStart;

        switch (currentKeyCode) {
            case KeyEvent.VK_UP:
                e.consume();
                shouldConsumeEvent = true;
                updateCommandFromHistory(-1);
                break;
            case KeyEvent.VK_DOWN:
                e.consume();
                shouldConsumeEvent = true;
                updateCommandFromHistory(1);
                break;
        }

        if (allowInputFlag) {
            shouldConsumeEvent = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (shouldConsumeEvent) {
            e.consume();
        }
        if (currentKeyCode == KeyEvent.VK_TAB) {
            e.consume();
            if (!suggestionWindow.isVisible()) {
                String currentInput = getCurrentInputLine().trim();
                if (!currentInput.isEmpty()) {
                    showSuggestions(currentInput);
                }
            }
        } else if (currentKeyCode == KeyEvent.VK_ENTER && suggestionWindow.isVisible()) {
            insertSelectedSuggestion();
            e.consume();
        } else if (currentKeyCode == KeyEvent.VK_UP && suggestionWindow.isVisible()) {
            int newIndex = Math.max(suggestionList.getSelectedIndex() - 1, 0);
            suggestionList.setSelectedIndex(newIndex);
            e.consume();
        } else if (currentKeyCode == KeyEvent.VK_DOWN && suggestionWindow.isVisible()) {
            int newIndex = Math.min(suggestionList.getSelectedIndex() + 1, suggestionModel.getSize() - 1);
            suggestionList.setSelectedIndex(newIndex);
            e.consume();
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        currentCaretPosition = e.getDot();
        allowInputFlag = (currentCaretPosition >= lastSelectionStart);
        if (currentCaretPosition < lastSelectionStart) {
            SwingUtilities.invokeLater(() -> terminal.setCaretPosition(lastSelectionStart));
        }
        if (suggestionWindow.isVisible() &&
                e.getDot() < lastSelectionStart) {
            suggestionWindow.setVisible(false);
        }
    }

    private void initSuggestionComponents() {
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JXList<>(suggestionModel);
        suggestionList.setCellRenderer(new DefaultListCellRenderer());
        suggestionWindow = new JWindow();
        suggestionWindow.getContentPane().add(new JScrollPane(suggestionList));
        suggestionWindow.setSize(200, 150);
        suggestionWindow.setFocusableWindowState(false);
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                insertSelectedSuggestion();
            }
        });
    }

    // 新增自动完成逻辑
    private void showSuggestions(String inputPrefix) {
        suggestionModel.clear();
        String upperPrefix = inputPrefix.toUpperCase();
        for (String cmd : redisCommands) {
            if (cmd.startsWith(upperPrefix)) {
                suggestionModel.addElement(cmd);
            }
        }

        if (!suggestionModel.isEmpty()) {
            // 动态计算窗口尺寸
            int width = suggestionList.getPreferredSize().width + 20;
            int height = Math.min(suggestionList.getPreferredSize().height, 300);
            suggestionWindow.setSize(width, height);

            Point caretPos = terminal.getCaret().getMagicCaretPosition();
            if (caretPos != null) {
                Point screenPos = terminal.getLocationOnScreen();
                suggestionWindow.setLocation(
                        screenPos.x + caretPos.x,
                        screenPos.y + caretPos.y + terminal.getFont().getSize()
                );
                suggestionWindow.setVisible(true);
                suggestionList.setSelectedIndex(0);
            }
        } else {
            suggestionWindow.setVisible(false);
        }
    }

    private void insertSelectedSuggestion() {
        String selected = suggestionList.getSelectedValue();
        if (selected != null) {
            String fullText = terminal.getText();
            // 精确获取当前输入行（到第一个换行符为止）
            int lineEnd = fullText.indexOf('\n', lastSelectionStart);
            lineEnd = lineEnd == -1 ? fullText.length() : lineEnd;
            String currentLine = fullText.substring(lastSelectionStart, lineEnd);

            // 构建智能替换内容
            String newContent;
            if (currentLine.isEmpty()) {
                newContent = selected;
            } else {
                // 保留用户已输入部分的大小写
                newContent = selected.startsWith(currentLine.toUpperCase())
                        ? selected
                        : currentLine + selected.substring(currentLine.length());
            }

            // 精准替换当前行内容
            String newText = fullText.substring(0, lastSelectionStart)
                    + newContent
                    + fullText.substring(lineEnd);

            SwingUtilities.invokeLater(() -> {
                terminal.setText(newText);
                // 更新光标位置到新内容末尾
                int newCaretPos = lastSelectionStart + newContent.length();
                terminal.setCaretPosition(newCaretPos);
                suggestionWindow.setVisible(false);
            });
        }
    }

    private String getCurrentInputLine() {
        return terminal.getText().substring(lastSelectionStart);
    }
}
