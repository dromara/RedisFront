package cn.devcms.redisfront.ui.component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TerminalComponent extends JPanel implements KeyListener, CaretListener {
    private final JTextArea terminal;
    private final StringBuffer textBuffer = new StringBuffer();
    private int currentDot = -1;
    private int currentKeyCode = 0;
    private boolean allowInputFlag = false;
    private boolean consumeFlag = false;

    public TerminalComponent() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        terminal = new JTextArea();
        add(new JScrollPane(terminal), BorderLayout.CENTER);
        terminal.requestFocus();
        terminal.setCaretColor(Color.WHITE);
        terminal.setForeground(Color.WHITE);
        terminal.setBackground(Color.BLACK);
        terminal.addKeyListener(this);
        terminal.addCaretListener(this);
        terminalInit();
    }

    private void terminalInit() {
        this.append("\n");
        this.append("connection 127.0.0.1:6379 redis server success...");
        this.append("\n");
        this.append("\n");
        this.append("127.0.0.1:6379[11]>");
    }


    private void append(String message) {
        terminal.append(message);
        textBuffer.append(message);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (consumeFlag) {
            e.consume();
            return;
        }
        if (currentKeyCode == KeyEvent.VK_ENTER) {
            var subStartLength = textBuffer.length();
            var subEndLength = terminal.getText().length() - 1;
            if (subStartLength < subEndLength) {
                String input = terminal.getText().substring(subStartLength, subEndLength);
                textBuffer.append(input);
                textBuffer.append("\n");
                this.append(input);
                this.append("\n");
            } else {
                textBuffer.append("\n");
                this.append("\n");
            }
            this.append("127.0.0.1:6379[11]>");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        currentKeyCode = e.getKeyCode();
        if (allowInputFlag) {
            consumeFlag = false;
        }
        if (
                (currentKeyCode == KeyEvent.VK_BACK_SPACE || currentKeyCode == KeyEvent.VK_ENTER || currentKeyCode == KeyEvent.VK_UP || currentKeyCode == KeyEvent.VK_LEFT) && currentDot == textBuffer.length()
        ) {
            e.consume();
            consumeFlag = true;
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
        allowInputFlag = currentDot >= textBuffer.length();
        var pos = terminal.getText().length();
        if (currentDot < pos) {
            terminal.setCaretPosition(pos);
        }
    }

}

