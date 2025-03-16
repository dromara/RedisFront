package org.dromara.redisfront.ui.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;

@Getter
public class ClickKeyTreeNodeEvent extends QSEvent {
    private final Integer id;
    public ClickKeyTreeNodeEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }
}
