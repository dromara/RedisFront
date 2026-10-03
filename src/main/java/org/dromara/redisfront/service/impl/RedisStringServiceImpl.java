package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.service.RedisStringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisStringServiceImpl implements RedisStringService {

    private static final Logger log = LoggerFactory.getLogger(RedisStringServiceImpl.class);


    @Override
    public String set(RedisConnectContext redisConnectContext, String key, byte[] value) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.set(key, value),
                commands -> commands.set(key, value));
    }

    @Override
    public byte[] get(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.get(key),
                commands -> commands.get(key));
    }

    @Override
    public Long strlen(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.strlen(key),
                commands -> commands.strlen(key));
    }

    @Override
    public String setex(RedisConnectContext redisConnectContext, String key, long seconds, byte[] value) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.setex(key, seconds, value),
                commands -> commands.setex(key, seconds, value));
    }


}
