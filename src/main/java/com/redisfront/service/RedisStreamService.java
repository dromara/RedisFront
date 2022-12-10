package com.redisfront.service;

import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisStreamServiceImpl;
import io.lettuce.core.*;

import java.util.List;
import java.util.Map;

/**
 * RedisStreamService
 *
 * @author Jin
 */
public interface RedisStreamService {

    RedisStreamService service = new RedisStreamServiceImpl();

    Long xdel(ConnectInfo connectInfo, String key, String... messageIds);

    Long xack(ConnectInfo connectInfo, String key, String group, String... messageIds);

    String xadd(ConnectInfo connectInfo, String key, Map<String, String> body);

    String xadd(ConnectInfo connectInfo, String id, String key, Map<String, String> body);

    List<StreamMessage<String, String>> xrange(ConnectInfo connectInfo, String key, Range<String> range, Limit limit);

    String xadd(ConnectInfo connectInfo, String key, XAddArgs args, Object... keysAndValues);

    String xgroupCreate(ConnectInfo connectInfo, XReadArgs.StreamOffset<String> streamOffset, String group);

    String xgroupCreate(ConnectInfo connectInfo, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args);

    Long xlen(ConnectInfo connectInfo, String key);

}
