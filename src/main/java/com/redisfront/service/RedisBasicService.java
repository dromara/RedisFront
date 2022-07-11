package com.redisfront.service;

import com.redisfront.constant.Enum;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisBasicServiceImpl;
import com.redisfront.util.FunUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RedisBasicService {

    RedisBasicService service = new RedisBasicServiceImpl();


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
            if (!FunUtil.startWith(s, "#") && FunUtil.isNotEmpty(s)) {
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


}
