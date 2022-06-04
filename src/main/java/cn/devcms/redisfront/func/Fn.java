package cn.devcms.redisfront.func;

import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import java.awt.*;

public class Fn {

    public static void revalidateAndRepaintAllFramesAndDialogs() {
        FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
    }

    public static void removeAllComponents(JPanel panel) {
        Component[] components = panel.getComponents();
        if (components != null && components.length > 0) {
            for (Component component : components) {
                panel.remove(component);
            }
        }
    }

}
