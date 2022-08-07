package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.service.RedisStringService;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.ui.dialog.LogsDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisStringServiceImpl implements RedisStringService {

    private static final Logger log = LoggerFactory.getLogger(RedisStringServiceImpl.class);


    @Override
    public String set(ConnectInfo connectInfo, String key, String value) {


        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("SET ".concat(key).concat(" ").concat(value));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.set(key, value));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.set(key, value));
        }
    }

    @Override
    public String get(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("GET ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.get(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.get(key));
        }
    }

    @Override
    public Long strlen(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("STRLEN ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.strlen(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.strlen(key));
        }
    }

    @Override
    public String setex(ConnectInfo connectInfo, String key, long seconds, String value) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.setex(key, seconds, value));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.setex(key, seconds, value));
        }
    }


}
