package com.redisfront.service.impl;

import com.redisfront.common.func.Fn;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
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
        return new JedisCluster(clusterNodes, getJedisClientConfig(connectInfo));
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
            return strToMap(clusterInfo);
        }
    }

    @Override
    public Map<String, Object> getInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var info = jedis.info();
            return strToMap(info);
        }
    }

    @Override
    public Map<String, Object> getCpuInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var cpuInfo = jedis.info("cpu");
            return strToMap(cpuInfo);
        }
    }

    @Override
    public Map<String, Object> getMemoryInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var memoryInfo = jedis.info("memory");
            return strToMap(memoryInfo);
        }
    }

    @Override
    public Map<String, Object> getServerInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var server = jedis.info("server");
            return strToMap(server);
        }
    }

    @Override
    public Map<String, Object> getKeySpace(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var server = jedis.info("keyspace");
            return strToMap(server);
        }
    }

    @Override
    public Map<String, Object> getClientInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var clientInfo = jedis.clientInfo();
            return strToMap(clientInfo);
        }
    }

    @Override
    public Map<String, Object> getStatInfo(ConnectInfo connectInfo) {
        try (var jedis = new Jedis(connectInfo.host(), connectInfo.port(), getJedisClientConfig(connectInfo))) {
            var statInfo = jedis.info("stats");
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
