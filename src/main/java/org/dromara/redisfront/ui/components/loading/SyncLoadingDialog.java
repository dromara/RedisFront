package org.dromara.redisfront.ui.components.loading;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.Getter;
import org.dromara.quickswing.ui.app.QSDialog;
import org.dromara.redisfront.RedisFrontContext;
import org.dromara.redisfront.ui.widget.MainWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public class SyncLoadingDialog extends QSDialog<MainWidget> {
    private JProgressBar progressBar;
    private JLabel messageLabel;
    private final RedisFrontContext context;
    private final SyncLoadingWaiter syncLoadingWaiter;

    private SyncLoadingDialog(MainWidget owner) {
        super(owner, true);
        this.setResizable(false);
        this.setAlwaysOnTop(true);
        this.setMinimumSize(new Dimension(500, 100));
        this.setupUI();
        this.context = (RedisFrontContext) owner.getContext();
        syncLoadingWaiter = new SyncLoadingWaiter(this);
    }

    protected void setupUI() {
        var contentPane = new JPanel();
        contentPane.setBorder(new FlatLineBorder(new Insets(1, 1, 1, 1), UIManager.getColor("Component.borderColor")));
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        setContentPane(contentPane);
        messageLabel = new JLabel($tr("LoadingDialog.loadInfoLabel.default.message"));
        contentPane.add(messageLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        contentPane.add(progressBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar.setMaximum(100);
        progressBar.setMinimumSize(new Dimension(-1, 10));
    }


    public static SyncLoadingDialog newInstance(MainWidget owner) {
        return new SyncLoadingDialog(owner);
    }

    public void showSyncLoadingDialog(Supplier<Object> supplier, BiConsumer<Object, Exception> biConsumer) {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                syncLoadingWaiter.cancel(false);
                messageLabel.setText($tr("LoadingDialog.loadInfoLabel.cancel.message"));
                super.windowClosing(e);
            }
        });
        this.syncLoadingWaiter.setSupplier(supplier);
        this.syncLoadingWaiter.execute();
        this.context.taskExecute(syncLoadingWaiter::get, (object, exception) -> {
            getOwner().getContentPane().setEnabled(true);
            biConsumer.accept(object, exception);
        });
    }

}
