package com.redisfront.util;

import com.redisfront.RedisFrontApplication;
import com.redisfront.ui.component.LoadingDialog;

import java.awt.*;

public class LoadingUtil {

    private static LoadingDialog loadingDialog;

    public static void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog();
        }
        loadingDialog.setMinimumSize(new Dimension(500, -1));
        loadingDialog.setLocationRelativeTo(RedisFrontApplication.frame);
        loadingDialog.pack();
        loadingDialog.setVisible(true);
    }

    public static void closeDialog() {
        if (FunUtil.isNotNull(loadingDialog)) {
            loadingDialog.dispose();
            loadingDialog = null;
        }
    }


}
