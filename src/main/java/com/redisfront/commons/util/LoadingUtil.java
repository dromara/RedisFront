package com.redisfront.commons.util;

import com.redisfront.RedisFrontApplication;
import com.redisfront.commons.func.Fn;
import com.redisfront.ui.component.LoadingDialog;

import javax.swing.*;
import java.awt.*;

/**
 * LoadingUtil
 *
 * @author Jin
 */
public class LoadingUtil {

    private static LoadingDialog loadingDialog;

    public synchronized static void showDialog() {
        SwingUtilities.invokeLater(() -> {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog();
            }
            loadingDialog.setMinimumSize(new Dimension(500, -1));
            loadingDialog.setLocationRelativeTo(RedisFrontApplication.frame);
            loadingDialog.pack();
            loadingDialog.setVisible(true);


        });
    }

    public synchronized static void closeDialog() {
        SwingUtilities.invokeLater(() -> {
            if (Fn.isNotNull(loadingDialog)) {
                loadingDialog.dispose();
                loadingDialog = null;
            }
        });
    }


}
