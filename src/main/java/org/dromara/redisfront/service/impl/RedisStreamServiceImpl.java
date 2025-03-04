package org.dromara.redisfront.service.impl;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.*;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.service.RedisStreamService;

import java.util.List;
import java.util.Map;

/**
 * RedisStreamService
 *
 * @author Jin
 */
public class RedisStreamServiceImpl implements RedisStreamService {

    @Override
    public Long xdel(RedisConnectContext redisConnectContext, String key, String... messageIds) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xdel(key, messageIds));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xdel(key, messageIds));
        }
    }

    @Override
    public Long xack(RedisConnectContext redisConnectContext, String key, String group, String... messageIds) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xack(key, group, messageIds));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xack(key, group, messageIds));
        }
    }

    @Override
    public String xadd(RedisConnectContext redisConnectContext, String key, Map<String, String> body) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xadd(key, new XAddArgs(), body));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xadd(key, body));
        }
    }

    @Override
    public String xadd(RedisConnectContext redisConnectContext, String id, String key, Map<String, String> body) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
        }
    }

    @Override
    public List<StreamMessage<String, String>> xrange(RedisConnectContext redisConnectContext, String key, Range<String> range, Limit limit) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xrange(key, range, limit));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xrange(key, range, limit));
        }
    }

    @Override
    public String xadd(RedisConnectContext redisConnectContext, String key, XAddArgs args, Object... keysAndValues) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xadd(key, args, keysAndValues));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xadd(key, args, keysAndValues));
        }
    }

    @Override
    public String xgroupCreate(RedisConnectContext redisConnectContext, XReadArgs.StreamOffset<String> streamOffset, String group) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xgroupCreate(streamOffset, group));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xgroupCreate(streamOffset, group));
        }
    }

    @Override
    public String xgroupCreate(RedisConnectContext redisConnectContext, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xgroupCreate(streamOffset, group, args));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xgroupCreate(streamOffset, group, args));
        }
    }

    @Override
    public Long xlen(RedisConnectContext redisConnectContext, String key) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, commands -> commands.xlen(key));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.xlen(key));
        }
    }
}
