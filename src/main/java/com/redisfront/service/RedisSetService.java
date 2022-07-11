package com.redisfront.service;

import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisSetServiceImpl;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;

import java.util.List;
import java.util.Set;

public interface RedisSetService {

    RedisSetService service = new RedisSetServiceImpl();

    Long sadd(ConnectInfo connectInfo, String key, String... members);

    Long scard(ConnectInfo connectInfo, String key);

    Set<String> sdiff(ConnectInfo connectInfo, String... keys);


    Long sdiffstore(ConnectInfo connectInfo, String destination, String... keys);


    Set<String> sinter(ConnectInfo connectInfo, String... keys);

    Long sinterstore(ConnectInfo connectInfo, String destination, String... keys);

    Boolean sismember(ConnectInfo connectInfo, String key, String member);

    Set<String> smembers(ConnectInfo connectInfo, String key);

    List<Boolean> smismember(ConnectInfo connectInfo, String key, String... members);

    Boolean smove(ConnectInfo connectInfo, String source, String destination, String member);

    String spop(ConnectInfo connectInfo, String key);

    Set<String> spop(ConnectInfo connectInfo, String key, long count);

    String srandmember(ConnectInfo connectInfo, String key);

    List<String> srandmember(ConnectInfo connectInfo, String key, long count);


    Long srem(ConnectInfo connectInfo, String key, String... members);

    Set<String> sunion(ConnectInfo connectInfo, String... keys);


    ValueScanCursor<String> sscan(ConnectInfo connectInfo, String key);

    ValueScanCursor<String> sscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor, ScanArgs scanArgs);

    ValueScanCursor<String> sscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor);


}
