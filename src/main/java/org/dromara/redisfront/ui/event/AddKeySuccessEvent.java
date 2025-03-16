package org.dromara.redisfront.ui.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;

@Getter
public class AddKeySuccessEvent extends QSEvent {
    private final Integer id;
    public AddKeySuccessEvent(Object message, Integer id) {
        super(message);
        this.id = id;
    }
}
