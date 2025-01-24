package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.ConnectContext;
import io.lettuce.core.*;
import org.dromara.redisfront.service.impl.RedisZSetServiceImpl;

import java.util.List;

public interface RedisZSetService {
    RedisZSetService service = new RedisZSetServiceImpl();

    Long zadd(ConnectContext connectContext, String key, double score, String member);

    Long zadd(ConnectContext connectContext, String key, ScoredValue<String>... scoredValues);

    Double zaddincr(ConnectContext connectContext, String key, double score, String member);

    Long zcard(ConnectContext connectContext, String key);

    Long zrem(ConnectContext connectContext, String key, String... members);
    List<ScoredValue<String>> zrange(ConnectContext connectContext, String key, long start, long stop);

    Long zcount(ConnectContext connectContext, String key, Range<? extends Number> range);

    List<String> zrangebyscore(ConnectContext connectContext, String key, Range<? extends Number> range, Limit limit);

    List<ScoredValue<String>> zrangebyscoreWithScores(ConnectContext connectContext, String key, Range<? extends Number> range);

    List<String> zrevrangebyscore(ConnectContext connectContext, String key, Range<? extends Number> range, Limit limit);

    Long zrevrank(ConnectContext connectContext, String key, String member);

    ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key);

    ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key, ScanArgs scanArgs);

    ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key, ScanCursor scanCursor);
    Double zscore(ConnectContext connectContext, String key, String member);

    ScoredValue<String> zpopmin(ConnectContext connectContext, String key);

    List<ScoredValue<String>> zpopmin(ConnectContext connectContext, String key, long count);


}
