package com.redisfront.service;

import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisZSetServiceImpl;
import io.lettuce.core.*;

import java.util.List;

public interface RedisZSetService {
    RedisZSetService service = new RedisZSetServiceImpl();

    Long zadd(ConnectInfo connectInfo,String key, double score, String member);

    Long zadd(ConnectInfo connectInfo,String key, ScoredValue<String>... scoredValues);

    Double zaddincr(ConnectInfo connectInfo,String key, double score, String member);

    Long zcard(ConnectInfo connectInfo,String key);

    Long zrem(ConnectInfo connectInfo,String key, String... members);
    List<ScoredValue<String>> zrange(ConnectInfo connectInfo,String key, long start, long stop);

    Long zcount(ConnectInfo connectInfo, String key, Range<? extends Number> range);

    List<String> zrangebyscore(ConnectInfo connectInfo,String key, Range<? extends Number> range, Limit limit);

    List<ScoredValue<String>> zrangebyscoreWithScores(ConnectInfo connectInfo,String key, Range<? extends Number> range);

    List<String> zrevrangebyscore(ConnectInfo connectInfo,String key, Range<? extends Number> range, Limit limit);

    Long zrevrank(ConnectInfo connectInfo,String key, String member);

    ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo,String key);

    ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo,String key, ScanArgs scanArgs);

    ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo,String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo,String key, ScanCursor scanCursor);
    Double zscore(ConnectInfo connectInfo,String key, String member);

    ScoredValue<String> zpopmin(ConnectInfo connectInfo,String key);

    List<ScoredValue<String>> zpopmin(ConnectInfo connectInfo,String key, long count);


}
