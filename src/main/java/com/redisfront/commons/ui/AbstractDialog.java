package com.redisfront.commons.ui;

import com.redisfront.commons.handler.ProcessHandler;

import javax.swing.*;
import java.awt.*;

/**
 * BaseDialog
 *
 * @author Jin
 */
public abstract class AbstractDialog<T> extends JDialog {
    protected ProcessHandler<T> processHandler;

    public AbstractDialog(Frame owner, ProcessHandler<T> processHandler) {
        super(owner);
        this.processHandler = processHandler;
    }

    public AbstractDialog(Frame owner) {
        super(owner);
    }
}
