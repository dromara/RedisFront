package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.*;
import org.dromara.redisfront.service.impl.RedisZSetServiceImpl;

import java.util.List;

public interface RedisZSetService {
    RedisZSetService service = new RedisZSetServiceImpl();

    Long zadd(RedisConnectContext redisConnectContext, String key, double score, byte[] member);

    Long zadd(RedisConnectContext redisConnectContext, String key, ScoredValue<byte[]>... scoredValues);

    Double zaddincr(RedisConnectContext redisConnectContext, String key, double score, byte[] member);

    Long zcard(RedisConnectContext redisConnectContext, String key);

    Long zrem(RedisConnectContext redisConnectContext, String key, byte[]... members);
    List<ScoredValue<byte[]>> zrange(RedisConnectContext redisConnectContext, String key, long start, long stop);

    Long zcount(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range);

    List<byte[]> zrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit);

    List<ScoredValue<byte[]>> zrangebyscoreWithScores(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range);

    List<byte[]> zrevrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit);

    Long zrevrank(RedisConnectContext redisConnectContext, String key, byte[] member);

    ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key);

    ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key, ScanArgs scanArgs);

    ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ScoredValueScanCursor<byte[]> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor);
    Double zscore(RedisConnectContext redisConnectContext, String key, byte[] member);

    ScoredValue<byte[]> zpopmin(RedisConnectContext redisConnectContext, String key);

    List<ScoredValue<byte[]>> zpopmin(RedisConnectContext redisConnectContext, String key, long count);


}
