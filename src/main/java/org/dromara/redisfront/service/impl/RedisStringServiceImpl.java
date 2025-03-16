package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.service.RedisStringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisStringServiceImpl implements RedisStringService {

    private static final Logger log = LoggerFactory.getLogger(RedisStringServiceImpl.class);


    @Override
    public String set(RedisConnectContext redisConnectContext, String key, String value) {

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.set(key, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.set(key, value));
        }
    }

    @Override
    public String get(RedisConnectContext redisConnectContext, String key) {

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.get(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.get(key));
        }
    }

    @Override
    public Long strlen(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.strlen(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.strlen(key));
        }
    }

    @Override
    public String setex(RedisConnectContext redisConnectContext, String key, long seconds, String value) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.setex(key, seconds, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.setex(key, seconds, value));
        }
    }


}
