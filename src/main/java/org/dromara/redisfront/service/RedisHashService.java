package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.ConnectContext;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.service.impl.RedisHashServiceImpl;

import java.util.List;
import java.util.Map;

public interface RedisHashService {

    RedisHashService service = new RedisHashServiceImpl();

    String hget(ConnectContext connectContext, String key, String field);

    Map<String, String> hgetall(ConnectContext connectContext, String key);

    List<String> hkeys(ConnectContext connectContext, String key);

    Long hlen(ConnectContext connectContext, String key);

    String hmset(ConnectContext connectContext, String key, Map<String, String> map);

    MapScanCursor<String, String> hscan(ConnectContext connectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);
    MapScanCursor<String, String> hscan(ConnectContext connectContext, String key, ScanCursor scanCursor);

    Boolean hset(ConnectContext connectContext, String key, String field, String value);

    Long hset(ConnectContext connectContext, String key, Map<String, String> map);

    Long hstrlen(ConnectContext connectContext, String key, String field);

    List<String> hvals(ConnectContext connectContext, String key);

    Long hdel(ConnectContext connectContext, String key, String... fields);
}
