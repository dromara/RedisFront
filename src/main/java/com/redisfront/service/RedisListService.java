package com.redisfront.service;


import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisListServiceImpl;
import io.lettuce.core.output.ValueStreamingChannel;

import java.util.List;

public interface RedisListService {

    RedisListService service = new RedisListServiceImpl();

    List<String> lrange(ConnectInfo connectInfo, String key, long start, long stop);

    Long lrem(ConnectInfo connectInfo,String key, long count, String value);

    Long llen(ConnectInfo connectInfo, String key);

    String lpop(ConnectInfo connectInfo, String key);

    List<String> lpop(ConnectInfo connectInfo, String key, long count);

    Long lpush(ConnectInfo connectInfo, String key, String... values);

    String lset(ConnectInfo connectInfo, String key, long index, String value);

    String rpop(ConnectInfo connectInfo, String key);

    List<String> rpop(ConnectInfo connectInfo, String key, long count);

    Long rpush(ConnectInfo connectInfo, String key, String... values);

}
