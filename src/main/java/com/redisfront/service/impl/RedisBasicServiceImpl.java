package com.redisfront.service.impl;

import com.redisfront.constant.Enum;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.util.FunUtil;
import com.redisfront.util.LettuceUtil;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisBasicServiceImpl implements RedisBasicService {

    private static final Logger log = LoggerFactory.getLogger(RedisBasicServiceImpl.class);

    @Override
    public String type(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, redisCommands -> redisCommands.type(key));
        } else {
            return LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.type(key));
        }
    }

    @Override
    public Long ttl(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, redisCommands -> redisCommands.ttl(key));
        } else {
            return LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.ttl(key));
        }
    }

    @Override
    public Boolean ping(ConnectInfo connectInfo) {
        String ping = LettuceUtil.exec(connectInfo, BaseRedisCommands::ping);
        return FunUtil.equal(ping, "PONG");
    }

    @Override
    public Enum.RedisMode getRedisModeEnum(ConnectInfo connectInfo) {
        Map<String, Object> server = getServerInfo(connectInfo);
        String redisMode = (String) server.get("redis_mode");
        log.info("获取到Redis [ {}:{} ] 服务类型 - {}", connectInfo.host(), connectInfo.port(), redisMode);
        return Enum.RedisMode.valueOf(redisMode.toUpperCase());
    }

    @Override
    public List<ClusterNode> getClusterNodes(ConnectInfo connectInfo) {
        String clusterNodes = LettuceUtil.exec(connectInfo, RedisClusterCommands::clusterNodes);
        return strToClusterNodes(clusterNodes);
    }

    @Override
    public Map<String, Object> getClusterInfo(ConnectInfo connectInfo) {
        var clusterInfo = LettuceUtil.exec(connectInfo, RedisClusterCommands::clusterInfo);
        log.info("获取到Redis [ {}:{} ] ClusterInfo - {}", connectInfo.host(), connectInfo.port(), clusterInfo);
        return strToMap(clusterInfo);
    }

    @Override
    public Map<String, Object> getInfo(ConnectInfo connectInfo) {
        var info = LettuceUtil.exec(connectInfo, RedisServerCommands::info);
        log.info("获取到Redis [ {}:{} ] Info - {}", connectInfo.host(), connectInfo.port(), info);
        return strToMap(info);
    }

    @Override
    public Map<String, Object> getCpuInfo(ConnectInfo connectInfo) {
        var cpuInfo = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("cpu"));
        log.info("获取到Redis [ {}:{} ] serverInfo - {}", connectInfo.host(), connectInfo.port(), cpuInfo);
        return strToMap(cpuInfo);
    }

    @Override
    public Map<String, Object> getMemoryInfo(ConnectInfo connectInfo) {
        var memoryInfo = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("memory"));
        log.info("获取到Redis [ {}:{} ] memoryInfo - {}", connectInfo.host(), connectInfo.port(), memoryInfo);
        return strToMap(memoryInfo);
    }

    @Override
    public Map<String, Object> getServerInfo(ConnectInfo connectInfo) {
        var server = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("server"));
        log.info("获取到Redis [ {}:{} ] serverInfo - {}", connectInfo.host(), connectInfo.port(), server);
        return strToMap(server);
    }

    @Override
    public Map<String, Object> getKeySpace(ConnectInfo connectInfo) {
        var keyspace = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("keyspace"));
        log.info("获取到Redis [ {}:{} ] keyspace - {}", connectInfo.host(), connectInfo.port(), keyspace);
        return strToMap(keyspace);
    }

    @Override
    public Map<String, Object> getClientInfo(ConnectInfo connectInfo) {
        var clientInfo = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("client"));
        log.info("获取到Redis [ {}:{} ] clientInfo - {}", connectInfo.host(), connectInfo.port(), clientInfo);
        return strToMap(clientInfo);
    }

    @Override
    public Map<String, Object> getStatInfo(ConnectInfo connectInfo) {
        var statInfo = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("stats"));
        log.info("获取到Redis [ {}:{} ] statInfo - {}", connectInfo.host(), connectInfo.port(), statInfo);
        return strToMap(statInfo);
    }

    @Override
    public Boolean isClusterMode(ConnectInfo connectInfo) {
        var cluster = LettuceUtil.exec(connectInfo, redisCommands -> redisCommands.info("Cluster"));
        return (FunUtil.equal(strToMap(cluster).get("cluster_enabled"), "1"));
    }


    @Override
    public Long dbSize(ConnectInfo connectInfo) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, RedisAdvancedClusterCommands::dbsize);
        } else {
            return LettuceUtil.exec(connectInfo, RedisServerCommands::dbsize);
        }
    }


    private List<ClusterNode> strToClusterNodes(String str) {
        List<ClusterNode> clusterNodes = new ArrayList<>();
        for (var s : str.split("\n")) {
            if (!FunUtil.startWith(s, "#") && FunUtil.isNotEmpty(s)) {
                var v = s.split(" ");
                var clusterNode = new ClusterNode()
                        .setId(v[0])
                        .setIpAndPort(v[1])
                        .setFlags(v[2])
                        .setMaster(v[3])
                        .setPing(v[4])
                        .setPong(v[5])
                        .setEpoch(v[6])
                        .setState(v[7])
                        .setSlot(v.length == 9 ? v[8] : null)
                        .setHost(v[1].split("@")[0].split(":")[0])
                        .setPort(Integer.valueOf(v[1].split("@")[0].split(":")[1]));
                clusterNodes.add(clusterNode);
            }
        }
        return clusterNodes;
    }

}
