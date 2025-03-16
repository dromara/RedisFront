package org.dromara.redisfront.ui.components.loading;

import cn.hutool.core.lang.Assert;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
class SyncLoadingWaiter<T> extends SwingWorker<T, Object> {
    private static final String TIMEOUT_MESSAGE_KEY = "SyncLoadingWaiter.loadInfoLabel.timeout.message";
    private static final int TIMER_DELAY_MS = 1000;
    private static final int MAX_PROGRESS = 100;

    private final Timer timer;
    private final AtomicInteger progressCount;
    private final SyncLoadingDialog syncLoadingDialog;

    @Setter
    private Supplier<T> supplier;
    @Setter
    private BiConsumer<T, Exception> biConsumer;

    public SyncLoadingWaiter(SyncLoadingDialog syncLoadingDialog) {
        this.syncLoadingDialog = syncLoadingDialog;
        this.progressCount = new AtomicInteger(0);
        this.timer = new Timer(TIMER_DELAY_MS, _ -> updateProgress());
        initPropertyChangeListener();
    }
    private void initPropertyChangeListener() {
        this.addPropertyChangeListener(event -> {
            switch (event.getPropertyName()) {
                case "state":
                    handleStateChange(event.getNewValue());
                    break;
                case "progress":
                    syncLoadingDialog.setProgressValue((Integer) event.getNewValue());
                    break;
            }
        });
    }

    private void handleStateChange(Object newState) {
        if (StateValue.STARTED == newState) {
            timer.start();
        } else if (StateValue.DONE == newState) {
            terminated();
        }
    }

    private void updateProgress() {
        if (progressCount.get() < MAX_PROGRESS) {
            setProgress(progressCount.incrementAndGet());
        } else {
            publish("timeout");
        }
    }

    public void terminated() {
        this.cancel(true);
        this.timer.stop();
        this.syncLoadingDialog.setVisible(false);
        this.syncLoadingDialog.dispose();
    }

    @Override
    protected void done() {
        try {
            T result = get();
            this.biConsumer.accept(result, null);
        } catch (CancellationException e) {
            log.info("Task was cancelled: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Task execution failed: {}", e.getMessage(), e);
            this.timer.stop();
            this.biConsumer.accept(null, e);
        }
    }

    @Override
    protected void process(List<Object> chunks) {
        if (!chunks.isEmpty() && "timeout".equals(chunks.getFirst())) {
            timer.stop();
            cancel(true);
            syncLoadingDialog.setMessageValue(syncLoadingDialog.$tr(TIMEOUT_MESSAGE_KEY));
        }
    }

    @Override
    protected T doInBackground() {
        Assert.notNull(supplier, "supplier must not be null");
        return supplier.get();
    }
}
