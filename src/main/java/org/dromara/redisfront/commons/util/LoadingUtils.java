package org.dromara.redisfront.commons.util;

import org.dromara.redisfront.Application;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.ui.component.LoadingDialog;

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
        loadingDialog.setLocationRelativeTo(Application.frame);
        loadingDialog.pack();
        loadingDialog.setVisible(true);

    }

    public synchronized static void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(null);
        }
        loadingDialog.setMinimumSize(new Dimension(500, -1));
        loadingDialog.setLocationRelativeTo(Application.frame);
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
