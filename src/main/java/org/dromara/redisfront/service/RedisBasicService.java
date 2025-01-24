package org.dromara.redisfront.service;

import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.model.ClusterNode;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.model.LogInfo;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.output.KeyStreamingChannel;
import org.dromara.redisfront.service.impl.RedisBasicServiceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RedisBasicService {

    RedisBasicService service = new RedisBasicServiceImpl();

    String flushdb(ConnectContext connectContext);

    String flushall(ConnectContext connectContext);

    KeyScanCursor<String> scan(ConnectContext connectContext, ScanArgs scanArgs);

    KeyScanCursor<String> scan(ConnectContext connectContext, ScanCursor scanCursor, ScanArgs scanArgs);

    KeyScanCursor<String> scan(ConnectContext connectContext, ScanCursor scanCursor);

    StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel);

    StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel, ScanArgs scanArgs);

    StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs);

    Map<String, String> configGet(ConnectContext connectContext, String... keys);

    StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor);

    /**
     * del
     *
     * @param connectContext connectInfo
     * @param key         key
     * @return String
     */
    Long del(ConnectContext connectContext, String key);

    String rename(ConnectContext connectContext, String key, String newKey);


    Boolean expire(ConnectContext connectContext, String key, Long ttl);


    /**
     * type
     *
     * @param connectContext connectInfo
     * @param key         key
     * @return String
     */
    String type(ConnectContext connectContext, String key);

    /**
     * ttl
     *
     * @param connectContext connectInfo
     * @param key         key
     * @return String
     */
    Long ttl(ConnectContext connectContext, String key);

    /**
     * redis ping
     *
     * @param connectContext 连接信息
     * @return Boolean
     */
    Boolean ping(ConnectContext connectContext);

    /**
     * 获取 redisMode
     *
     * @param connectContext 连接信息
     * @return Enum.RedisMode
     */
    Enums.RedisMode getRedisModeEnum(ConnectContext connectContext);

    /**
     * 获取集群节点
     *
     * @param connectContext 连接信息
     * @return Map
     */
    List<ClusterNode> getClusterNodes(ConnectContext connectContext);


    /**
     * 获取集群信息
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getClusterInfo(ConnectContext connectContext);

    /**
     * 获取info
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getInfo(ConnectContext connectContext);

    /**
     * 获取cpu info
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getCpuInfo(ConnectContext connectContext);

    /**
     * 获取memory info
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getMemoryInfo(ConnectContext connectContext);

    /**
     * 获取 server info
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getServerInfo(ConnectContext connectContext);

    /**
     * 获取单机 KeySpace
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getKeySpace(ConnectContext connectContext);

    /**
     * 获取 client info
     *
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getClientInfo(ConnectContext connectContext);

    /**
     * @param connectContext 连接信息
     * @return Map
     */
    Map<String, Object> getStatInfo(ConnectContext connectContext);

    Boolean isClusterMode(ConnectContext connectContext);

    Long dbSize(ConnectContext connectContext);

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

    static LogInfo buildLogInfo(ConnectContext connectContext) {
        return new LogInfo().setDate(LocalDateTime.now()).setIp(connectContext.getHost());
    }


}
