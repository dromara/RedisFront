package org.dromara.redisfront.service;


import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.service.impl.RedisListServiceImpl;


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
