package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.scanner.context.RedisScanContext;
import org.dromara.redisfront.service.RedisZSetService;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.*;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
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
    public Long zadd(RedisConnectContext redisConnectContext, String key, double score, String member) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("ZADD ".concat(key).concat(" ").concat(String.valueOf(score)).concat(" ").concat(member));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zadd(key, score, member));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zadd(key, score, member));
        }
    }


    @SafeVarargs
    @Override
    public final Long zadd(RedisConnectContext redisConnectContext, String key, ScoredValue<String>... scoredValues) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zadd(key, scoredValues));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zadd(key, scoredValues));
        }
    }

    @Override
    public Double zaddincr(RedisConnectContext redisConnectContext, String key, double score, String member) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zaddincr(key, score, member));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zaddincr(key, score, member));
        }
    }

    @Override
    public Long zcard(RedisConnectContext redisConnectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("ZCARD ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zcard(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zcard(key));
        }
    }

    @Override
    public Long zrem(RedisConnectContext redisConnectContext, String key, String... members) {
        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("ZREM ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zrem(key, members));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zrem(key, members));
        }
    }

    @Override
    public List<ScoredValue<String>> zrange(RedisConnectContext redisConnectContext, String key, long start, long stop) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zrangeWithScores(key, start, stop));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zrangeWithScores(key, start, stop));
        }
    }

    @Override
    public Long zcount(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zcount(key, range));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zcount(key, range));
        }
    }

    @Override
    public List<String> zrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zrangebyscore(key, range, limit));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zrangebyscore(key, range, limit));
        }
    }

    @Override
    public List<ScoredValue<String>> zrangebyscoreWithScores(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zrangebyscoreWithScores(key, range));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zrangebyscoreWithScores(key, range));
        }
    }

    @Override
    public List<String> zrevrangebyscore(RedisConnectContext redisConnectContext, String key, Range<? extends Number> range, Limit limit) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zrevrangebyscore(key, range, limit));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zrevrangebyscore(key, range, limit));
        }
    }

    @Override
    public Long zrevrank(RedisConnectContext redisConnectContext, String key, String member) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zrevrank(key, member));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zrevrank(key, member));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zscan(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zscan(key));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zscan(key, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zscan(key, scanArgs));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public ScoredValueScanCursor<String> zscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zscan(key, scanCursor));
        }
    }

    @Override
    public Double zscore(RedisConnectContext redisConnectContext, String key, String member) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zscore(key, member));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zscore(key, member));
        }
    }

    @Override
    public ScoredValue<String> zpopmin(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zpopmin(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zpopmin(key));
        }
    }

    @Override
    public List<ScoredValue<String>> zpopmin(RedisConnectContext redisConnectContext, String key, long count) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.zpopmin(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.zpopmin(key, count));
        }
    }
}
