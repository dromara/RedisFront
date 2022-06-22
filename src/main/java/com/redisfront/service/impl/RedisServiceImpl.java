package com.redisfront.service.impl;

import com.redisfront.constant.RedisModeEnum;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
import com.redisfront.util.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ClusterPipeline;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.providers.ClusterConnectionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisServiceImpl implements RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Override
    public ClusterPipeline getClusterPipeline(ConnectInfo connectInfo) {
        Set<HostAndPort> clusterNodes = getClusterNodes(connectInfo)
                .stream()
                .map(e -> new HostAndPort(connectInfo.host(), e.port()))
                .collect(Collectors.toSet());
        ClusterConnectionProvider clusterConnectionProvider = new ClusterConnectionProvider(clusterNodes, getJedisClientConfig(connectInfo));
        return new ClusterPipeline(clusterConnectionProvider);
    }

    @Override
    public JedisCluster getJedisCluster(ConnectInfo connectInfo) {
        Set<HostAndPort> clusterNodes = getClusterNodes(connectInfo)
                .stream()
                .map(e -> new HostAndPort(connectInfo.host(), e.port()))
                .collect(Collectors.toSet());
        log.info("获取到Redis [ {}:{} ] 集群节点 - {}", connectInfo.host(), connectInfo.port(), clusterNodes);
        return new JedisCluster(clusterNodes, getJedisClientConfig(connectInfo));
    }

    @Override
    public Boolean ping(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            return Fn.equal(jedis.ping(), "PONG");
        }
    }

    @Override
    public RedisModeEnum getRedisModeEnum(ConnectInfo connectInfo) {
        Map<String, Object> server = getServerInfo(connectInfo);
        String redisMode = (String) server.get("redis_mode");
        log.info("获取到Redis [ {}:{} ] 服务类型 - {}", connectInfo.host(), connectInfo.port(), redisMode);
        return RedisModeEnum.valueOf(redisMode.toUpperCase());
    }

    @Override
    public List<ClusterNode> getClusterNodes(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var clusterNodes = jedis.clusterNodes();
            return strToClusterNodes(clusterNodes);
        }
    }

    private List<ClusterNode> strToClusterNodes(String str) {
        List<ClusterNode> clusterNodes = new ArrayList<>();
        for (var s : str.split("\n")) {
            if (!Fn.startWith(s, "#") && Fn.isNotEmpty(s)) {
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

    @Override
    public Map<String, Object> getClusterInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var clusterInfo = jedis.clusterInfo();
            log.info("获取到Redis [ {}:{} ] ClusterInfo - {}", connectInfo.host(), connectInfo.port(), clusterInfo);
            return strToMap(clusterInfo);
        }
    }

    @Override
    public Map<String, Object> getInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var info = jedis.info();
            log.info("获取到Redis [ {}:{} ] Info - {}", connectInfo.host(), connectInfo.port(), info);
            return strToMap(info);
        }
    }

    @Override
    public Map<String, Object> getCpuInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var cpuInfo = jedis.info("cpu");
            log.info("获取到Redis [ {}:{} ] cpuInfo - {}", connectInfo.host(), connectInfo.port(), cpuInfo);
            return strToMap(cpuInfo);
        }
    }

    @Override
    public Map<String, Object> getMemoryInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var memoryInfo = jedis.info("memory");
            log.info("获取到Redis [ {}:{} ] memoryInfo - {}", connectInfo.host(), connectInfo.port(), memoryInfo);
            return strToMap(memoryInfo);
        }
    }

    @Override
    public Map<String, Object> getServerInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var server = jedis.info("server");
            log.info("获取到Redis [ {}:{} ] serverInfo - {}", connectInfo.host(), connectInfo.port(), server);
            return strToMap(server);
        }
    }

    @Override
    public Map<String, Object> getKeySpace(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var keyspace = jedis.info("keyspace");
            log.info("获取到Redis [ {}:{} ] keyspace - {}", connectInfo.host(), connectInfo.port(), keyspace);
            return strToMap(keyspace);
        }
    }

    @Override
    public Map<String, Object> getClientInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var clientInfo = jedis.clientInfo();
            log.info("获取到Redis [ {}:{} ] clientInfo - {}", connectInfo.host(), connectInfo.port(), clientInfo);
            return strToMap(clientInfo);
        }
    }

    @Override
    public Map<String, Object> getStatInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var statInfo = jedis.info("stats");
            log.info("获取到Redis [ {}:{} ] statInfo - {}", connectInfo.host(), connectInfo.port(), statInfo);
            return strToMap(statInfo);
        }
    }

    @Override
    public Boolean isClusterMode(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var cluster = jedis.info("Cluster");
            return (Fn.equal(strToMap(cluster).get("cluster_enabled"), "1"));
        }
    }

    @Override
    public Long getKeyCount(ConnectInfo connectInfo) {
        if (isClusterMode(connectInfo)) {
            var clusterNodes = getClusterNodes(connectInfo);
            return clusterNodes
                    .stream()
                    .map(clusterNode -> {
                        try (var jedis = new Jedis(connectInfo.host(), clusterNode.port(), getJedisClientConfig(connectInfo))) {
                            return strToMap(jedis.info("keyspace"));
                        }
                    })
                    .map(this::countKeys)
                    .reduce(Long::sum)
                    .orElse(0L);
        } else {
            var keySpace = RedisService.service.getKeySpace(connectInfo);
            return countKeys(keySpace);
        }
    }

    private Long countKeys(Map<String, Object> keySpace) {
        return keySpace.values()
                .stream()
                .map(s -> s.toString().split(",")[0])
                .map(s -> Long.parseLong(s.replace("keys=", "")))
                .reduce(Long::sum)
                .orElse(0L);
    }

}
