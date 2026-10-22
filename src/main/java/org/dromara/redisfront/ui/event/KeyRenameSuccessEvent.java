package org.dromara.redisfront.ui.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;

@Getter
public class KeyRenameSuccessEvent extends QSEvent {
    private final Integer id;
    public KeyRenameSuccessEvent(Object message, Integer id) {
        super(message);
        this.id = id;
    }
}
