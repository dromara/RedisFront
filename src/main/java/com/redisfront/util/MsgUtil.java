package com.redisfront.util;

import com.redisfront.RedisFrontApplication;

import javax.swing.*;
import java.awt.*;

/**
 * DialogUtil
 *
 * @author Jin
 */
public class MsgUtil {

    public static void showInformationDialog(String message) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(RedisFrontApplication.frame),
                message,
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showInformationDialog(String message, Exception ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(RedisFrontApplication.frame),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorDialog(String message, Exception ex) {
        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(RedisFrontApplication.frame),
                message + "\n\n" + ex.getMessage(),
                "RedisFront", JOptionPane.ERROR_MESSAGE);
    }

}
