package cn.devcms.redisfront.common.base;

import cn.devcms.redisfront.model.ConnectInfo;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * BaseDialog
 *
 * @author Jin
 */
public class BaseDialog<T> extends JDialog {

    protected Consumer<T> callback;

    public BaseDialog(Frame owner, Consumer<T> callback) {
        super(owner);
        this.callback = callback;
    }

    public BaseDialog(Frame owner) {
        super(owner);
    }
}
