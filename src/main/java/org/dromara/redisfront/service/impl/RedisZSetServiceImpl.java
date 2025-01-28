package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.model.context.ScanContext;
import org.dromara.redisfront.service.RedisZSetService;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.*;
import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;

import java.util.Arrays;
import java.util.List;

/**
 * RedisZSetServiceImpl
 *
 * @author Jin
 */
public class RedisZSetServiceImpl implements RedisZSetService {
    @Override
    public Long zadd(ConnectContext connectContext, String key, double score, String member) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("ZADD ".concat(key).concat(" ").concat(String.valueOf(score)).concat(" ").concat(member));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zadd(key, score, member));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zadd(key, score, member));
        }
    }


    @SafeVarargs
    @Override
    public final Long zadd(ConnectContext connectContext, String key, ScoredValue<String>... scoredValues) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zadd(key, scoredValues));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zadd(key, scoredValues));
        }
    }

    @Override
    public Double zaddincr(ConnectContext connectContext, String key, double score, String member) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zaddincr(key, score, member));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zaddincr(key, score, member));
        }
    }

    @Override
    public Long zcard(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("ZCARD ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zcard(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zcard(key));
        }
    }

    @Override
    public Long zrem(ConnectContext connectContext, String key, String... members) {
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("ZREM ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zrem(key, members));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zrem(key, members));
        }
    }

    @Override
    public List<ScoredValue<String>> zrange(ConnectContext connectContext, String key, long start, long stop) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zrangeWithScores(key, start, stop));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zrangeWithScores(key, start, stop));
        }
    }

    @Override
    public Long zcount(ConnectContext connectContext, String key, Range<? extends Number> range) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zcount(key, range));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zcount(key, range));
        }
    }

    @Override
    public List<String> zrangebyscore(ConnectContext connectContext, String key, Range<? extends Number> range, Limit limit) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zrangebyscore(key, range, limit));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zrangebyscore(key, range, limit));
        }
    }

    @Override
    public List<ScoredValue<String>> zrangebyscoreWithScores(ConnectContext connectContext, String key, Range<? extends Number> range) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zrangebyscoreWithScores(key, range));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zrangebyscoreWithScores(key, range));
        }
    }

    @Override
    public List<String> zrevrangebyscore(ConnectContext connectContext, String key, Range<? extends Number> range, Limit limit) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zrevrangebyscore(key, range, limit));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zrevrangebyscore(key, range, limit));
        }
    }

    @Override
    public Long zrevrank(ConnectContext connectContext, String key, String member) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zrevrank(key, member));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zrevrank(key, member));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zscan(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zscan(key));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("ZSCAN ".concat(key).concat(" ").concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zscan(key, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zscan(key, scanArgs));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {


        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("ZSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(ConnectContext connectContext, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("ZSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zscan(key, scanCursor));
        }
    }

    @Override
    public Double zscore(ConnectContext connectContext, String key, String member) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zscore(key, member));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zscore(key, member));
        }
    }

    @Override
    public ScoredValue<String> zpopmin(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zpopmin(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zpopmin(key));
        }
    }

    @Override
    public List<ScoredValue<String>> zpopmin(ConnectContext connectContext, String key, long count) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.zpopmin(key, count));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.zpopmin(key, count));
        }
    }
}
