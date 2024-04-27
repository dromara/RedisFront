package org.dromara.redisfront.service;

import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.service.impl.RedisPubSubServiceImpl;

/**
 * PubSubService
 *
 * @author Jin
 */
public interface RedisPubSubService {
    RedisPubSubService service = new RedisPubSubServiceImpl();

    Long publish(ConnectInfo connectInfo, String channel, String message);


}
