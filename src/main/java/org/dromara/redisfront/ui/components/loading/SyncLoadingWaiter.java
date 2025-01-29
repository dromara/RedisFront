package org.dromara.redisfront.ui.components.loading;

import cn.hutool.core.lang.Assert;
import lombok.Setter;
import org.dromara.redisfront.commons.Fn;

import javax.swing.*;
import java.util.function.Supplier;

public class SyncLoadingWaiter extends SwingWorker<Object, Object> {

    private final Timer timer;
    private final SyncLoadingDialog syncLoadingDialog;
    @Setter
    private Supplier<Object> supplier;
    private Integer count = 0;

    public SyncLoadingWaiter(SyncLoadingDialog syncLoadingDialog) {
        this.syncLoadingDialog = syncLoadingDialog;
        this.timer = new Timer(100, _ -> {
            if (count < 100) {
                setProgress(count += 1);
            } else {
                publish("timeout");
            }
        });
        this.addPropertyChangeListener(event -> {
            if (Fn.equal(event.getPropertyName(), "state")) {
                if (StateValue.STARTED == event.getNewValue()) {
                    this.timer.start();
                    syncLoadingDialog.setLocationRelativeTo(syncLoadingDialog.getOwner());
                    syncLoadingDialog.setVisible(true);
                    syncLoadingDialog.pack();
                } else if (StateValue.DONE == event.getNewValue()) {
                    this.timer.stop();
                    this.cancel(true);
                    syncLoadingDialog.setVisible(false);
                    syncLoadingDialog.dispose();
                }
            } else if (Fn.equal(event.getPropertyName(), "progress")) {
                syncLoadingDialog.getProgressBar().setValue((Integer) event.getNewValue());
            }
        });
    }

    @Override
    protected void process(java.util.List<Object> chunks) {
        for (Object chunk : chunks) {
            if (Fn.equal(chunk, "timeout")) {
                this.timer.stop();
                this.cancel(true);
                syncLoadingDialog.getMessageLabel().setText(syncLoadingDialog.$tr("LoadingDialog.loadInfoLabel.timeout.message"));
            }
        }
    }

    @Override
    protected Object doInBackground() {
        Assert.notNull(supplier, "supplier must not be null");
        return supplier.get();
    }
}