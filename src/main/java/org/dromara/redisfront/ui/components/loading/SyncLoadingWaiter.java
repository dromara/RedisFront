package org.dromara.redisfront.ui.components.loading;

import cn.hutool.core.lang.Assert;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.commons.Fn;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class SyncLoadingWaiter extends SwingWorker<Object, Object> {

    private final Timer timer;
    private final AtomicInteger count;
    private final SyncLoadingDialog syncLoadingDialog;
    private static final String TIMEOUT_MESSAGE_KEY = "LoadingDialog.loadInfoLabel.timeout.message";
    @Setter
    private Supplier<Object> supplier;
    @Setter
    private BiConsumer<Object, Exception> biConsumer;

    public SyncLoadingWaiter(SyncLoadingDialog syncLoadingDialog) {
        this.syncLoadingDialog = syncLoadingDialog;
        this.count = new AtomicInteger(0);
        this.timer = new Timer(100, _ -> {
            if (count.get() < 100) {
                setProgress(count.incrementAndGet());
            } else {
                this.publish("timeout");
            }
        });
        this.addPropertyChangeListener(event -> {
            if (Fn.equal(event.getPropertyName(), "state")) {
                if (StateValue.STARTED == event.getNewValue()) {
                    this.timer.start();
                } else if (StateValue.DONE == event.getNewValue()) {
                    terminated();
                }
            } else if (Fn.equal(event.getPropertyName(), "progress")) {
                this.syncLoadingDialog.setProgressValue((Integer) event.getNewValue());
            }
        });
    }

    public void terminated() {
        if (this.isDone()) {
            boolean cancelResult = this.cancel(true);
            if (!cancelResult) {
                log.warn("Failed to cancel the task.");
            }
        }
        if(this.timer.isRunning()) {
            this.timer.stop();
        }
        this.syncLoadingDialog.setVisible(false);
        this.syncLoadingDialog.dispose();

    }

    @Override
    protected void done() {
        try {
            this.biConsumer.accept(this.get(), null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.biConsumer.accept(null, e);
        }
    }

    @Override
    protected void process(List<Object> chunks) {
        if (chunks.isEmpty()) {
            return;
        }
        if (Fn.equal(chunks.getFirst(), "timeout")) {
            this.timer.stop();
            this.cancel(true);
            this.syncLoadingDialog.setMessageValue(syncLoadingDialog.$tr(TIMEOUT_MESSAGE_KEY));
        }
    }

    @Override
    protected Object doInBackground() {
        Assert.notNull(supplier, "supplier must not be null");
        return supplier.get();
    }
}