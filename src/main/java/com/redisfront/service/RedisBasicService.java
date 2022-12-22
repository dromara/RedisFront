package com.redisfront.service;

import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.LogInfo;
import com.redisfront.service.impl.RedisBasicServiceImpl;
import com.redisfront.commons.func.Fn;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.output.KeyStreamingChannel;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RedisBasicService {

    RedisBasicService service = new RedisBasicServiceImpl();

    String flushdb(ConnectInfo connectInfo);

    String flushall(ConnectInfo connectInfo);

    KeyScanCursor<String> scan(ConnectInfo connectInfo, ScanArgs scanArgs);

    KeyScanCursor<String> scan(ConnectInfo connectInfo, ScanCursor scanCursor, ScanArgs scanArgs);

    KeyScanCursor<String> scan(ConnectInfo connectInfo, ScanCursor scanCursor);

    StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel);

    StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel, ScanArgs scanArgs);

    StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs);

    Map<String, String> configGet(ConnectInfo connectInfo, String... keys);
    StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel, ScanCursor scanCursor);

    /**
     * del
     *
     * @param connectInfo connectInfo
     * @param key         key
     * @return String
     */
    Long del(ConnectInfo connectInfo, String key);

    String rename(ConnectInfo connectInfo, String key, String newKey);


    Boolean expire(ConnectInfo connectInfo, String key, Long ttl);


    /**
     * type
     *
     * @param connectInfo connectInfo
     * @param key         key
     * @return String
     */
    String type(ConnectInfo connectInfo, String key);

    /**
     * ttl
     *
     * @param connectInfo connectInfo
     * @param key         key
     * @return String
     */
    Long ttl(ConnectInfo connectInfo, String key);

    /**
     * redis ping
     *
     * @param connectInfo 连接信息
     * @return Boolean
     */
    Boolean ping(ConnectInfo connectInfo);

    /**
     * 获取 redisMode
     *
     * @param connectInfo 连接信息
     * @return Enum.RedisMode
     */
    Enum.RedisMode getRedisModeEnum(ConnectInfo connectInfo);

    /**
     * 获取集群节点
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    List<ClusterNode> getClusterNodes(ConnectInfo connectInfo);


    /**
     * 获取集群信息
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getClusterInfo(ConnectInfo connectInfo);

    /**
     * 获取info
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getInfo(ConnectInfo connectInfo);

    /**
     * 获取cpu info
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getCpuInfo(ConnectInfo connectInfo);

    /**
     * 获取memory info
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getMemoryInfo(ConnectInfo connectInfo);

    /**
     * 获取 server info
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getServerInfo(ConnectInfo connectInfo);

    /**
     * 获取单机 KeySpace
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getKeySpace(ConnectInfo connectInfo);

    /**
     * 获取 client info
     *
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getClientInfo(ConnectInfo connectInfo);

    /**
     * @param connectInfo 连接信息
     * @return Map
     */
    Map<String, Object> getStatInfo(ConnectInfo connectInfo);

    Boolean isClusterMode(ConnectInfo connectInfo);

    Long dbSize(ConnectInfo connectInfo);

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

    static LogInfo buildLogInfo(ConnectInfo connectInfo) {
        return new LogInfo().setDate(LocalDateTime.now()).setIp(connectInfo.host());
    }


}
