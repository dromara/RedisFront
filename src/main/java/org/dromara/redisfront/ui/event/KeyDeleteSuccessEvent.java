package org.dromara.redisfront.ui.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;

@Getter
public class KeyDeleteSuccessEvent extends QSEvent {
    private final Integer id;
    public KeyDeleteSuccessEvent(Object source, Integer id) {
        super(source);
        this.id = id;
    }
}
