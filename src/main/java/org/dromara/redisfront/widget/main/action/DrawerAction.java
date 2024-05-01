package org.dromara.redisfront.widget.main.action;


import lombok.Setter;
import org.dromara.quickswing.ui.app.AppAction;
import org.dromara.redisfront.widget.main.MainWidget;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;
import raven.drawer.component.DrawerPanel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DrawerAction extends AppAction<MainWidget> {
    private Animator animator;
    private final DrawerPanel drawerPanel;
    private boolean drawerOpen = true;
    @Setter
    private Consumer<Boolean> beforeProcess;
    @Setter
    private Consumer<Boolean> afterProcess;
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

        public void begin(Animator source) {
            if(beforeProcess !=null){
                beforeProcess.accept(drawerOpen);
            }
        }

        @Override
        public void timingEvent(Animator source, double fraction) {
            int width;
            if (drawerOpen) {
                width = (int) (250 - 250 * fraction);
            } else {
                width = (int) (250 * fraction);
            }
            drawerPanel.setPreferredSize(new Dimension(width , -1));
            drawerPanel.revalidate();
            app.revalidate();

        }

        @Override
        public void end(Animator source) {
            if(beforeProcess !=null){
                afterProcess.accept(drawerOpen);
            }
            drawerOpen = !drawerOpen;

        }

    };

}
