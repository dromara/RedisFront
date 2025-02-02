package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.impl.RedisStringServiceImpl;

public interface RedisStringService {
    RedisStringService service = new RedisStringServiceImpl();

    String set(RedisConnectContext redisConnectContext, String key, String value);

    String get(RedisConnectContext redisConnectContext, String key);

    Long strlen(RedisConnectContext redisConnectContext, String key);

    String setex(RedisConnectContext redisConnectContext, String key, long seconds, String value);

}
