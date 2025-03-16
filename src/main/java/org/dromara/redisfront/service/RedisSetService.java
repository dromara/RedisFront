package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import org.dromara.redisfront.service.impl.RedisSetServiceImpl;

import java.util.List;
import java.util.Set;

public interface RedisSetService {

    RedisSetService service = new RedisSetServiceImpl();

    Long sadd(RedisConnectContext redisConnectContext, String key, String... members);

    Long scard(RedisConnectContext redisConnectContext, String key);

    Set<String> sdiff(RedisConnectContext redisConnectContext, String... keys);


    Long sdiffstore(RedisConnectContext redisConnectContext, String destination, String... keys);


    Set<String> sinter(RedisConnectContext redisConnectContext, String... keys);

    Long sinterstore(RedisConnectContext redisConnectContext, String destination, String... keys);

    Boolean sismember(RedisConnectContext redisConnectContext, String key, String member);

    Set<String> smembers(RedisConnectContext redisConnectContext, String key);

    List<Boolean> smismember(RedisConnectContext redisConnectContext, String key, String... members);

    Boolean smove(RedisConnectContext redisConnectContext, String source, String destination, String member);

    String spop(RedisConnectContext redisConnectContext, String key);

    Set<String> spop(RedisConnectContext redisConnectContext, String key, long count);

    String srandmember(RedisConnectContext redisConnectContext, String key);

    List<String> srandmember(RedisConnectContext redisConnectContext, String key, long count);


    Long srem(RedisConnectContext redisConnectContext, String key, String... members);

    Set<String> sunion(RedisConnectContext redisConnectContext, String... keys);


    ValueScanCursor<String> sscan(RedisConnectContext redisConnectContext, String key);

    ValueScanCursor<String> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ValueScanCursor<String> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor);


}
