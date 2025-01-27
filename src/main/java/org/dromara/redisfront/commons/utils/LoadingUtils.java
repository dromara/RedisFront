package org.dromara.redisfront.commons.utils;

import org.dromara.redisfront.RedisFrontMain;
import org.dromara.redisfront.Fn;
import org.dromara.redisfront.ui.components.loading.SyncLoadingDialog;

import java.awt.*;

/**
 * LoadingUtil
 *
 * @author Jin
 */
public class LoadingUtils {

    private static SyncLoadingDialog syncLoadingDialog;

    public synchronized static void showDialog(String message) {
        syncLoadingDialog.setMinimumSize(new Dimension(500, -1));
        syncLoadingDialog.setLocationRelativeTo(RedisFrontMain.frame);
        syncLoadingDialog.pack();
        syncLoadingDialog.setVisible(true);

    }

    public synchronized static void showDialog() {
        syncLoadingDialog.setMinimumSize(new Dimension(500, -1));
        syncLoadingDialog.setLocationRelativeTo(RedisFrontMain.frame);
        syncLoadingDialog.pack();
        syncLoadingDialog.setVisible(true);

    }

    public synchronized static void closeDialog() {
        if (Fn.isNotNull(syncLoadingDialog)) {
            syncLoadingDialog.dispose();
            syncLoadingDialog = null;
        }
    }


}
