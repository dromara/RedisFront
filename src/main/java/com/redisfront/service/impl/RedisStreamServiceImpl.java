package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisStreamService;
import io.lettuce.core.*;

import java.util.List;
import java.util.Map;

/**
 * RedisStreamService
 *
 * @author Jin
 */
public class RedisStreamServiceImpl implements RedisStreamService {

    @Override
    public Long xdel(ConnectInfo connectInfo, String key, String... messageIds) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xdel(key, messageIds));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xdel(key, messageIds));
        }
    }

    @Override
    public Long xack(ConnectInfo connectInfo, String key, String group, String... messageIds) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xack(key, group, messageIds));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xack(key, group, messageIds));
        }
    }

    @Override
    public String xadd(ConnectInfo connectInfo, String key, Map<String, String> body) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xadd(key, new XAddArgs(), body));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xadd(key, body));
        }
    }

    @Override
    public String xadd(ConnectInfo connectInfo, String id, String key, Map<String, String> body) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
        }
    }

    @Override
    public List<StreamMessage<String, String>> xrange(ConnectInfo connectInfo, String key, Range<String> range, Limit limit) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xrange(key, range, limit));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xrange(key, range, limit));
        }
    }

    @Override
    public String xadd(ConnectInfo connectInfo, String key, XAddArgs args, Object... keysAndValues) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xadd(key, args, keysAndValues));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xadd(key, args, keysAndValues));
        }
    }

    @Override
    public String xgroupCreate(ConnectInfo connectInfo, XReadArgs.StreamOffset<String> streamOffset, String group) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xgroupCreate(streamOffset, group));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xgroupCreate(streamOffset, group));
        }
    }

    @Override
    public String xgroupCreate(ConnectInfo connectInfo, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xgroupCreate(streamOffset, group, args));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xgroupCreate(streamOffset, group, args));
        }
    }

    @Override
    public Long xlen(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.xlen(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.xlen(key));
        }
    }
}
