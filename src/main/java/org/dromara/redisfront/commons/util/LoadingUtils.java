package org.dromara.redisfront.commons.util;

import org.dromara.redisfront.RedisFrontMain;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.ui.core.LoadingDialog;

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
        loadingDialog.setLocationRelativeTo(RedisFrontMain.frame);
        loadingDialog.pack();
        loadingDialog.setVisible(true);

    }

    public synchronized static void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(null);
        }
        loadingDialog.setMinimumSize(new Dimension(500, -1));
        loadingDialog.setLocationRelativeTo(RedisFrontMain.frame);
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
