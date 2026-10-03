package org.dromara.redisfront.service.impl;

import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisSetService;

import java.util.List;
import java.util.Set;

/**
 * RedisSetServiceImpl
 *
 * @author Jin
 */
public class RedisSetServiceImpl implements RedisSetService {
    @Override
    public Long sadd(RedisConnectContext redisConnectContext, String key, byte[]... members) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sadd(key, members),
                commands -> commands.sadd(key, members));
    }

    @Override
    public Long scard(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.scard(key),
                commands -> commands.scard(key));
    }

    @Override
    public Set<byte[]> sdiff(RedisConnectContext redisConnectContext, String... keys) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sdiff(keys),
                commands -> commands.sdiff(keys));
    }

    @Override
    public Long sdiffstore(RedisConnectContext redisConnectContext, String destination, String... keys) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sdiffstore(destination, keys),
                commands -> commands.sdiffstore(destination, keys));
    }

    @Override
    public Set<byte[]> sinter(RedisConnectContext redisConnectContext, String... keys) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sinter(keys),
                commands -> commands.sinter(keys));
    }

    @Override
    public Long sinterstore(RedisConnectContext redisConnectContext, String destination, String... keys) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sinterstore(destination, keys),
                commands -> commands.sinterstore(destination, keys));
    }

    @Override
    public Boolean sismember(RedisConnectContext redisConnectContext, String key, byte[] member) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sismember(key, member),
                commands -> commands.sismember(key, member));
    }

    @Override
    public Set<byte[]> smembers(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.smembers(key),
                commands -> commands.smembers(key));
    }

    @Override
    public List<Boolean> smismember(RedisConnectContext redisConnectContext, String key, byte[]... members) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.smismember(key, members),
                commands -> commands.smismember(key, members));
    }

    @Override
    public Boolean smove(RedisConnectContext redisConnectContext, String source, String destination, byte[] member) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.smove(source, destination, member),
                commands -> commands.smove(source, destination, member));
    }

    @Override
    public byte[] spop(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.spop(key),
                commands -> commands.spop(key));
    }

    @Override
    public Set<byte[]> spop(RedisConnectContext redisConnectContext, String key, long count) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.spop(key, count),
                commands -> commands.spop(key, count));
    }

    @Override
    public byte[] srandmember(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.srandmember(key),
                commands -> commands.srandmember(key));
    }

    @Override
    public List<byte[]> srandmember(RedisConnectContext redisConnectContext, String key, long count) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.srandmember(key, count),
                commands -> commands.srandmember(key, count));
    }

    @Override
    public Long srem(RedisConnectContext redisConnectContext, String key, byte[]... members) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.srem(key, members),
                commands -> commands.srem(key, members));
    }

    @Override
    public Set<byte[]> sunion(RedisConnectContext redisConnectContext, String... keys) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sunion(keys),
                commands -> commands.sunion(keys));
    }

    @Override
    public ValueScanCursor<byte[]> sscan(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sscan(key),
                commands -> commands.sscan(key));
    }

    @Override
    public ValueScanCursor<byte[]> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sscan(key, scanCursor, scanArgs),
                commands -> commands.sscan(key, scanCursor, scanArgs));
    }

    @Override
    public ValueScanCursor<byte[]> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.sscan(key, scanCursor),
                commands -> commands.sscan(key, scanCursor));
    }
}
