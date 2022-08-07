package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.ScanContext;
import com.redisfront.service.RedisBasicService;
import com.redisfront.service.RedisHashService;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * RedisHashServiceImpl
 *
 * @author Jin
 */
public class RedisHashServiceImpl implements RedisHashService {

    @Override
    public String hget(ConnectInfo connectInfo, String key, String field) {

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hget(key, field));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hget(key, field));
        }
    }

    @Override
    public Map<String, String> hgetall(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hgetall(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hgetall(key));
        }
    }

    @Override
    public List<String> hkeys(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hkeys(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hkeys(key));
        }
    }

    @Override
    public Long hlen(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hlen(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hlen(key));
        }
    }

    @Override
    public String hmset(ConnectInfo connectInfo, String key, Map<String, String> map) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hmset(key, map));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hmset(key, map));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("HSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("HSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hscan(key, scanCursor));
        }
    }

    @Override
    public Boolean hset(ConnectInfo connectInfo, String key, String field, String value) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("HSET ".concat(key).concat(" ").concat(field).concat(" ").concat(value));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hset(key, field, value));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hset(key, field, value));
        }
    }

    @Override
    public Long hset(ConnectInfo connectInfo, String key, Map<String, String> map) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hset(key, map));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hset(key, map));
        }
    }

    @Override
    public Long hstrlen(ConnectInfo connectInfo, String key, String field) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hstrlen(key, field));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hstrlen(key, field));
        }
    }

    @Override
    public List<String> hvals(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hvals(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hvals(key));
        }
    }

    @Override
    public Long hdel(ConnectInfo connectInfo, String key, String... fields) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("HSET ".concat(key).concat(" ").concat(Arrays.toString(fields).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.hdel(key, fields));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.hdel(key, fields));
        }
    }

}
