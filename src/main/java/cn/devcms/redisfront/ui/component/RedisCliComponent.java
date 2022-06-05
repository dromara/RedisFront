package cn.devcms.redisfront.ui.component;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.swing.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;

public class RedisCliComponent extends JDesktopPane {


    private final SwingTerminal terminal;
    private final JScrollBar scrollBar;
    private final Thread thread;


    public RedisCliComponent() throws IOException {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollBar = new JScrollBar();
        scrollBar.setMinimum(0);
        scrollBar.setMaximum(20);
        scrollBar.setValue(0);
        scrollBar.setVisibleAmount(20);
        scrollBar.addAdjustmentListener(new ScrollbarListener());
        add(scrollBar, BorderLayout.EAST);

        terminal = new SwingTerminal(TerminalEmulatorDeviceConfiguration.getDefault().withLineBufferScrollbackSize(500), SwingTerminalFontConfiguration.getDefault(), TerminalEmulatorColorConfiguration.getDefault(), new ScrollController());
        terminal.addResizeListener((terminal, newSize) -> {
            try {
                terminal.setCursorPosition(0, 0);
                terminal.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        terminal.setFocusable(true);
        add(terminal, BorderLayout.CENTER);


        thread = new Thread(inputRunnable);
    }

    public void pollInputStart() {
        thread.start();
    }

    Runnable inputRunnable = new Runnable() {
        final TerminalPosition[] cursorPosition = {new TerminalPosition(0, 0)};

        @Override
        public void run() {
            while (true) {
                KeyStroke keyStroke = terminal.pollInput();
                System.out.println(keyStroke);
                if (keyStroke != null) {
                    switch (keyStroke.getKeyType()) {
                        case ArrowDown:
                            if (terminal.getTerminalSize().getRows() > cursorPosition[0].getRow() + 1) {
                                cursorPosition[0] = cursorPosition[0].withRelativeRow(1);
                                terminal.setCursorPosition(cursorPosition[0].getColumn(), cursorPosition[0].getRow());
                            }
                            break;
                        case ArrowUp:
                            if (cursorPosition[0].getRow() > 0) {
                                cursorPosition[0] = cursorPosition[0].withRelativeRow(-1);
                                terminal.setCursorPosition(cursorPosition[0].getColumn(), cursorPosition[0].getRow());
                            }
                            break;
                        case ArrowRight:
                            if (cursorPosition[0].getColumn() + 1 < terminal.getTerminalSize().getColumns()) {
                                cursorPosition[0] = cursorPosition[0].withRelativeColumn(1);
                                terminal.setCursorPosition(cursorPosition[0].getColumn(), cursorPosition[0].getRow());
                            }
                            break;
                        case ArrowLeft:
                            if (cursorPosition[0].getColumn() > 0) {
                                cursorPosition[0] = cursorPosition[0].withRelativeColumn(-1);
                                terminal.setCursorPosition(cursorPosition[0].getColumn(), cursorPosition[0].getRow());
                            }
                            break;
                        case Backspace:
                            break;
                        case Enter:
                            terminal.putCharacter('\n');
                            break;
                        case Character:
                            terminal.putCharacter(keyStroke.getCharacter());
                            break;
                        default:
                    }
                    terminal.flush();
                }

            }
        }
    };

    private class ScrollController implements TerminalScrollController {
        private int scrollValue;

        @Override
        public void updateModel(final int totalSize, final int screenHeight) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(() -> updateModel(totalSize, screenHeight));
                return;
            }

            int value = scrollBar.getValue();
            int maximum = scrollBar.getMaximum();
            int visibleAmount = scrollBar.getVisibleAmount();

            if (maximum != totalSize) {
                int lastMaximum = maximum;
                maximum = Math.max(totalSize, screenHeight);
                if (lastMaximum < maximum && lastMaximum - visibleAmount - value == 0) {
                    value = scrollBar.getValue() + (maximum - lastMaximum);
                }
            }
            if (value + screenHeight > maximum) {
                value = maximum - screenHeight;
            }
            if (visibleAmount != screenHeight) {
                if (visibleAmount > screenHeight) {
                    value += visibleAmount - screenHeight;
                }
                visibleAmount = screenHeight;
            }
            if (value > maximum - visibleAmount) {
                value = maximum - visibleAmount;
            }
            if (value < 0) {
                value = 0;
            }

            this.scrollValue = value;

            if (scrollBar.getMaximum() != maximum) {
                scrollBar.setMaximum(maximum);
            }
            if (scrollBar.getVisibleAmount() != visibleAmount) {
                scrollBar.setVisibleAmount(visibleAmount);
            }
            if (scrollBar.getValue() != value) {
                scrollBar.setValue(value);
            }

        }

        @Override
        public int getScrollingOffset() {
            return scrollValue;
        }
    }

    private class ScrollbarListener implements AdjustmentListener {
        @Override
        public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
            terminal.repaint();
        }
    }

    public SwingTerminal getTerminal() {
        return terminal;
    }


}
