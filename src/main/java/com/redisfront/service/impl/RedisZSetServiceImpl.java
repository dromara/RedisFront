package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.model.ScanContext;
import com.redisfront.service.RedisBasicService;
import com.redisfront.service.RedisZSetService;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.*;

import java.util.Arrays;
import java.util.List;

/**
 * RedisZSetServiceImpl
 *
 * @author Jin
 */
public class RedisZSetServiceImpl implements RedisZSetService {
    @Override
    public Long zadd(ConnectInfo connectInfo, String key, double score, String member) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("ZADD ".concat(key).concat(" ").concat(String.valueOf(score)).concat(" ").concat(member));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zadd(key, score, member));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zadd(key, score, member));
        }
    }


    @SafeVarargs
    @Override
    public final Long zadd(ConnectInfo connectInfo, String key, ScoredValue<String>... scoredValues) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zadd(key, scoredValues));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zadd(key, scoredValues));
        }
    }

    @Override
    public Double zaddincr(ConnectInfo connectInfo, String key, double score, String member) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zaddincr(key, score, member));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zaddincr(key, score, member));
        }
    }

    @Override
    public Long zcard(ConnectInfo connectInfo, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("ZCARD ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zcard(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zcard(key));
        }
    }

    @Override
    public Long zrem(ConnectInfo connectInfo, String key, String... members) {
        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("ZREM ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zrem(key, members));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zrem(key, members));
        }
    }

    @Override
    public List<ScoredValue<String>> zrange(ConnectInfo connectInfo, String key, long start, long stop) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zrangeWithScores(key, start, stop));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zrangeWithScores(key, start, stop));
        }
    }

    @Override
    public Long zcount(ConnectInfo connectInfo, String key, Range<? extends Number> range) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zcount(key, range));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zcount(key, range));
        }
    }

    @Override
    public List<String> zrangebyscore(ConnectInfo connectInfo, String key, Range<? extends Number> range, Limit limit) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zrangebyscore(key, range, limit));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zrangebyscore(key, range, limit));
        }
    }

    @Override
    public List<ScoredValue<String>> zrangebyscoreWithScores(ConnectInfo connectInfo, String key, Range<? extends Number> range) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zrangebyscoreWithScores(key, range));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zrangebyscoreWithScores(key, range));
        }
    }

    @Override
    public List<String> zrevrangebyscore(ConnectInfo connectInfo, String key, Range<? extends Number> range, Limit limit) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zrevrangebyscore(key, range, limit));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zrevrangebyscore(key, range, limit));
        }
    }

    @Override
    public Long zrevrank(ConnectInfo connectInfo, String key, String member) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zrevrank(key, member));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zrevrank(key, member));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zscan(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zscan(key));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo, String key, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("ZSCAN ".concat(key).concat(" ").concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zscan(key, scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zscan(key, scanArgs));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor, ScanArgs scanArgs) {


        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("ZSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectInfo connectInfo, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectInfo).setInfo("ZSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zscan(key, scanCursor));
        }
    }

    @Override
    public Double zscore(ConnectInfo connectInfo, String key, String member) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zscore(key, member));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zscore(key, member));
        }
    }

    @Override
    public ScoredValue<String> zpopmin(ConnectInfo connectInfo, String key) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zpopmin(key));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zpopmin(key));
        }
    }

    @Override
    public List<ScoredValue<String>> zpopmin(ConnectInfo connectInfo, String key, long count) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, commands -> commands.zpopmin(key, count));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.zpopmin(key, count));
        }
    }
}
