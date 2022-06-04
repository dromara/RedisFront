package cn.devcms.redisfront.ui.component;

import com.googlecode.lanterna.terminal.swing.SwingTerminal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RedisCommandComponent extends JPanel {


    private final SwingTerminal terminal;

    public RedisCommandComponent() {
        terminal = new SwingTerminal();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        add(terminal);
    }

    public SwingTerminal getTerminal() {
        return terminal;
    }
}
