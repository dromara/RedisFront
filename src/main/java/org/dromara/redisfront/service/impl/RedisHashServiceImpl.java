package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.model.context.ScanContext;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.service.RedisHashService;
import org.dromara.redisfront.ui.dialog.LogsDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * RedisHashServiceImpl
 *
 * @author Jin
 */
public class RedisHashServiceImpl implements RedisHashService {

    @Override
    public String hget(ConnectContext connectContext, String key, String field) {

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hget(key, field));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hget(key, field));
        }
    }

    @Override
    public Map<String, String> hgetall(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hgetall(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hgetall(key));
        }
    }

    @Override
    public List<String> hkeys(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hkeys(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hkeys(key));
        }
    }

    @Override
    public Long hlen(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hlen(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hlen(key));
        }
    }

    @Override
    public String hmset(ConnectContext connectContext, String key, Map<String, String> map) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hmset(key, map));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hmset(key, map));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(ConnectContext connectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {

        ScanContext.MyScanArgs myScanArgs = (ScanContext.MyScanArgs) scanArgs;
        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("HSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()).concat(myScanArgs.getCommandStr()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(ConnectContext connectContext, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("HSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hscan(key, scanCursor));
        }
    }

    @Override
    public Boolean hset(ConnectContext connectContext, String key, String field, String value) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("HSET ".concat(key).concat(" ").concat(field).concat(" ").concat(value));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hset(key, field, value));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hset(key, field, value));
        }
    }

    @Override
    public Long hset(ConnectContext connectContext, String key, Map<String, String> map) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hset(key, map));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hset(key, map));
        }
    }

    @Override
    public Long hstrlen(ConnectContext connectContext, String key, String field) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hstrlen(key, field));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hstrlen(key, field));
        }
    }

    @Override
    public List<String> hvals(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hvals(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hvals(key));
        }
    }

    @Override
    public Long hdel(ConnectContext connectContext, String key, String... fields) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("HSET ".concat(key).concat(" ").concat(Arrays.toString(fields).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.hdel(key, fields));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.hdel(key, fields));
        }
    }

}
