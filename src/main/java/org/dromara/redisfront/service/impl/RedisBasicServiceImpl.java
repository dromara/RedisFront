package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.Enums;
import org.dromara.redisfront.Fn;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.model.ClusterNode;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.model.context.ScanContext;
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
    public String flushdb(ConnectContext connectContext) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("flushdb".toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, RedisAdvancedClusterCommands::flushdb);
        } else {
            return LettuceUtils.exec(connectContext, RedisServerCommands::flushdb);
        }
    }

    @Override
    public Map<String, String> configGet(ConnectContext connectContext, String... keys) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.configGet(keys));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.configGet(keys));
        }
    }

    @Override
    public String flushall(ConnectContext connectContext) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("flushall".toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, RedisAdvancedClusterCommands::flushall);
        } else {
            return LettuceUtils.exec(connectContext, RedisServerCommands::flushall);
        }
    }

    @Override
    public KeyScanCursor<String> scan(ConnectContext connectContext, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SCAN ".concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(scanArgs));
        }
    }

    @Override
    public KeyScanCursor<String> scan(ConnectContext connectContext, ScanCursor scanCursor, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SCAN ".concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(scanCursor, scanArgs));
        }
    }

    @Override
    public KeyScanCursor<String> scan(ConnectContext connectContext, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SCAN ".concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(scanCursor));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(scanCursor));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(channel));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(channel));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel, ScanArgs scanArgs) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(channel, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(channel, scanArgs));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(channel, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(channel, scanCursor, scanArgs));
        }
    }

    @Override
    public StreamScanCursor scan(ConnectContext connectContext, KeyStreamingChannel<String> channel, ScanCursor scanCursor) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.scan(channel, scanCursor));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.scan(channel, scanCursor));
        }
    }

    @Override
    public Long del(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("del ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.del(key));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.del(key));
        }
    }

    @Override
    public String rename(ConnectContext connectContext, String key, String newKey) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("rename ".concat(key).concat(" ").concat(newKey).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.rename(key, newKey));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.rename(key, newKey));
        }
    }

    @Override
    public Boolean expire(ConnectContext connectContext, String key, Long ttl) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("expire ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.expire(key, ttl));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.expire(key, ttl));
        }
    }

    @Override
    public String type(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("type ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.type(key));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.type(key));
        }
    }

    @Override
    public Long ttl(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext)
                .setInfo("ttl ".concat(key).toUpperCase());
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, redisCommands -> redisCommands.ttl(key));
        } else {
            return LettuceUtils.exec(connectContext, redisCommands -> redisCommands.ttl(key));
        }
    }

    @Override
    public Boolean ping(ConnectContext connectContext) {
        String ping = LettuceUtils.exec(connectContext, BaseRedisCommands::ping);
        return Fn.equal(ping, "PONG");
    }

    @Override
    public Enums.RedisMode getRedisModeEnum(ConnectContext connectContext) {
        Map<String, Object> server = getServerInfo(connectContext);
        String redisMode = (String) server.get("redis_mode");
        log.info("获取到Redis [ {}:{} ] 服务类型 - {}", connectContext.getHost(), connectContext.getPort(), redisMode);
        return Enums.RedisMode.valueOf(redisMode.toUpperCase());
    }

    @Override
    public List<ClusterNode> getClusterNodes(ConnectContext connectContext) {

        String clusterNodes = LettuceUtils.exec(connectContext, RedisClusterCommands::clusterNodes);
        return strToClusterNodes(clusterNodes);
    }

    @Override
    public Map<String, Object> getClusterInfo(ConnectContext connectContext) {
        var clusterInfo = LettuceUtils.exec(connectContext, RedisClusterCommands::clusterInfo);
        log.info("获取到Redis [ {}:{} ] ClusterInfo - {}", connectContext.getHost(), connectContext.getPort(), clusterInfo);
        return strToMap(clusterInfo);
    }

    @Override
    public Map<String, Object> getInfo(ConnectContext connectContext) {
        var info = LettuceUtils.exec(connectContext, RedisServerCommands::info);
        log.info("获取到Redis [ {}:{} ] Info - {}", connectContext.getHost(), connectContext.getPort(), info);
        return strToMap(info);
    }

    @Override
    public Map<String, Object> getCpuInfo(ConnectContext connectContext) {
        var cpuInfo = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("cpu"));
        log.debug("获取到Redis [ {}:{} ] cpuInfo - {}", connectContext.getHost(), connectContext.getPort(), cpuInfo);
        return strToMap(cpuInfo);
    }

    @Override
    public Map<String, Object> getMemoryInfo(ConnectContext connectContext) {
        var memoryInfo = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("memory"));
        log.info("获取到Redis [ {}:{} ] memoryInfo - {}", connectContext.getHost(), connectContext.getPort(), memoryInfo);
        return strToMap(memoryInfo);
    }

    @Override
    public Map<String, Object> getServerInfo(ConnectContext connectContext) {
        var server = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("server"));
        log.info("获取到Redis [ {}:{} ] serverInfo - {}", connectContext.getHost(), connectContext.getPort(), server);
        return strToMap(server);
    }

    @Override
    public Map<String, Object> getKeySpace(ConnectContext connectContext) {
        var keyspace = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("keyspace"));
        log.info("获取到Redis [ {}:{} ] keyspace - {}", connectContext.getHost(), connectContext.getPort(), keyspace);
        return strToMap(keyspace);
    }

    @Override
    public Map<String, Object> getClientInfo(ConnectContext connectContext) {
        var clientInfo = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("clients"));
        log.info("获取到Redis [ {}:{} ] clientInfo - {}", connectContext.getHost(), connectContext.getPort(), clientInfo);
        return strToMap(clientInfo);
    }

    @Override
    public Map<String, Object> getStatInfo(ConnectContext connectContext) {
        var statInfo = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("stats"));
        log.info("获取到Redis [ {}:{} ] statInfo - {}", connectContext.getHost(), connectContext.getPort(), statInfo);
        return strToMap(statInfo);
    }

    @Override
    public Boolean isClusterMode(ConnectContext connectContext) {
        var cluster = LettuceUtils.exec(connectContext, redisCommands -> redisCommands.info("Cluster"));
        return (Fn.equal(strToMap(cluster).get("cluster_enabled"), "1"));
    }


    @Override
    public Long dbSize(ConnectContext connectContext) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, RedisAdvancedClusterCommands::dbsize);
        } else {
            return LettuceUtils.exec(connectContext, RedisServerCommands::dbsize);
        }
    }


    private List<ClusterNode> strToClusterNodes(String str) {
        List<ClusterNode> clusterNodes = new ArrayList<>();
        for (var s : str.split("\n")) {
            if (Fn.startWith(s, "#") && Fn.isNotEmpty(s)) {
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
