package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.ConnectContext;
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

    Long xdel(ConnectContext connectContext, String key, String... messageIds);

    Long xack(ConnectContext connectContext, String key, String group, String... messageIds);

    String xadd(ConnectContext connectContext, String key, Map<String, String> body);

    String xadd(ConnectContext connectContext, String id, String key, Map<String, String> body);

    List<StreamMessage<String, String>> xrange(ConnectContext connectContext, String key, Range<String> range, Limit limit);

    String xadd(ConnectContext connectContext, String key, XAddArgs args, Object... keysAndValues);

    String xgroupCreate(ConnectContext connectContext, XReadArgs.StreamOffset<String> streamOffset, String group);

    String xgroupCreate(ConnectContext connectContext, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args);

    Long xlen(ConnectContext connectContext, String key);

}
