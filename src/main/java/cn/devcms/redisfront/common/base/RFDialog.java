package cn.devcms.redisfront.common.base;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * BaseDialog
 *
 * @author Jin
 */
public class RFDialog<T> extends JDialog {


    protected Consumer<T> callback;

    public RFDialog(Frame owner, Consumer<T> callback) {
        super(owner);
        this.callback = callback;
    }

    public RFDialog(Frame owner) {
        super(owner);
    }
}
