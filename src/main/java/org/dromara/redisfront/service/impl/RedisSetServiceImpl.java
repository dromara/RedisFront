package org.dromara.redisfront.service.impl;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.service.RedisSetService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * RedisSetServiceImpl
 *
 * @author Jin
 */
public class RedisSetServiceImpl implements RedisSetService {
    @Override
    public Long sadd(RedisConnectContext redisConnectContext, String key, String... members) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("SADD ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sadd(key, members));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sadd(key, members));
        }
    }

    @Override
    public Long scard(RedisConnectContext redisConnectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("SCARD ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.scard(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.scard(key));
        }
    }

    @Override
    public Set<String> sdiff(RedisConnectContext redisConnectContext, String... keys) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sdiff(keys));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sdiff(keys));
        }
    }

    @Override
    public Long sdiffstore(RedisConnectContext redisConnectContext, String destination, String... keys) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sdiffstore(destination, keys));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sdiffstore(destination, keys));
        }
    }

    @Override
    public Set<String> sinter(RedisConnectContext redisConnectContext, String... keys) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sinter(keys));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sinter(keys));
        }
    }

    @Override
    public Long sinterstore(RedisConnectContext redisConnectContext, String destination, String... keys) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sinterstore(destination, keys));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sinterstore(destination, keys));
        }
    }

    @Override
    public Boolean sismember(RedisConnectContext redisConnectContext, String key, String member) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sismember(key, member));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sismember(key, member));
        }
    }

    @Override
    public Set<String> smembers(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.smembers(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.smembers(key));
        }
    }

    @Override
    public List<Boolean> smismember(RedisConnectContext redisConnectContext, String key, String... members) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.smismember(key, members));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.smismember(key, members));
        }
    }

    @Override
    public Boolean smove(RedisConnectContext redisConnectContext, String source, String destination, String member) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.smove(source, destination, member));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.smove(source, destination, member));
        }
    }

    @Override
    public String spop(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.spop(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.spop(key));
        }
    }

    @Override
    public Set<String> spop(RedisConnectContext redisConnectContext, String key, long count) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.spop(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.spop(key, count));
        }
    }

    @Override
    public String srandmember(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.srandmember(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.srandmember(key));
        }
    }

    @Override
    public List<String> srandmember(RedisConnectContext redisConnectContext, String key, long count) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.srandmember(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.srandmember(key, count));
        }
    }

    @Override
    public Long srem(RedisConnectContext redisConnectContext, String key, String... members) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("SREM ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.srem(key, members));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.srem(key, members));
        }
    }

    @Override
    public Set<String> sunion(RedisConnectContext redisConnectContext, String... keys) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sunion(keys));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sunion(keys));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sscan(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sscan(key));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("SSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.sscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.sscan(key, scanCursor));
        }
    }
}
