package org.dromara.redisfront.model.event;

import lombok.Getter;
import org.dromara.quickswing.events.QSEvent;
import org.dromara.redisfront.model.context.RedisConnectContext;

@Getter
public class RedisFrontEvent extends QSEvent {
    private final RedisConnectContext redisConnectContext;

    public RedisFrontEvent(Object source, RedisConnectContext redisConnectContext) {
        super(source);
        this.redisConnectContext = redisConnectContext;
    }
}
