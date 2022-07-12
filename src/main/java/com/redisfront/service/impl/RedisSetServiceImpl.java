package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisSetService;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtil;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;

import java.util.List;
import java.util.Set;

/**
 * RedisSetServiceImpl
 *
 * @author Jin
 */
public class RedisSetServiceImpl implements RedisSetService {
    @Override
    public Long sadd(ConnectInfo connectInfo, String key, String... members) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sadd(key, members));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sadd(key, members));
        }
    }

    @Override
    public Long scard(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.scard(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.scard(key));
        }
    }

    @Override
    public Set<String> sdiff(ConnectInfo connectInfo, String... keys) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sdiff(keys));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sdiff(keys));
        }
    }

    @Override
    public Long sdiffstore(ConnectInfo connectInfo, String destination, String... keys) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sdiffstore(destination, keys));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sdiffstore(destination, keys));
        }
    }

    @Override
    public Set<String> sinter(ConnectInfo connectInfo, String... keys) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sinter(keys));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sinter(keys));
        }
    }

    @Override
    public Long sinterstore(ConnectInfo connectInfo, String destination, String... keys) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sinterstore(destination, keys));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sinterstore(destination, keys));
        }
    }

    @Override
    public Boolean sismember(ConnectInfo connectInfo, String key, String member) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sismember(key, member));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sismember(key, member));
        }
    }

    @Override
    public Set<String> smembers(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.smembers(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.smembers(key));
        }
    }

    @Override
    public List<Boolean> smismember(ConnectInfo connectInfo, String key, String... members) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.smismember(key, members));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.smismember(key, members));
        }
    }

    @Override
    public Boolean smove(ConnectInfo connectInfo, String source, String destination, String member) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.smove(source, destination, member));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.smove(source, destination, member));
        }
    }

    @Override
    public String spop(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.spop(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.spop(key));
        }
    }

    @Override
    public Set<String> spop(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.spop(key, count));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.spop(key, count));
        }
    }

    @Override
    public String srandmember(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.srandmember(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.srandmember(key));
        }
    }

    @Override
    public List<String> srandmember(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.srandmember(key, count));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.srandmember(key, count));
        }
    }

    @Override
    public Long srem(ConnectInfo connectInfo, String key, String... members) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.srem(key, members));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.srem(key, members));
        }
    }

    @Override
    public Set<String> sunion(ConnectInfo connectInfo, String... keys) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sunion(keys));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sunion(keys));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sscan(key));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sscan(key));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sscan(key,scanCursor,scanArgs));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sscan(key,scanCursor,scanArgs));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtil.clusterExec(connectInfo, commands -> commands.sscan(key,scanCursor));
        } else {
            return LettuceUtil.exec(connectInfo, commands -> commands.sscan(key,scanCursor));
        }
    }
}
