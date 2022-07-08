package com.redisfront.ui.component;

import com.formdev.flatlaf.ui.FlatLineBorder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.redisfront.util.FunUtil;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {

    private final ProgressBarWorker progressBarWorker;


    public LoadingDialog() {
        setResizable(false);
        setModal(false);
        setAlwaysOnTop(true);
        setUndecorated(true);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new FlatLineBorder(new Insets(1, 1, 1, 1), UIManager.getColor("Component.borderColor")));
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        setContentPane(contentPane);
        JLabel loadInfoLabel = new JLabel();
        loadInfoLabel.setText("");
        contentPane.add(loadInfoLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        var progressBar = new JProgressBar();
        contentPane.add(progressBar, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        progressBar.setMaximum(100);
        progressBarWorker = new ProgressBarWorker(progressBar, 50);
        loadInfoLabel.setText("加载中，请稍后....");
    }

    @Override
    public void pack() {
        super.pack();
        progressBarWorker.execute();
    }

    @Override
    public void dispose() {
        super.dispose();
        progressBarWorker.cancel(true);
    }

    public static class ProgressBarWorker extends SwingWorker<Void, Integer> {
        private final int delay;

        public ProgressBarWorker(JProgressBar progressBar, int delay) {
            this.delay = delay;
            addPropertyChangeListener(evt -> {
                if (FunUtil.equal(evt.getPropertyName(), "progress")) {
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
