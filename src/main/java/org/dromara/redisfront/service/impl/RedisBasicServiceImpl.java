package org.dromara.redisfront.service.impl;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.output.KeyStreamingChannel;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.ClusterNode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisBasicServiceImpl implements RedisBasicService {

    private static final Logger log = LoggerFactory.getLogger(RedisBasicServiceImpl.class);

    @Override
    public String flushdb(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, RedisAdvancedClusterCommands::flushdb);
        } else {
            return LettuceUtils.exec(redisConnectContext, RedisServerCommands::flushdb);
        }
    }

    @Override
    public Map<String, String> configGet(RedisConnectContext redisConnectContext, String... keys) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.configGet(keys));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.configGet(keys));
        }
    }

    @Override
    public String flushall(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, RedisAdvancedClusterCommands::flushall);
        } else {
            return LettuceUtils.exec(redisConnectContext, RedisServerCommands::flushall);
        }
    }

    @Override
    public KeyScanCursor<String> scan(RedisConnectContext redisConnectContext, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(scanArgs));
        }
    }

    @Override
    public KeyScanCursor<String> scan(RedisConnectContext redisConnectContext, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(scanCursor, scanArgs));
        }
    }

    @Override
    public KeyScanCursor<String> scan(RedisConnectContext redisConnectContext, ScanCursor scanCursor) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(scanCursor));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(scanCursor));
        }
    }

    @Override
    public StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(channel));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(channel));
        }
    }

    @Override
    public StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(channel, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(channel, scanArgs));
        }
    }

    @Override
    public StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(channel, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(channel, scanCursor, scanArgs));
        }
    }

    @Override
    public StreamScanCursor scan(RedisConnectContext redisConnectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.scan(channel, scanCursor));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.scan(channel, scanCursor));
        }
    }

    @Override
    public Long del(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.del(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.del(key));
        }
    }

    @Override
    public String rename(RedisConnectContext redisConnectContext, String key, String newKey) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.rename(key, newKey));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.rename(key, newKey));
        }
    }

    @Override
    public Boolean expire(RedisConnectContext redisConnectContext, String key, Long ttl) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.expire(key, ttl));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.expire(key, ttl));
        }
    }

    @Override
    public String type(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.type(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.type(key));
        }
    }

    @Override
    public Long ttl(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, redisCommands -> redisCommands.ttl(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.ttl(key));
        }
    }

    @Override
    public Boolean ping(RedisConnectContext redisConnectContext) {
        String ping = LettuceUtils.exec(redisConnectContext, BaseRedisCommands::ping);
        return RedisFrontUtils.equal(ping, "PONG");
    }

    @Override
    public RedisMode getRedisModeEnum(RedisConnectContext redisConnectContext) {
        Map<String, Object> server = getServerInfo(redisConnectContext);
        String redisMode = (String) server.get("redis_mode");
        log.debug("获取到Redis [ {}:{} ] 服务类型 - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), redisMode);
        return RedisMode.valueOf(redisMode.toUpperCase());
    }

    @Override
    public List<ClusterNode> getClusterNodes(RedisConnectContext redisConnectContext) {
        String clusterNodes = LettuceUtils.exec(redisConnectContext, RedisClusterCommands::clusterNodes);
        log.debug("获取到Redis [ {}:{} ] clusterNodes - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), clusterNodes);
        return strToClusterNodes(clusterNodes);
    }

    @Override
    public Map<String, Object> getClusterInfo(RedisConnectContext redisConnectContext) {
        var clusterInfo = LettuceUtils.exec(redisConnectContext, RedisClusterCommands::clusterInfo);
        log.debug("获取到Redis [ {}:{} ] ClusterInfo - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), clusterInfo);
        return strToMap(clusterInfo);
    }

    @Override
    public Map<String, Object> getInfo(RedisConnectContext redisConnectContext) {
        var info = LettuceUtils.exec(redisConnectContext, RedisServerCommands::info);
        log.debug("获取到Redis [ {}:{} ] Info - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), info);
        return strToMap(info);
    }

    @Override
    public Map<String, Object> getInfo(RedisConnectContext redisConnectContext, String section) {
        var info = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info(section));
        log.debug("获取到Redis [ {}:{} ] Info - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), info);
        return strToMap(info);
    }

    @Override
    public Map<String, Object> getCpuInfo(RedisConnectContext redisConnectContext) {
        var cpuInfo = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("cpu"));
        log.debug("获取到Redis [ {}:{} ] cpuInfo - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), cpuInfo);
        return strToMap(cpuInfo);
    }

    @Override
    public Map<String, Object> getMemoryInfo(RedisConnectContext redisConnectContext) {
        var memoryInfo = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("memory"));
        log.debug("获取到Redis [ {}:{} ] memoryInfo - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), memoryInfo);
        return strToMap(memoryInfo);
    }

    @Override
    public Map<String, Object> getServerInfo(RedisConnectContext redisConnectContext) {
        var server = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("server"));
        log.debug("获取到Redis [ {}:{} ] serverInfo - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), server);
        return strToMap(server);
    }

    @Override
    public Map<String, Object> getKeySpace(RedisConnectContext redisConnectContext) {
        var keyspace = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("keyspace"));
        log.debug("获取到Redis [ {}:{} ] keyspace - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), keyspace);
        return strToMap(keyspace);
    }

    @Override
    public Map<String, Object> getClientInfo(RedisConnectContext redisConnectContext) {
        var clientInfo = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("clients"));
        log.debug("获取到Redis [ {}:{} ] clientInfo - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), clientInfo);
        return strToMap(clientInfo);
    }

    @Override
    public Map<String, Object> getStatInfo(RedisConnectContext redisConnectContext) {
        var statInfo = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("stats"));
        log.debug("获取到Redis [ {}:{} ] statInfo - {}", redisConnectContext.getHost(), redisConnectContext.getPort(), statInfo);
        return strToMap(statInfo);
    }

    @Override
    public Boolean isClusterMode(RedisConnectContext redisConnectContext) {
        var cluster = LettuceUtils.exec(redisConnectContext, redisCommands -> redisCommands.info("Cluster"));
        return (RedisFrontUtils.equal(strToMap(cluster).get("cluster_enabled"), "1"));
    }


    @Override
    public Long dbSize(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, RedisAdvancedClusterCommands::dbsize);
        } else {
            return LettuceUtils.exec(redisConnectContext, RedisServerCommands::dbsize);
        }
    }


    private List<ClusterNode> strToClusterNodes(String str) {
        List<ClusterNode> clusterNodes = new ArrayList<>();
        for (var s : str.split("\n")) {
            if (RedisFrontUtils.startWith(s, "#") && RedisFrontUtils.isNotEmpty(s)) {
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
