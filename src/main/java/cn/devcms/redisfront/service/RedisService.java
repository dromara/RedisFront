package cn.devcms.redisfront.service;

import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.model.ClusterNode;
import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.service.impl.RedisServiceImpl;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisClientConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RedisService {

    RedisService service = new RedisServiceImpl();

    List<ClusterNode> getClusterNodes(ConnectInfo connectInfo);

    Map<String, Object> getClusterInfo(ConnectInfo connectInfo);

    Map<String, Object> getInfo(ConnectInfo connectInfo);

    Map<String, Object> getCpuInfo(ConnectInfo connectInfo);

    Map<String, Object> getMemoryInfo(ConnectInfo connectInfo);

    Map<String, Object> getServerInfo(ConnectInfo connectInfo);

    Map<String, Object> getKeySpace(ConnectInfo connectInfo);

    Map<String, Object> getClientInfo(ConnectInfo connectInfo);

    Map<String, Object> getStatInfo(ConnectInfo connectInfo);

    Boolean isClusterMode(ConnectInfo connectInfo);

    Long getKeyCount(ConnectInfo connectInfo);

    default Map<String, Object> strToMap(String str) {
        Map<String, Object> result = new HashMap<>();
        for (String s : str.split("\r\n")) {
            if (!Fn.startWith(s, "#") && Fn.isNotEmpty(s)) {
                String[] v = s.split(":");
                if (v.length > 1) {
                    result.put(v[0], v[1]);
                } else {
                    result.put(v[0], "");
                }
            }
        }
        return result;
    }

    default JedisClientConfig getJedisClientConfig(ConnectInfo connectInfo) {
        return DefaultJedisClientConfig
                .builder()
                .database(connectInfo.database())
                .user(connectInfo.user())
                .password(connectInfo.password())
                .build();
    }


}
