package org.dromara.redisfront.service.impl;
import org.dromara.redisfront.commons.enums.Enums;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.model.context.ScanContext;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ValueScanCursor;
import org.dromara.redisfront.Fn;
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
    public Long sadd(ConnectContext connectContext, String key, String... members) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SADD ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sadd(key, members));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sadd(key, members));
        }
    }

    @Override
    public Long scard(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SCARD ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.scard(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.scard(key));
        }
    }

    @Override
    public Set<String> sdiff(ConnectContext connectContext, String... keys) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sdiff(keys));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sdiff(keys));
        }
    }

    @Override
    public Long sdiffstore(ConnectContext connectContext, String destination, String... keys) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sdiffstore(destination, keys));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sdiffstore(destination, keys));
        }
    }

    @Override
    public Set<String> sinter(ConnectContext connectContext, String... keys) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sinter(keys));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sinter(keys));
        }
    }

    @Override
    public Long sinterstore(ConnectContext connectContext, String destination, String... keys) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sinterstore(destination, keys));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sinterstore(destination, keys));
        }
    }

    @Override
    public Boolean sismember(ConnectContext connectContext, String key, String member) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sismember(key, member));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sismember(key, member));
        }
    }

    @Override
    public Set<String> smembers(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.smembers(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.smembers(key));
        }
    }

    @Override
    public List<Boolean> smismember(ConnectContext connectContext, String key, String... members) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.smismember(key, members));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.smismember(key, members));
        }
    }

    @Override
    public Boolean smove(ConnectContext connectContext, String source, String destination, String member) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.smove(source, destination, member));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.smove(source, destination, member));
        }
    }

    @Override
    public String spop(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.spop(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.spop(key));
        }
    }

    @Override
    public Set<String> spop(ConnectContext connectContext, String key, long count) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.spop(key, count));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.spop(key, count));
        }
    }

    @Override
    public String srandmember(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.srandmember(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.srandmember(key));
        }
    }

    @Override
    public List<String> srandmember(ConnectContext connectContext, String key, long count) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.srandmember(key, count));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.srandmember(key, count));
        }
    }

    @Override
    public Long srem(ConnectContext connectContext, String key, String... members) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SREM ".concat(key).concat(" ").concat(Arrays.toString(members).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.srem(key, members));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.srem(key, members));
        }
    }

    @Override
    public Set<String> sunion(ConnectContext connectContext, String... keys) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sunion(keys));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sunion(keys));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sscan(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sscan(key));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(ConnectContext connectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {


        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public ValueScanCursor<String> sscan(ConnectContext connectContext, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.sscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.sscan(key, scanCursor));
        }
    }
}
