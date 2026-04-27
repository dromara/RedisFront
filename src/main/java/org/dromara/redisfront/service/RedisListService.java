package org.dromara.redisfront.service;


import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.impl.RedisListServiceImpl;


import java.util.List;

public interface RedisListService {

    RedisListService service = new RedisListServiceImpl();

    List<byte[]> lrange(RedisConnectContext redisConnectContext, String key, long start, long stop);

    Long lrem(RedisConnectContext redisConnectContext, String key, long count, byte[] value);

    Long llen(RedisConnectContext redisConnectContext, String key);

    byte[] lpop(RedisConnectContext redisConnectContext, String key);

    List<byte[]> lpop(RedisConnectContext redisConnectContext, String key, long count);

    Long lpush(RedisConnectContext redisConnectContext, String key, byte[]... values);

    String lset(RedisConnectContext redisConnectContext, String key, long index, byte[] value);

    byte[] rpop(RedisConnectContext redisConnectContext, String key);

    List<byte[]> rpop(RedisConnectContext redisConnectContext, String key, long count);

    Long rpush(RedisConnectContext redisConnectContext, String key, byte[]... values);

}
