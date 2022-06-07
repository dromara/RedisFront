package cn.devcms.redisfront.common.util;

import javax.swing.*;
import java.awt.*;

/**
 * DialogUtil
 *
 * @author Jin
 */
public class DialogUtil {


    public static void showInformationDialog(Component c, String message) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(c),
                message,
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInformationDialog(Component c, String message, Exception ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(c),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorDialog(Component c, String message, Exception ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(c),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.ERROR_MESSAGE);
    }

}
