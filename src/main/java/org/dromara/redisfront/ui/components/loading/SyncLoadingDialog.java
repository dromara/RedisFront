package org.dromara.redisfront.ui.components.loading;

import cn.hutool.core.util.StrUtil;
import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class SyncLoadingDialog extends QSDialog<RedisFrontWidget> {
    private JProgressBar progressBar;
    private JLabel messageLabel;

    private SyncLoadingDialog(RedisFrontWidget owner) {
        super(owner, true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(500, 100));
        this.setupUI(null);
    }

    private SyncLoadingDialog(RedisFrontWidget owner, String message) {
        super(owner, true);
        this.setResizable(true);
        this.setMinimumSize(new Dimension(500, 100));
        this.setupUI(message);
    }

    protected void setProgressValue(int value) {
        progressBar.setValue(value);
    }

    protected void setMessageValue(String message) {
        messageLabel.setText(message);
    }

    protected void setupUI(String message) {
        var contentPane = new JPanel();
        contentPane.setBorder(new FlatLineBorder(new Insets(1, 1, 1, 1), UIManager.getColor("Component.borderColor")));
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        setContentPane(contentPane);
        messageLabel = new JLabel();
        if (StrUtil.isNotBlank(message)) {
            messageLabel.setText(message);
        } else {
            messageLabel.setText($tr("LoadingDialog.loadInfoLabel.default.message"));
        }
        contentPane.add(messageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        contentPane.add(progressBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar.setMaximum(100);
        progressBar.setMinimumSize(new Dimension(-1, 10));
    }

    public static SyncLoadingDialog builder(RedisFrontWidget owner) {
        return new SyncLoadingDialog(owner);
    }
    public static SyncLoadingDialog builder(RedisFrontWidget owner, String message) {
        return new SyncLoadingDialog(owner, message);
    }

    public <T> void showSyncLoadingDialog(Supplier<T> supplier, BiConsumer<T, Exception> biConsumer) {
        SyncLoadingWaiter<T> syncLoadingWaiter = new SyncLoadingWaiter<>(this);
        syncLoadingWaiter.setSupplier(supplier);
        syncLoadingWaiter.setBiConsumer(biConsumer);
        syncLoadingWaiter.execute();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                syncLoadingWaiter.terminated();
                messageLabel.setText($tr("LoadingDialog.loadInfoLabel.cancel.message"));
                super.windowClosing(e);
            }
        });
        this.setLocationRelativeTo(getOwner());
        this.setVisible(true);
        this.pack();
    }

}
