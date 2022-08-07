package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.service.RedisListService;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.ui.dialog.LogsDialog;

import java.util.Arrays;
import java.util.List;

/**
 * RedisListServiceImpl
 *
 * @author Jin
 */
public class RedisListServiceImpl implements RedisListService {
    @Override
    public List<String> lrange(ConnectInfo connectInfo, String key, long start, long stop) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("LRANGE ".concat(key).concat(" ").concat(String.valueOf(start)).concat(" ").concat(String.valueOf(stop)));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.lrange(key, start, stop));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.lrange(key, start, stop));
        }
    }

    @Override
    public Long lrem(ConnectInfo connectInfo, String key, long count, String value) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("LREM ".concat(key).concat(" ").concat(String.valueOf(count)).concat(" ").concat(String.valueOf(value)));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.lrem(key, count, value));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.lrem(key, count, value));
        }
    }

    @Override
    public Long llen(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("LLEN ".concat(key).concat(" "));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.llen(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.llen(key));
        }
    }

    @Override
    public String lpop(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.lpop(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.lpop(key));
        }
    }

    @Override
    public List<String> lpop(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.lpop(key, count));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.lpop(key, count));
        }
    }

    @Override
    public Long lpush(ConnectInfo connectInfo, String key, String... values) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("LPUSH ".concat(key).concat(" ").concat(Arrays.toString(values).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.lpush(key, values));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.lpush(key, values));
        }
    }

    @Override
    public String lset(ConnectInfo connectInfo, String key, long index, String value) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.lset(key, index, value));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.lset(key, index, value));
        }
    }

    @Override
    public String rpop(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.rpop(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.rpop(key));
        }
    }

    @Override
    public List<String> rpop(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.rpop(key, count));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.rpop(key, count));
        }
    }

    @Override
    public Long rpush(ConnectInfo connectInfo, String key, String... values) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.rpush(key, values));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.rpush(key, values));
        }
    }
}
