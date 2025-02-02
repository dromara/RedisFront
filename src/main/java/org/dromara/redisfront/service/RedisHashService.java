package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.service.impl.RedisHashServiceImpl;

import java.util.List;
import java.util.Map;

public interface RedisHashService {

    RedisHashService service = new RedisHashServiceImpl();

    String hget(RedisConnectContext redisConnectContext, String key, String field);

    Map<String, String> hgetall(RedisConnectContext redisConnectContext, String key);

    List<String> hkeys(RedisConnectContext redisConnectContext, String key);

    Long hlen(RedisConnectContext redisConnectContext, String key);

    String hmset(RedisConnectContext redisConnectContext, String key, Map<String, String> map);

    MapScanCursor<String, String> hscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);
    MapScanCursor<String, String> hscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor);

    Boolean hset(RedisConnectContext redisConnectContext, String key, String field, String value);

    Long hset(RedisConnectContext redisConnectContext, String key, Map<String, String> map);

    Long hstrlen(RedisConnectContext redisConnectContext, String key, String field);

    List<String> hvals(RedisConnectContext redisConnectContext, String key);

    Long hdel(RedisConnectContext redisConnectContext, String key, String... fields);
}
