package org.dromara.redisfront.service.impl;

import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisHashService;

import java.util.List;
import java.util.Map;

/**
 * RedisHashServiceImpl
 *
 * @author Jin
 */
public class RedisHashServiceImpl implements RedisHashService {

    @Override
    public byte[] hget(RedisConnectContext redisConnectContext, String key, String field) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hget(key, field),
                commands -> commands.hget(key, field));
    }

    @Override
    public Map<String, byte[]> hgetall(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hgetall(key),
                commands -> commands.hgetall(key));
    }

    @Override
    public List<String> hkeys(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hkeys(key),
                commands -> commands.hkeys(key));
    }

    @Override
    public Long hlen(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hlen(key),
                commands -> commands.hlen(key));
    }

    @Override
    public String hmset(RedisConnectContext redisConnectContext, String key, Map<String, byte[]> map) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hmset(key, map),
                commands -> commands.hmset(key, map));
    }

    @Override
    public MapScanCursor<String, byte[]> hscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hscan(key, scanCursor, scanArgs),
                commands -> commands.hscan(key, scanCursor, scanArgs));
    }

    @Override
    public MapScanCursor<String, byte[]> hscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hscan(key, scanCursor),
                commands -> commands.hscan(key, scanCursor));
    }

    @Override
    public Boolean hset(RedisConnectContext redisConnectContext, String key, String field, byte[] value) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hset(key, field, value),
                commands -> commands.hset(key, field, value));
    }

    @Override
    public Long hset(RedisConnectContext redisConnectContext, String key, Map<String, byte[]> map) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hset(key, map),
                commands -> commands.hset(key, map));
    }

    @Override
    public Long hstrlen(RedisConnectContext redisConnectContext, String key, String field) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hstrlen(key, field),
                commands -> commands.hstrlen(key, field));
    }

    @Override
    public List<byte[]> hvals(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hvals(key),
                commands -> commands.hvals(key));
    }

    @Override
    public Long hdel(RedisConnectContext redisConnectContext, String key, String... fields) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.hdel(key, fields),
                commands -> commands.hdel(key, fields));
    }

}
