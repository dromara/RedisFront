package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.service.impl.RedisPubSubServiceImpl;

/**
 * PubSubService
 *
 * @author Jin
 */
public interface RedisPubSubService {
    RedisPubSubService service = new RedisPubSubServiceImpl();

    Long publish(ConnectContext connectContext, String channel, String message);


}
