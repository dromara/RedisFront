package org.dromara.redisfront.service.impl;
import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.model.context.ConnectContext;
import io.lettuce.core.*;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.commons.utils.LettuceUtils;
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
    public Long xdel(ConnectContext connectContext, String key, String... messageIds) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xdel(key, messageIds));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xdel(key, messageIds));
        }
    }

    @Override
    public Long xack(ConnectContext connectContext, String key, String group, String... messageIds) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xack(key, group, messageIds));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xack(key, group, messageIds));
        }
    }

    @Override
    public String xadd(ConnectContext connectContext, String key, Map<String, String> body) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xadd(key, new XAddArgs(), body));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xadd(key, body));
        }
    }

    @Override
    public String xadd(ConnectContext connectContext, String id, String key, Map<String, String> body) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
        }
    }

    @Override
    public List<StreamMessage<String, String>> xrange(ConnectContext connectContext, String key, Range<String> range, Limit limit) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xrange(key, range, limit));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xrange(key, range, limit));
        }
    }

    @Override
    public String xadd(ConnectContext connectContext, String key, XAddArgs args, Object... keysAndValues) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xadd(key, args, keysAndValues));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xadd(key, args, keysAndValues));
        }
    }

    @Override
    public String xgroupCreate(ConnectContext connectContext, XReadArgs.StreamOffset<String> streamOffset, String group) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xgroupCreate(streamOffset, group));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xgroupCreate(streamOffset, group));
        }
    }

    @Override
    public String xgroupCreate(ConnectContext connectContext, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xgroupCreate(streamOffset, group, args));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xgroupCreate(streamOffset, group, args));
        }
    }

    @Override
    public Long xlen(ConnectContext connectContext, String key) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.xlen(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.xlen(key));
        }
    }
}
