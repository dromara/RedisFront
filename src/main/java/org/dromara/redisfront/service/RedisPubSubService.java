package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.impl.RedisPubSubServiceImpl;

/**
 * PubSubService
 *
 * @author Jin
 */
public interface RedisPubSubService {
    RedisPubSubService service = new RedisPubSubServiceImpl();

    Long publish(RedisConnectContext redisConnectContext, String channel, String message);


}
