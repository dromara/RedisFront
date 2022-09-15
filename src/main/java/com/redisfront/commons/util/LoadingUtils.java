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
public class LoadingUtils {

    private static LoadingDialog loadingDialog;

    public synchronized static void showDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(message);
        }
        loadingDialog.setMinimumSize(new Dimension(500, -1));
        loadingDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        loadingDialog.pack();
        loadingDialog.setVisible(true);

    }

    public synchronized static void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(null);
        }
        loadingDialog.setMinimumSize(new Dimension(500, -1));
        loadingDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        loadingDialog.pack();
        loadingDialog.setVisible(true);

    }

    public synchronized static void closeDialog() {
        if (Fn.isNotNull(loadingDialog)) {
            loadingDialog.dispose();
            loadingDialog = null;
        }
    }


}
