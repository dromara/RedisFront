package org.dromara.redisfront.commons.utils;

import org.dromara.redisfront.RedisFrontMain;

import javax.swing.*;
import java.awt.*;

/**
 * DialogUtil
 *
 * @author Jin
 */
public class AlertUtils {

    private AlertUtils() {
    }

    public static void showInformationDialog(Component owner,String message) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(owner),
                message,
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInformationDialog(Component owner,String message, Throwable ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(owner),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorDialog(Component owner,String message, Throwable ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(owner),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.ERROR_MESSAGE);
    }


    public static int showConfirmDialog(Component owner, String message, int optionType) {
        return JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(owner),
                message,
                "RedisFront", optionType, JOptionPane.INFORMATION_MESSAGE);
    }

}
