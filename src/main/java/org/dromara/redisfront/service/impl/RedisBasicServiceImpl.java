package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.commons.util.LettuceUtils;
import org.dromara.redisfront.model.ClusterNode;
import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.model.ScanContext;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.api.sync.RedisServerCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import io.lettuce.core.output.KeyStreamingChannel;
import org.dromara.redisfront.service.RedisBasicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisBasicServiceImpl implements RedisBasicService {

    private static final Logger log = LoggerFactory.getLogger(RedisBasicServiceImpl.class);

    @Override
    public String flushdb(ConnectInfo connectInfo) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("flushdb".toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, RedisAdvancedClusterCommands::flushdb);
        } else {
            return LettuceUtils.exec(connectInfo, RedisServerCommands::flushdb);
        }
    }

    @Override
    public Map<String, String> configGet(ConnectInfo connectInfo, String... keys) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.configGet(keys));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.configGet(keys));
        }
    }

    @Override
    public String flushall(ConnectInfo connectInfo) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("flushall".toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, RedisAdvancedClusterCommands::flushall);
        } else {
            return LettuceUtils.exec(connectInfo, RedisServerCommands::flushall);
        }
    }

    @Override
    public KeyScanCursor<String> scan(ConnectInfo connectInfo, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("SCAN ".concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(scanArgs));
        }
    }

    @Override
    public KeyScanCursor<String> scan(ConnectInfo connectInfo, ScanCursor scanCursor, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("SCAN ".concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(scanCursor, scanArgs));
        }
    }

    @Override
    public KeyScanCursor<String> scan(ConnectInfo connectInfo, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("SCAN ".concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(scanCursor));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(scanCursor));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(channel));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(channel));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel, ScanArgs scanArgs) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(channel, scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(channel, scanArgs));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(channel, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(channel, scanCursor, scanArgs));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectInfo connectInfo, KeyStreamingChannel<String> channel, ScanCursor scanCursor) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.scan(channel, scanCursor));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.scan(channel, scanCursor));
        }
    }

    @Override
    public Long del(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("del ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.del(key));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.del(key));
        }
    }

    @Override
    public String rename(ConnectInfo connectInfo, String key, String newKey) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("rename ".concat(key).concat(" ").concat(newKey).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.rename(key, newKey));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.rename(key, newKey));
        }
    }

    @Override
    public Boolean expire(ConnectInfo connectInfo, String key, Long ttl) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("expire ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.expire(key, ttl));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.expire(key, ttl));
        }
    }

    @Override
    public String type(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("type ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.type(key));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.type(key));
        }
    }

    @Override
    public Long ttl(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo)
                .setInfo("ttl ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, redisCommands -> redisCommands.ttl(key));
        } else {
            return LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.ttl(key));
        }
    }

    @Override
    public Boolean ping(ConnectInfo connectInfo) {
        String ping = LettuceUtils.exec(connectInfo, BaseRedisCommands::ping);
        return Fn.equal(ping, "PONG");
    }

    @Override
    public Enums.RedisMode getRedisModeEnum(ConnectInfo connectInfo) {
        Map<String, Object> server = getServerInfo(connectInfo);
        String redisMode = (String) server.get("redis_mode");
        log.info("获取到Redis [ {}:{} ] 服务类型 - {}", connectInfo.getHost(), connectInfo.getPort(), redisMode);
        return Enums.RedisMode.valueOf(redisMode.toUpperCase());
    }

    @Override
    public List<ClusterNode> getClusterNodes(ConnectInfo connectInfo) {

        String clusterNodes = LettuceUtils.exec(connectInfo, RedisClusterCommands::clusterNodes);
        return strToClusterNodes(clusterNodes);
    }

    @Override
    public Map<String, Object> getClusterInfo(ConnectInfo connectInfo) {
        var clusterInfo = LettuceUtils.exec(connectInfo, RedisClusterCommands::clusterInfo);
        log.info("获取到Redis [ {}:{} ] ClusterInfo - {}", connectInfo.getHost(), connectInfo.getPort(), clusterInfo);
        return strToMap(clusterInfo);
    }

    @Override
    public Map<String, Object> getInfo(ConnectInfo connectInfo) {
        var info = LettuceUtils.exec(connectInfo, RedisServerCommands::info);
        log.info("获取到Redis [ {}:{} ] Info - {}", connectInfo.getHost(), connectInfo.getPort(), info);
        return strToMap(info);
    }

    @Override
    public Map<String, Object> getCpuInfo(ConnectInfo connectInfo) {
        var cpuInfo = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("cpu"));
        log.info("获取到Redis [ {}:{} ] cpuInfo - {}", connectInfo.getHost(), connectInfo.getPort(), cpuInfo);
        return strToMap(cpuInfo);
    }

    @Override
    public Map<String, Object> getMemoryInfo(ConnectInfo connectInfo) {
        var memoryInfo = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("memory"));
        log.info("获取到Redis [ {}:{} ] memoryInfo - {}", connectInfo.getHost(), connectInfo.getPort(), memoryInfo);
        return strToMap(memoryInfo);
    }

    @Override
    public Map<String, Object> getServerInfo(ConnectInfo connectInfo) {
        var server = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("server"));
        log.info("获取到Redis [ {}:{} ] serverInfo - {}", connectInfo.getHost(), connectInfo.getPort(), server);
        return strToMap(server);
    }

    @Override
    public Map<String, Object> getKeySpace(ConnectInfo connectInfo) {
        var keyspace = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("keyspace"));
        log.info("获取到Redis [ {}:{} ] keyspace - {}", connectInfo.getHost(), connectInfo.getPort(), keyspace);
        return strToMap(keyspace);
    }

    @Override
    public Map<String, Object> getClientInfo(ConnectInfo connectInfo) {
        var clientInfo = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("clients"));
        log.info("获取到Redis [ {}:{} ] clientInfo - {}", connectInfo.getHost(), connectInfo.getPort(), clientInfo);
        return strToMap(clientInfo);
    }

    @Override
    public Map<String, Object> getStatInfo(ConnectInfo connectInfo) {
        var statInfo = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("stats"));
        log.info("获取到Redis [ {}:{} ] statInfo - {}", connectInfo.getHost(), connectInfo.getPort(), statInfo);
        return strToMap(statInfo);
    }

    @Override
    public Boolean isClusterMode(ConnectInfo connectInfo) {
        var cluster = LettuceUtils.exec(connectInfo, redisCommands -> redisCommands.info("Cluster"));
        return (Fn.equal(strToMap(cluster).get("cluster_enabled"), "1"));
    }


    @Override
    public Long dbSize(ConnectInfo connectInfo) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, RedisAdvancedClusterCommands::dbsize);
        } else {
            return LettuceUtils.exec(connectInfo, RedisServerCommands::dbsize);
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

}
