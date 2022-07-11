package com.redisfront.service.impl;

import com.redisfront.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisHashService;
import com.redisfront.util.FunUtil;
import com.redisfront.util.LettuceUtil;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanCursor;

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
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hget(key, field));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hget(key, field));
        }
    }

    @Override
    public Map<String, String> hgetall(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hgetall(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hgetall(key));
        }
    }

    @Override
    public List<String> hkeys(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hkeys(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hkeys(key));
        }
    }

    @Override
    public Long hlen(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hlen(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hlen(key));
        }
    }

    @Override
    public String hmset(ConnectInfo connectInfo, String key, Map<String, String> map) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hmset(key, map));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hmset(key, map));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hscan(key, scanCursor));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hscan(key, scanCursor));
        }
    }

    @Override
    public Boolean hset(ConnectInfo connectInfo, String key, String field, String value) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hset(key, field, value));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hset(key, field, value));
        }
    }

    @Override
    public Long hset(ConnectInfo connectInfo, String key, Map<String, String> map) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hset(key, map));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hset(key, map));
        }
    }

    @Override
    public Long hstrlen(ConnectInfo connectInfo, String key, String field) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hstrlen(key, field));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hstrlen(key, field));
        }
    }

    @Override
    public List<String> hvals(ConnectInfo connectInfo, String key) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hvals(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hvals(key));
        }
    }

    @Override
    public Long hdel(ConnectInfo connectInfo, String key, String... fields) {
        if (FunUtil.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.hdel(key, fields));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.hdel(key, fields));
        }
    }

}
