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

    Long sadd(RedisConnectContext redisConnectContext, String key, byte[]... members);

    Long scard(RedisConnectContext redisConnectContext, String key);

    Set<byte[]> sdiff(RedisConnectContext redisConnectContext, String... keys);


    Long sdiffstore(RedisConnectContext redisConnectContext, String destination, String... keys);


    Set<byte[]> sinter(RedisConnectContext redisConnectContext, String... keys);

    Long sinterstore(RedisConnectContext redisConnectContext, String destination, String... keys);

    Boolean sismember(RedisConnectContext redisConnectContext, String key, byte[] member);

    Set<byte[]> smembers(RedisConnectContext redisConnectContext, String key);

    List<Boolean> smismember(RedisConnectContext redisConnectContext, String key, byte[]... members);

    Boolean smove(RedisConnectContext redisConnectContext, String source, String destination, byte[] member);

    byte[] spop(RedisConnectContext redisConnectContext, String key);

    Set<byte[]> spop(RedisConnectContext redisConnectContext, String key, long count);

    byte[] srandmember(RedisConnectContext redisConnectContext, String key);

    List<byte[]> srandmember(RedisConnectContext redisConnectContext, String key, long count);


    Long srem(RedisConnectContext redisConnectContext, String key, byte[]... members);

    Set<byte[]> sunion(RedisConnectContext redisConnectContext, String... keys);


    ValueScanCursor<byte[]> sscan(RedisConnectContext redisConnectContext, String key);

    ValueScanCursor<byte[]> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ValueScanCursor<byte[]> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor);


}
