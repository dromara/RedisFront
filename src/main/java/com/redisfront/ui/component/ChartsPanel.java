package com.redisfront.ui.component;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * ChartsPanel
 *
 * @author Jin
 */
public class ChartsPanel extends JPanel {


    protected final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public void shutDownScheduledExecutorService() {
        scheduledExecutor.shutdown();
    }


}
