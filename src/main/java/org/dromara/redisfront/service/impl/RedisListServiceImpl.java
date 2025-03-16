package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.service.RedisListService;

import java.util.Arrays;
import java.util.List;

/**
 * RedisListServiceImpl
 *
 * @author Jin
 */
public class RedisListServiceImpl implements RedisListService {
    @Override
    public List<String> lrange(RedisConnectContext redisConnectContext, String key, long start, long stop) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lrange(key, start, stop));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lrange(key, start, stop));
        }
    }

    @Override
    public Long lrem(RedisConnectContext redisConnectContext, String key, long count, String value) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lrem(key, count, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lrem(key, count, value));
        }
    }

    @Override
    public Long llen(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.llen(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.llen(key));
        }
    }

    @Override
    public String lpop(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lpop(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lpop(key));
        }
    }

    @Override
    public List<String> lpop(RedisConnectContext redisConnectContext, String key, long count) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lpop(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lpop(key, count));
        }
    }

    @Override
    public Long lpush(RedisConnectContext redisConnectContext, String key, String... values) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lpush(key, values));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lpush(key, values));
        }
    }

    @Override
    public String lset(RedisConnectContext redisConnectContext, String key, long index, String value) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lset(key, index, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lset(key, index, value));
        }
    }

    @Override
    public String rpop(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.rpop(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.rpop(key));
        }
    }

    @Override
    public List<String> rpop(RedisConnectContext redisConnectContext, String key, long count) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.rpop(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.rpop(key, count));
        }
    }

    @Override
    public Long rpush(RedisConnectContext redisConnectContext, String key, String... values) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.rpush(key, values));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.rpush(key, values));
        }
    }
}
