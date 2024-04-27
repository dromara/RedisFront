package org.dromara.redisfront.service;

import org.dromara.redisfront.model.ConnectInfo;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.service.impl.RedisHashServiceImpl;

import java.util.List;
import java.util.Map;

public interface RedisHashService {

    RedisHashService service = new RedisHashServiceImpl();

    String hget(ConnectInfo connectInfo, String key, String field);

    Map<String, String> hgetall(ConnectInfo connectInfo, String key);

    List<String> hkeys(ConnectInfo connectInfo, String key);

    Long hlen(ConnectInfo connectInfo, String key);

    String hmset(ConnectInfo connectInfo, String key, Map<String, String> map);

    MapScanCursor<String, String> hscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor, ScanArgs scanArgs);
    MapScanCursor<String, String> hscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor);

    Boolean hset(ConnectInfo connectInfo, String key, String field, String value);

    Long hset(ConnectInfo connectInfo, String key, Map<String, String> map);

    Long hstrlen(ConnectInfo connectInfo, String key, String field);

    List<String> hvals(ConnectInfo connectInfo, String key);

    Long hdel(ConnectInfo connectInfo, String key, String... fields);
}
