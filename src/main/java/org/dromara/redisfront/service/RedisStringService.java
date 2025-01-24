package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.service.impl.RedisStringServiceImpl;

public interface RedisStringService {
    RedisStringService service = new RedisStringServiceImpl();

    String set(ConnectContext connectContext, String key, String value);

    String get(ConnectContext connectContext, String key);

    Long strlen(ConnectContext connectContext, String key);

    String setex(ConnectContext connectContext, String key, long seconds, String value);

}
