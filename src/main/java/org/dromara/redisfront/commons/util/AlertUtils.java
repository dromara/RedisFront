package org.dromara.redisfront.commons.util;

import org.dromara.redisfront.Application;

import javax.swing.*;

/**
 * DialogUtil
 *
 * @author Jin
 */
public class AlertUtils {

    private AlertUtils() {
    }

    public static void showInformationDialog(String message) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(Application.frame),
                message,
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInformationDialog(String message, Throwable ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(Application.frame),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorDialog(String message, Throwable ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(Application.frame),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.ERROR_MESSAGE);
    }


    public static int showConfirmDialog(String message, int optionType) {
        return JOptionPane.showConfirmDialog(SwingUtilities.windowForComponent(Application.frame),
                message,
                "RedisFront", optionType, JOptionPane.INFORMATION_MESSAGE);
    }

}
