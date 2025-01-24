package org.dromara.redisfront.ui.support;

import javax.swing.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * ChartsPanel
 *
 * @author Jin
 */
public abstract class ChartsPanel extends JPanel {


    public void shutDownScheduledExecutorService() {
        getScheduledExecutor().shutdown();
    }


    protected abstract ScheduledExecutorService getScheduledExecutor();

}
