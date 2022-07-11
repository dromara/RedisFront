package com.redisfront.service.impl;

import com.redisfront.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisStringService;
import com.redisfront.util.FunUtil;
import com.redisfront.util.LettuceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisStringServiceImpl implements RedisStringService {

    private static final Logger log = LoggerFactory.getLogger(RedisStringServiceImpl.class);


    @Override
    public String set(ConnectInfo connectInfo, String key, String value) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.set(key, value));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.set(key, value));
        }
    }

    @Override
    public String get(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.get(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.get(key));
        }
    }

    @Override
    public Long strlen(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.strlen(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.strlen(key));
        }
    }

    @Override
    public String setex(ConnectInfo connectInfo, String key, long seconds, String value) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.setex(key, seconds, value));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.setex(key, seconds, value));
        }
    }


}
