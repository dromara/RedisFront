package org.dromara.redisfront.service;

import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.ClusterNode;
import org.dromara.redisfront.model.context.RedisConnectContext;
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

    String flushdb(RedisConnectContext redisConnectContext);

    String flushall(RedisConnectContext redisConnectContext);

    KeyScanCursor<String> scan(RedisConnectContext redisConnectContext, ScanArgs scanArgs);

    KeyScanCursor<String> scan(RedisConnectContext redisConnectContext, ScanCursor scanCursor, ScanArgs scanArgs);

    KeyScanCursor<String> scan(RedisConnectContext redisConnectContext, ScanCursor scanCursor);

    StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel);

    StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel, ScanArgs scanArgs);

    StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs);

    Map<String, String> configGet(RedisConnectContext redisConnectContext, String... keys);

    StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor);

    /**
     * del
     *
     * @param redisConnectContext connectInfo
     * @param key         key
     * @return String
     */
    Long del(RedisConnectContext redisConnectContext, String key);

    String rename(RedisConnectContext redisConnectContext, String key, String newKey);


    Boolean expire(RedisConnectContext redisConnectContext, String key, Long ttl);


    /**
     * type
     *
     * @param redisConnectContext connectInfo
     * @param key         key
     * @return String
     */
    String type(RedisConnectContext redisConnectContext, String key);

    /**
     * ttl
     *
     * @param redisConnectContext connectInfo
     * @param key         key
     * @return String
     */
    Long ttl(RedisConnectContext redisConnectContext, String key);

    /**
     * redis ping
     *
     * @param redisConnectContext 连接信息
     * @return Boolean
     */
    Boolean ping(RedisConnectContext redisConnectContext);

    /**
     * 获取 redisMode
     *
     * @param redisConnectContext 连接信息
     * @return Enum.RedisMode
     */
    RedisMode getRedisModeEnum(RedisConnectContext redisConnectContext);

    /**
     * 获取集群节点
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    List<ClusterNode> getClusterNodes(RedisConnectContext redisConnectContext);


    /**
     * 获取集群信息
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getClusterInfo(RedisConnectContext redisConnectContext);

    /**
     * 获取info
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getInfo(RedisConnectContext redisConnectContext);

    /**
     * 获取cpu info
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getCpuInfo(RedisConnectContext redisConnectContext);

    /**
     * 获取memory info
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getMemoryInfo(RedisConnectContext redisConnectContext);

    /**
     * 获取 server info
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getServerInfo(RedisConnectContext redisConnectContext);

    /**
     * 获取单机 KeySpace
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getKeySpace(RedisConnectContext redisConnectContext);

    /**
     * 获取 client info
     *
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getClientInfo(RedisConnectContext redisConnectContext);

    /**
     * @param redisConnectContext 连接信息
     * @return Map
     */
    Map<String, Object> getStatInfo(RedisConnectContext redisConnectContext);

    Boolean isClusterMode(RedisConnectContext redisConnectContext);

    Long dbSize(RedisConnectContext redisConnectContext);

    default Map<String, Object> strToMap(String str) {
        Map<String, Object> result = new HashMap<>();
        String[] tokens = str.split("\r\n");
        for (String token : tokens) {
            if (!Fn.startWith(token, "#") && Fn.isNotEmpty(token)) {
                String[] values = token.split(":");
                if (values.length > 1) {
                    result.put(values[0], values[1]);
                } else {
                    result.put(values[0], "");
                }
            }
        }
        return result;
    }

    static LogInfo buildLogInfo(RedisConnectContext redisConnectContext) {
        return new LogInfo().setDate(LocalDateTime.now()).setIp(redisConnectContext.getHost());
    }


}
