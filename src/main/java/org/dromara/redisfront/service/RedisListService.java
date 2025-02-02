package org.dromara.redisfront.service;


import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.impl.RedisListServiceImpl;


import java.util.List;

public interface RedisListService {

    RedisListService service = new RedisListServiceImpl();

    List<String> lrange(RedisConnectContext redisConnectContext, String key, long start, long stop);

    Long lrem(RedisConnectContext redisConnectContext, String key, long count, String value);

    Long llen(RedisConnectContext redisConnectContext, String key);

    String lpop(RedisConnectContext redisConnectContext, String key);

    List<String> lpop(RedisConnectContext redisConnectContext, String key, long count);

    Long lpush(RedisConnectContext redisConnectContext, String key, String... values);

    String lset(RedisConnectContext redisConnectContext, String key, long index, String value);

    String rpop(RedisConnectContext redisConnectContext, String key);

    List<String> rpop(RedisConnectContext redisConnectContext, String key, long count);

    Long rpush(RedisConnectContext redisConnectContext, String key, String... values);

}
