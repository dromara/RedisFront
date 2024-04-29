package org.dromara.redisfront.widget.main.action;


import org.dromara.quickswing.ui.app.AppAction;
import org.dromara.redisfront.widget.main.MainWidget;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import raven.drawer.component.DrawerPanel;

import java.awt.*;
import java.awt.event.ActionEvent;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DrawerAction extends AppAction<MainWidget> {
    protected DrawerAction(MainWidget app) {
        super(app);
    }

    private Animator animator;
    private DrawerPanel drawerPanel;
    private boolean drawerOpen = true;

    public DrawerAction(MainWidget app, DrawerPanel drawerPanel) {
        super(app);
        this.drawerPanel = drawerPanel;

    }

    @Override
    public void handleAction(ActionEvent e) {
        if (animator != null) {
            animator.stop();
        }
        SwingTimerTimingSource swingTimerTimingSource = new SwingTimerTimingSource();
        swingTimerTimingSource.init();
        animator = new Animator.Builder(swingTimerTimingSource)
                .setDebugName("700")
                .setDuration(500, MILLISECONDS)
                .addTargets(target)
                .setDisposeTimingSource(true)
                .build();
        animator.start();
    }

    TimingTarget target = new TimingTargetAdapter() {
        @Override
        public void timingEvent(Animator source, double fraction) {
            int width;
            if (drawerOpen) {
                width = (int) (250 - 250 * fraction);
            } else {
                width = (int) (250 * fraction);
            }
            System.out.println(width);
            drawerPanel.setPreferredSize(new Dimension(width , -1));
            drawerPanel.revalidate();
            app.revalidate();
        }

        @Override
        public void end(Animator source) {
            drawerOpen = !drawerOpen;
        }

    };

}
