package org.dromara.redisfront.widget.components.action;


import lombok.Getter;
import lombok.Setter;
import org.dromara.quickswing.ui.app.AppAction;
import org.dromara.redisfront.widget.MainWidget;
import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTarget;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DrawerAction extends AppAction<MainWidget> {
    private Animator animator;
    @Getter
    private boolean drawerOpen = true;
    @Setter
    private Consumer<Boolean> beforeProcess;
    @Setter
    private BiConsumer<Double,Boolean> process;
    @Setter
    private Consumer<Boolean> afterProcess;
    public DrawerAction(MainWidget app,BiConsumer<Double,Boolean> process) {
        super(app);
        this.process = process;
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
            process.accept(fraction,drawerOpen);
            app.revalidate();
            app.repaint();
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
