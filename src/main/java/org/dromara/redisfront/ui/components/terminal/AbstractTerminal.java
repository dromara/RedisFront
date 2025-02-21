
package org.dromara.redisfront.ui.components.terminal;

import cn.hutool.core.util.StrUtil;
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
import java.util.Arrays;

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
        terminal.setFont(UIManager.getFont("TextArea.font").deriveFont(15f));
        terminal.addKeyListener(this);
        terminal.addCaretListener(this);
        terminal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 获取当前光标位置
                int caretPos = terminal.getCaretPosition();
                // 当光标处于保护区域时阻止操作
                shouldConsumeEvent = caretPos <= lastSelectionStart;
                if (shouldConsumeEvent) {
                    terminal.setCaretPosition(Math.min(terminal.getText().length(), lastSelectionStart));
                }
            }
        });

        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("清空");
        menuItem.addActionListener(_ -> {
            terminal.setText("");
            this.print("\n");
            this.print(buildPrompt());
        });
        jPopupMenu.add(menuItem);
        terminal.setComponentPopupMenu(jPopupMenu);

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
        if (currentKeyCode == KeyEvent.VK_ENTER && suggestionWindow.isVisible()) {
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


    @Override
    public void keyPressed(KeyEvent e) {
        currentKeyCode = e.getKeyCode();
        if (currentKeyCode == KeyEvent.VK_TAB || suggestionWindow.isVisible()) {
            e.consume();
            shouldConsumeEvent = true;
            return;
        }
        if (currentKeyCode == KeyEvent.VK_BACK_SPACE || currentKeyCode == KeyEvent.VK_LEFT) {
            commandHistoryIndex = 0;
            // 获取当前光标位置
            int caretPos = terminal.getCaretPosition();
            // 当光标处于保护区域时阻止操作
            shouldConsumeEvent = caretPos <= lastSelectionStart;
            if (shouldConsumeEvent) {
                e.consume();
                return;
            }
        }

        shouldConsumeEvent = (currentKeyCode == KeyEvent.VK_BACK_SPACE
                && currentCaretPosition <= lastSelectionStart)
                || ((currentKeyCode == KeyEvent.VK_DOWN
                || currentKeyCode == KeyEvent.VK_LEFT)
                && currentCaretPosition <= lastSelectionStart);

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
            SwingUtilities.invokeLater(() -> {
                if (terminal.getCaretPosition() != lastSelectionStart) {
                    if (commandHistoryIndex != 0) {
                        var commandLength = 0;
                        if (commandHistoryIndex < commandHistory.size()) {
                            String command = commandHistory.get(commandHistoryIndex);
                            commandLength = command.length();
                        }
                        int temp = lastSelectionStart + commandLength;
                        if (temp > terminal.getText().length()) {
                            temp = lastSelectionStart;
                        }
                        terminal.setCaretPosition(Math.min(terminal.getText().length(), temp));
                        commandHistoryIndex = 0;
                    }
                } else {
                    terminal.setCaretPosition(lastSelectionStart);
                }
            });
        }
        if (suggestionWindow.isVisible() &&
                e.getDot() < lastSelectionStart) {
            suggestionWindow.setVisible(false);
        }
    }

    private void updateCommandFromHistory(int indexOffset) {
        if (commandHistory.isEmpty()) {
            return;
        }
        int newIndex = commandHistoryIndex + indexOffset;
        newIndex = Math.max(newIndex, 0);
        newIndex = Math.min(newIndex, commandHistory.size());

        String input = terminal.getText().substring(0, lastSelectionStart);
        if (newIndex < commandHistory.size()) {
            commandHistoryIndex = newIndex;
            String historyCommand = commandHistory.get(commandHistoryIndex);
            terminal.setText(input.concat(historyCommand));
        } else {
            commandHistoryIndex = commandHistory.size();
            terminal.setText(input);
        }
    }

    private void initSuggestionComponents() {
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JXList<>(suggestionModel);
        suggestionList.setCellRenderer(new DefaultListCellRenderer());
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setVisibleRowCount(5);
        suggestionList.putClientProperty("JList.autoSelectOnMouseMove", true);
        suggestionList.setRequestFocusEnabled(true);
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
            int maxWidth = Arrays.stream(redisCommands)
                    .mapToInt(cmd -> suggestionList.getFontMetrics(suggestionList.getFont()).stringWidth(cmd))
                    .max().orElse(200);
            int width = Math.min(maxWidth + 30, 400);
            int height = Math.min(suggestionList.getPreferredSize().height + 30, 300);

            suggestionWindow.setSize(width, height);

            Point caretPos = terminal.getCaret().getMagicCaretPosition();
            if (caretPos != null) {
                Point screenPos = terminal.getLocationOnScreen();
                suggestionWindow.setLocation(
                        screenPos.x + caretPos.x,
                        screenPos.y + caretPos.y + terminal.getFont().getSize()
                );
                suggestionWindow.setVisible(true);
                suggestionList.requestFocusInWindow();
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
            int lineEnd = fullText.indexOf('\n', lastSelectionStart);
            lineEnd = lineEnd == -1 ? fullText.length() : lineEnd;
            String currentLine = fullText.substring(lastSelectionStart, lineEnd);

            String upperInput = currentLine.toUpperCase();
            if (selected.startsWith(upperInput)) {
                String preservedPart = currentLine.isEmpty() ? ""
                        : selected.substring(0, currentLine.length()).toLowerCase();
                String originText = preservedPart + selected.substring(currentLine.length());
                String newContent = StrUtil.isUpperCase(currentLine) ? originText.toUpperCase() : originText.toLowerCase();
                String newText = fullText.substring(0, lastSelectionStart)
                        + newContent
                        + fullText.substring(lineEnd);
                SwingUtilities.invokeLater(() -> {
                    terminal.setText(newText);
                    terminal.setCaretPosition(lastSelectionStart + newContent.length());
                });
            }
        }
        SwingUtilities.invokeLater(() -> suggestionWindow.setVisible(false));
    }

    private String getCurrentInputLine() {
        return terminal.getText().substring(lastSelectionStart);
    }
}
