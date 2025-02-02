package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.service.RedisListService;

import java.util.Arrays;
import java.util.List;

/**
 * RedisListServiceImpl
 *
 * @author Jin
 */
public class RedisListServiceImpl implements RedisListService {
    @Override
    public List<String> lrange(RedisConnectContext redisConnectContext, String key, long start, long stop) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("LRANGE ".concat(key).concat(" ").concat(String.valueOf(start)).concat(" ").concat(String.valueOf(stop)));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lrange(key, start, stop));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lrange(key, start, stop));
        }
    }

    @Override
    public Long lrem(RedisConnectContext redisConnectContext, String key, long count, String value) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("LREM ".concat(key).concat(" ").concat(String.valueOf(count)).concat(" ").concat(String.valueOf(value)));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lrem(key, count, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lrem(key, count, value));
        }
    }

    @Override
    public Long llen(RedisConnectContext redisConnectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("LLEN ".concat(key).concat(" "));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.llen(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.llen(key));
        }
    }

    @Override
    public String lpop(RedisConnectContext redisConnectContext, String key) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lpop(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lpop(key));
        }
    }

    @Override
    public List<String> lpop(RedisConnectContext redisConnectContext, String key, long count) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lpop(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lpop(key, count));
        }
    }

    @Override
    public Long lpush(RedisConnectContext redisConnectContext, String key, String... values) {

        var logInfo = RedisBasicService.buildLogInfo(redisConnectContext).setInfo("LPUSH ".concat(key).concat(" ").concat(Arrays.toString(values).replace("[", "").replace("]", "").replace(","," ")));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lpush(key, values));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lpush(key, values));
        }
    }

    @Override
    public String lset(RedisConnectContext redisConnectContext, String key, long index, String value) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.lset(key, index, value));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.lset(key, index, value));
        }
    }

    @Override
    public String rpop(RedisConnectContext redisConnectContext, String key) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.rpop(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.rpop(key));
        }
    }

    @Override
    public List<String> rpop(RedisConnectContext redisConnectContext, String key, long count) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.rpop(key, count));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.rpop(key, count));
        }
    }

    @Override
    public Long rpush(RedisConnectContext redisConnectContext, String key, String... values) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.rpush(key, values));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.rpush(key, values));
        }
    }
}
