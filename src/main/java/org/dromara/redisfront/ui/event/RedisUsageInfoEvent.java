package org.dromara.redisfront.ui.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;

@Getter
public class RedisUsageInfoEvent extends QSEvent {
    private final Integer id;
    public RedisUsageInfoEvent( Integer id,Object message) {
        super(message);
        this.id = id;
    }

}
