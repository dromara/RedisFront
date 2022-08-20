package com.redisfront.ui.component;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.AlertUtils;
import com.redisfront.commons.util.LocaleUtils;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {
    private final Timer timer;
    private final ProgressBarWorker progressBarWorker;
    private JProgressBar progressBar;

    public LoadingDialog(String message) {
        setResizable(false);
        setModal(false);
        setAlwaysOnTop(true);
        setUndecorated(true);
        initComponentUI(message);
        progressBarWorker = new ProgressBarWorker(progressBar, 20);
        timer = new Timer((20 * 1000), e -> {
            dispose();
            AlertUtils.showInformationDialog(LocaleUtils.getMessageFromBundle("LoadingDialog.timeout.message"));
        });
    }

    private void initComponentUI(String message) {
        var contentPane = new JPanel();
        contentPane.setBorder(new FlatLineBorder(new Insets(1, 1, 1, 1), UIManager.getColor("Component.borderColor")));
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        setContentPane(contentPane);
        JLabel loadInfoLabel = new JLabel();
        loadInfoLabel.setText(Fn.isEmpty(message) ? LocaleUtils.getMessageFromBundle("LoadingDialog.loadInfoLabel.default.message") : message);
        contentPane.add(loadInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar = new JProgressBar();
        contentPane.add(progressBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar.setMaximum(100);
    }

    @Override
    public void pack() {
        progressBarWorker.execute();
        timer.start();
        super.pack();
    }

    @Override
    public void dispose() {
        progressBarWorker.cancel(true);
        timer.stop();
        super.dispose();
    }

    public static class ProgressBarWorker extends SwingWorker<Void, Integer> {
        private final int delay;

        public ProgressBarWorker(JProgressBar progressBar, int delay) {
            this.delay = delay;
            addPropertyChangeListener(evt -> {
                if (Fn.equal(evt.getPropertyName(), "progress")) {
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            });
        }

        @Override
        protected Void doInBackground() throws Exception {
            for (int i = 0; i < 100; i++) {
                setProgress(i);
                Thread.sleep(delay);
            }
            return null;
        }
    }

}
