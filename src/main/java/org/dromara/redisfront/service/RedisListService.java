package org.dromara.redisfront.service;


import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.service.impl.RedisListServiceImpl;


import java.util.List;

public interface RedisListService {

    RedisListService service = new RedisListServiceImpl();

    List<String> lrange(ConnectContext connectContext, String key, long start, long stop);

    Long lrem(ConnectContext connectContext, String key, long count, String value);

    Long llen(ConnectContext connectContext, String key);

    String lpop(ConnectContext connectContext, String key);

    List<String> lpop(ConnectContext connectContext, String key, long count);

    Long lpush(ConnectContext connectContext, String key, String... values);

    String lset(ConnectContext connectContext, String key, long index, String value);

    String rpop(ConnectContext connectContext, String key);

    List<String> rpop(ConnectContext connectContext, String key, long count);

    Long rpush(ConnectContext connectContext, String key, String... values);

}
