package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.*;
import org.dromara.redisfront.service.impl.RedisStreamServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * RedisStreamService
 *
 * @author Jin
 */
public interface RedisStreamService {

    RedisStreamService service = new RedisStreamServiceImpl();

    Long xdel(RedisConnectContext redisConnectContext, String key, String... messageIds);

    Long xack(RedisConnectContext redisConnectContext, String key, String group, String... messageIds);

    String xadd(RedisConnectContext redisConnectContext, String key, Map<String, String> body);

    String xadd(RedisConnectContext redisConnectContext, String id, String key, Map<String, String> body);

    List<StreamMessage<String, String>> xrange(RedisConnectContext redisConnectContext, String key, Range<String> range, Limit limit);

    String xadd(RedisConnectContext redisConnectContext, String key, XAddArgs args, Object... keysAndValues);

    String xgroupCreate(RedisConnectContext redisConnectContext, XReadArgs.StreamOffset<String> streamOffset, String group);

    String xgroupCreate(RedisConnectContext redisConnectContext, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args);

    Long xlen(RedisConnectContext redisConnectContext, String key);

}
