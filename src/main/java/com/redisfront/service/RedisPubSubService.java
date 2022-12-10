package com.redisfront.service;

import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisPubSubServiceImpl;

/**
 * PubSubService
 *
 * @author Jin
 */
public interface RedisPubSubService {
    RedisPubSubService service = new RedisPubSubServiceImpl();

    Long publish(ConnectInfo connectInfo, String channel, String message);


}
