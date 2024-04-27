package org.dromara.redisfront.commons.ui;

import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.model.ConnectInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public abstract class AbstractTerminal extends JPanel implements KeyListener, CaretListener {
    protected final JTextArea terminal;
    private int lastSelectionStart = 0;
    private int currentDot = -1;
    private int currentKeyCode = 0;
    private boolean allowInputFlag = false;
    private boolean consumeFlag = false;

    private final ArrayList<String> commandHistory;
    private Integer commandHistoryIndex;

    public AbstractTerminal() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        terminal = new JTextArea();
        terminal.requestFocus();
        terminal.setCaretColor(Color.WHITE);
        terminal.setForeground(Color.WHITE);
        terminal.setBackground(Color.BLACK);
        terminal.addKeyListener(this);
        terminal.addCaretListener(this);
        commandHistory = new ArrayList<>();
        commandHistoryIndex = 0;
        add(new JScrollPane(terminal), BorderLayout.CENTER);
    }

    protected abstract void inputProcessHandler(String input);

    protected abstract ConnectInfo connectInfo();

    protected abstract String databaseName();

    protected void println(String message) {
        terminal.append(message);
        terminal.append("\n");
        lastSelectionStart = terminal.getText().length();
    }

    protected void print(String message) {
        terminal.append(message);
        lastSelectionStart = terminal.getText().length();
    }

    protected void printConnectedSuccessMessage() {
        this.println("");
        this.println("connection ".concat(connectInfo().host()).concat(":") + connectInfo().port() + " redis server success...");
        this.println("");
        this.print(connectInfo().host().concat(":").concat(String.valueOf(connectInfo().port())).concat(Fn.equal("0", databaseName()) ? "" : "[" + databaseName() + "]").concat(">"));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (consumeFlag) {
            e.consume();
            return;
        }
        if (currentKeyCode == KeyEvent.VK_ENTER && e.getKeyChar() == '\n') {
            var subStartLength = lastSelectionStart;
            var subEndLength = terminal.getText().length();

            if ((subStartLength < subEndLength)) {
                String input = terminal.getText().substring(subStartLength, subEndLength);
                if (Fn.isNotEmpty(input)) {
                    if (input.contains("\n") && !input.endsWith("\n")) {
                        var oldText = input;
                        input = input.replace("\n", "").concat("\n");
                        terminal.setText(terminal.getText().replace(oldText, input));
                    } else {
                        input = input.concat("\n");
                    }

                    var text = input.replace("\n", "");
                    if (Fn.isNotEmpty(text)) {
                        commandHistory.add(text);
                        commandHistoryIndex = commandHistory.size() - 1;
                    }

                    this.inputProcessHandler(input.trim());
                }
            }
            this.print("\n");
            this.print(connectInfo().host().concat(":").concat(connectInfo().port().toString()).concat(Fn.equal("0", databaseName()) ? "" : "[" + databaseName() + "]").concat(">"));
        } else if (currentKeyCode == KeyEvent.VK_ENTER && e.getKeyChar() != '\n') {
            e.consume();
            terminal.setText(terminal.getText().concat(String.valueOf(e.getKeyChar())));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        currentKeyCode = e.getKeyCode();
        if ((currentKeyCode == KeyEvent.VK_BACK_SPACE || currentKeyCode == KeyEvent.VK_DOWN || currentKeyCode == KeyEvent.VK_LEFT) && currentDot <= lastSelectionStart) {
            e.consume();
            consumeFlag = true;
        }

        if (currentKeyCode == KeyEvent.VK_UP) {
            e.consume();
            consumeFlag = true;
            if (!commandHistory.isEmpty()) {
                if (commandHistoryIndex > 0) {
                    String input = terminal.getText().substring(0, lastSelectionStart);
                    commandHistoryIndex = commandHistoryIndex - 1;
                    System.out.println("commandCacheIndex:" + commandHistoryIndex);
                    System.out.println("commandCache.size():" + commandHistory.size());
                    terminal.setText(input.concat(commandHistory.get(commandHistoryIndex)));
                } else if (commandHistoryIndex == 0) {
                    String input = terminal.getText().substring(0, lastSelectionStart);
                    terminal.setText(input.concat(commandHistory.get(commandHistoryIndex)));
                }
            }
        }

        if (currentKeyCode == KeyEvent.VK_DOWN && !commandHistory.isEmpty()) {
            e.consume();
            consumeFlag = true;
            if (!commandHistory.isEmpty()) {
                if (commandHistoryIndex < commandHistory.size() - 1) {
                    String input = terminal.getText().substring(0, lastSelectionStart);
                    commandHistoryIndex = commandHistoryIndex + 1;
                    System.out.println("commandCacheIndex:" + commandHistoryIndex);
                    System.out.println("commandCache.size():" + commandHistory.size());
                    terminal.setText(input.concat(commandHistory.get(commandHistoryIndex)));
                }
            } else if (commandHistoryIndex == 0) {
                String input = terminal.getText().substring(0, lastSelectionStart);
                terminal.setText(input.concat(commandHistory.get(commandHistoryIndex)));
            }
        }

        if (allowInputFlag) {
            consumeFlag = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (consumeFlag) {
            e.consume();
        }
    }

    public void caretUpdate(CaretEvent e) {
        currentDot = e.getDot();
        allowInputFlag = (currentDot >= lastSelectionStart);
    }

}

