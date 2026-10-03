package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisListService;

import java.util.List;

/**
 * RedisListServiceImpl
 *
 * @author Jin
 */
public class RedisListServiceImpl implements RedisListService {
    @Override
    public List<byte[]> lrange(RedisConnectContext redisConnectContext, String key, long start, long stop) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.lrange(key, start, stop),
                commands -> commands.lrange(key, start, stop));
    }

    @Override
    public Long lrem(RedisConnectContext redisConnectContext, String key, long count, byte[] value) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.lrem(key, count, value),
                commands -> commands.lrem(key, count, value));
    }

    @Override
    public Long llen(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.llen(key),
                commands -> commands.llen(key));
    }

    @Override
    public byte[] lpop(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.lpop(key),
                commands -> commands.lpop(key));
    }

    @Override
    public List<byte[]> lpop(RedisConnectContext redisConnectContext, String key, long count) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.lpop(key, count),
                commands -> commands.lpop(key, count));
    }

    @Override
    public Long lpush(RedisConnectContext redisConnectContext, String key, byte[]... values) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.lpush(key, values),
                commands -> commands.lpush(key, values));
    }

    @Override
    public String lset(RedisConnectContext redisConnectContext, String key, long index, byte[] value) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.lset(key, index, value),
                commands -> commands.lset(key, index, value));
    }

    @Override
    public byte[] rpop(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.rpop(key),
                commands -> commands.rpop(key));
    }

    @Override
    public List<byte[]> rpop(RedisConnectContext redisConnectContext, String key, long count) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.rpop(key, count),
                commands -> commands.rpop(key, count));
    }

    @Override
    public Long rpush(RedisConnectContext redisConnectContext, String key, byte[]... values) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.rpush(key, values),
                commands -> commands.rpush(key, values));
    }
}
