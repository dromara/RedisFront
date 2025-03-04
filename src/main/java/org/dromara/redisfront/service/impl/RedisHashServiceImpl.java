package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
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
    public String hget(RedisConnectContext redisConnectContext, String key, String field) {

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hget(key, field));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hget(key, field));
        }
    }

    @Override
    public Map<String, String> hgetall(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hgetall(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hgetall(key));
        }
    }

    @Override
    public List<String> hkeys(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hkeys(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hkeys(key));
        }
    }

    @Override
    public Long hlen(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hlen(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hlen(key));
        }
    }

    @Override
    public String hmset(RedisConnectContext redisConnectContext, String key, Map<String, String> map) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hmset(key, map));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hmset(key, map));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor, ScanArgs scanArgs) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hscan(key, scanCursor, scanArgs));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hscan(key, scanCursor, scanArgs));
        }
    }

    @Override
    public MapScanCursor<String, String> hscan(RedisConnectContext redisConnectContext, String key, ScanCursor scanCursor) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("HSCAN ".concat(key).concat(" ").concat(scanCursor.getCursor()));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hscan(key, scanCursor));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hscan(key, scanCursor));
        }
    }

    @Override
    public Boolean hset(RedisConnectContext redisConnectContext, String key, String field, String value) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("HSET ".concat(key).concat(" ").concat(field).concat(" ").concat(value));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hset(key, field, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hset(key, field, value));
        }
    }

    @Override
    public Long hset(RedisConnectContext redisConnectContext, String key, Map<String, String> map) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hset(key, map));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hset(key, map));
        }
    }

    @Override
    public Long hstrlen(RedisConnectContext redisConnectContext, String key, String field) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hstrlen(key, field));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hstrlen(key, field));
        }
    }

    @Override
    public List<String> hvals(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hvals(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hvals(key));
        }
    }

    @Override
    public Long hdel(RedisConnectContext redisConnectContext, String key, String... fields) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("HSET ".concat(key).concat(" ").concat(Arrays.toString(fields).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.hdel(key, fields));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.hdel(key, fields));
        }
    }

}
