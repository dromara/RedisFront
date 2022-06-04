package cn.devcms.redisfront.ui.component;

import com.googlecode.lanterna.terminal.swing.SwingTerminal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RedisCommandComponent extends JPanel {


    private final SwingTerminal terminal;

    public RedisCommandComponent() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        terminal = new SwingTerminal();
        JPopupMenu menu = new JPopupMenu();
        JMenuItem clearItem = new JMenuItem("清除");
        menu.add(clearItem);
        JMenuItem copyItem = new JMenuItem("复制");
        menu.add(copyItem);
        JMenuItem pasteItem = new JMenuItem("粘贴");
        menu.add(pasteItem);
        terminal.setComponentPopupMenu(menu);
        terminal.updateUI();
        add(terminal);
    }

    public SwingTerminal getTerminal() {
        return terminal;
    }
}
