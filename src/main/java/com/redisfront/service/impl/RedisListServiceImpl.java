package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisListService;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtil;

import java.util.List;

/**
 * RedisListServiceImpl
 *
 * @author Jin
 */
public class RedisListServiceImpl implements RedisListService {
    @Override
    public Long llen(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.llen(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.llen(key));
        }
    }

    @Override
    public String lpop(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.lpop(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.lpop(key));
        }
    }

    @Override
    public List<String> lpop(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.lpop(key, count));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.lpop(key, count));
        }
    }

    @Override
    public Long lpush(ConnectInfo connectInfo, String key, String... values) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.lpush(key, values));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.lpush(key, values));
        }
    }

    @Override
    public String lset(ConnectInfo connectInfo, String key, long index, String value) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.lset(key, index, value));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.lset(key, index, value));
        }
    }

    @Override
    public String rpop(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.rpop(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.rpop(key));
        }
    }

    @Override
    public List<String> rpop(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.rpop(key, count));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.rpop(key, count));
        }
    }

    @Override
    public Long rpush(ConnectInfo connectInfo, String key, String... values) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.rpush(key, values));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.rpush(key, values));
        }
    }
}
