package org.dromara.redisfront.worker;

import javax.swing.*;
import java.util.List;


@SuppressWarnings("all")
public class DataSyncWorker extends SwingWorker<Void, String> {





    @Override
    protected Void doInBackground() throws Exception {

        return null;
    }


    private void updateProgress(int i, int total) {
        int progress = (i + 1) * 100 / total;
        setProgress(progress);
    }

    @Override
    protected void process(List<String> chunks) {
        for (String chunk : chunks) {

        }
    }
}
