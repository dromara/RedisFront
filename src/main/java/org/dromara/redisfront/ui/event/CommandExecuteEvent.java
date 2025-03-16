package org.dromara.redisfront.ui.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;

@Getter
public class CommandExecuteEvent extends QSEvent {
    private final Integer id;
    public CommandExecuteEvent(Object message, Integer id) {
        super(message);
        this.id = id;
    }
}
