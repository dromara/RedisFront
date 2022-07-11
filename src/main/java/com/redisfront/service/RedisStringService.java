package com.redisfront.service;

import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisStringServiceImpl;

public interface RedisStringService {
    RedisStringService service = new RedisStringServiceImpl();

    String set(ConnectInfo connectInfo, String key, String value);

    String get(ConnectInfo connectInfo, String key);

    Long strlen(ConnectInfo connectInfo, String key);

    String setex(ConnectInfo connectInfo, String key, long seconds, String value);

}
