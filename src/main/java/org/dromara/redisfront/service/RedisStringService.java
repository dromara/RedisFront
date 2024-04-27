package org.dromara.redisfront.service;

import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.service.impl.RedisStringServiceImpl;

public interface RedisStringService {
    RedisStringService service = new RedisStringServiceImpl();

    String set(ConnectInfo connectInfo, String key, String value);

    String get(ConnectInfo connectInfo, String key);

    Long strlen(ConnectInfo connectInfo, String key);

    String setex(ConnectInfo connectInfo, String key, long seconds, String value);

}
