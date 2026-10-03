package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisZSetService;
import io.lettuce.core.*;

import java.util.List;

/**
 * RedisZSetServiceImpl
 *
 * @author Jin
 */
public class RedisZSetServiceImpl implements RedisZSetService {
    @Override
    public Long zadd(RedisConnectContext redisConnectContext, String key, double score, byte[] member) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zadd(key, score, member),
                commands -> commands.zadd(key, score, member));
    }


    @SafeVarargs
    @Override
    public final Long zadd(RedisConnectContext redisConnectContext, String key, ScoredValue<byte[]>... scoredValues) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zadd(key, scoredValues),
                commands -> commands.zadd(key, scoredValues));
    }

    @Override
    public Double zaddincr(RedisConnectContext redisConnectContext, String key, double score, byte[] member) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zaddincr(key, score, member),
                commands -> commands.zaddincr(key, score, member));
    }

    @Override
    public Long zcard(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zcard(key),
                commands -> commands.zcard(key));
    }

    @Override
    public Long zrem(RedisConnectContext redisConnectContext, String key, byte[]... members) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zrem(key, members),
                commands -> commands.zrem(key, members));
    }

    @Override
    public List<ScoredValue<byte[]>> zrange(RedisConnectContext redisConnectContext, String key, long start, long stop) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zrangeWithScores(key, start, stop),
                commands -> commands.zrangeWithScores(key, start, stop));
    }

    @Override
    public Long zcount(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zcount(key, range),
                commands -> commands.zcount(key, range));
    }

    @Override
    public List<byte[]> zrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zrangebyscore(key, range, limit),
                commands -> commands.zrangebyscore(key, range, limit));
    }

    @Override
    public List<ScoredValue<byte[]>> zrangebyscoreWithScores(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zrangebyscoreWithScores(key, range),
                commands -> commands.zrangebyscoreWithScores(key, range));
    }

    @Override
    public List<byte[]> zrevrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zrevrangebyscore(key, range, limit),
                commands -> commands.zrevrangebyscore(key, range, limit));
    }

    @Override
    public Long zrevrank(RedisConnectContext redisConnectContext, String key, byte[] member) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zrevrank(key, member),
                commands -> commands.zrevrank(key, member));
    }

    @Override
    public ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zscan(key),
                commands -> commands.zscan(key));
    }

    @Override
    public ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key, ScanArgs scanArgs) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zscan(key, scanArgs),
                commands -> commands.zscan(key, scanArgs));
    }

    @Override
    public ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zscan(key, scanCursor, scanArgs),
                commands -> commands.zscan(key, scanCursor, scanArgs));
    }

    @Override
    public ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zscan(key, scanCursor),
                commands -> commands.zscan(key, scanCursor));
    }

    @Override
    public Double zscore(RedisConnectContext redisConnectContext, String key, byte[] member) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zscore(key, member),
                commands -> commands.zscore(key, member));
    }

    @Override
    public ScoredValue<byte[]> zpopmin(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zpopmin(key),
                commands -> commands.zpopmin(key));
    }

    @Override
    public List<ScoredValue<byte[]>> zpopmin(RedisConnectContext redisConnectContext, String key, long count) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.zpopmin(key, count),
                commands -> commands.zpopmin(key, count));
    }
}
