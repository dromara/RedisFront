package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.*;
import org.dromara.redisfront.service.impl.RedisZSetServiceImpl;

import java.util.List;

public interface RedisZSetService {
    RedisZSetService service = new RedisZSetServiceImpl();

    Long zadd(RedisConnectContext redisConnectContext, String key, double score, String member);

    Long zadd(RedisConnectContext redisConnectContext, String key, ScoredValue<String>... scoredValues);

    Double zaddincr(RedisConnectContext redisConnectContext, String key, double score, String member);

    Long zcard(RedisConnectContext redisConnectContext, String key);

    Long zrem(RedisConnectContext redisConnectContext, String key, String... members);
    List<ScoredValue<String>> zrange(RedisConnectContext redisConnectContext, String key, long start, long stop);

    Long zcount(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range);

    List<String> zrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit);

    List<ScoredValue<String>> zrangebyscoreWithScores(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range);

    List<String> zrevrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit);

    Long zrevrank(RedisConnectContext redisConnectContext, String key, String member);

    ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key);

    ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key, ScanArgs scanArgs);

    ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor);
    Double zscore(RedisConnectContext redisConnectContext, String key, String member);

    ScoredValue<String> zpopmin(RedisConnectContext redisConnectContext, String key);

    List<ScoredValue<String>> zpopmin(RedisConnectContext redisConnectContext, String key, long count);


}
