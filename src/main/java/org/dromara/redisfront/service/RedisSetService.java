package org.dromara.redisfront.service;

import org.dromara.redisfront.model.context.ConnectContext;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import org.dromara.redisfront.service.impl.RedisSetServiceImpl;

import java.util.List;
import java.util.Set;

public interface RedisSetService {

    RedisSetService service = new RedisSetServiceImpl();

    Long sadd(ConnectContext connectContext, String key, String... members);

    Long scard(ConnectContext connectContext, String key);

    Set<String> sdiff(ConnectContext connectContext, String... keys);


    Long sdiffstore(ConnectContext connectContext, String destination, String... keys);


    Set<String> sinter(ConnectContext connectContext, String... keys);

    Long sinterstore(ConnectContext connectContext, String destination, String... keys);

    Boolean sismember(ConnectContext connectContext, String key, String member);

    Set<String> smembers(ConnectContext connectContext, String key);

    List<Boolean> smismember(ConnectContext connectContext, String key, String... members);

    Boolean smove(ConnectContext connectContext, String source, String destination, String member);

    String spop(ConnectContext connectContext, String key);

    Set<String> spop(ConnectContext connectContext, String key, long count);

    String srandmember(ConnectContext connectContext, String key);

    List<String> srandmember(ConnectContext connectContext, String key, long count);


    Long srem(ConnectContext connectContext, String key, String... members);

    Set<String> sunion(ConnectContext connectContext, String... keys);


    ValueScanCursor<String> sscan(ConnectContext connectContext, String key);

    ValueScanCursor<String> sscan(ConnectContext connectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ValueScanCursor<String> sscan(ConnectContext connectContext, String key, ScanCursor scanCursor);


}
