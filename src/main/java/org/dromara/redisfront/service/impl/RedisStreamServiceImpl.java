package org.dromara.redisfront.service.impl;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.*;
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
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xdel(key, messageIds),
                commands -> commands.xdel(key, messageIds));
    }

    @Override
    public Long xack(RedisConnectContext redisConnectContext, String key, String group, String... messageIds) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xack(key, group, messageIds),
                commands -> commands.xack(key, group, messageIds));
    }

    @Override
    public String xadd(RedisConnectContext redisConnectContext, String key, Map<String, byte[]> body) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xadd(key, body),
                commands -> commands.xadd(key, new XAddArgs(), body));
    }

    @Override
    public String xadd(RedisConnectContext redisConnectContext, String id, String key, Map<String, byte[]> body) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body),
                commands -> commands.xadd(key, XAddArgs.Builder.minId(id), body));
    }

    @Override
    public List<StreamMessage<String, byte[]>> xrange(RedisConnectContext redisConnectContext, String key, Range<String> range, Limit limit) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xrange(key, range, limit),
                commands -> commands.xrange(key, range, limit));
    }

    @Override
    public String xadd(RedisConnectContext redisConnectContext, String key, XAddArgs args, Object... keysAndValues) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xadd(key, args, keysAndValues),
                commands -> commands.xadd(key, args, keysAndValues));
    }

    @Override
    public String xgroupCreate(RedisConnectContext redisConnectContext, XReadArgs.StreamOffset<String> streamOffset, String group) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xgroupCreate(streamOffset, group),
                commands -> commands.xgroupCreate(streamOffset, group));
    }

    @Override
    public String xgroupCreate(RedisConnectContext redisConnectContext, XReadArgs.StreamOffset<String> streamOffset, String group, XGroupCreateArgs args) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xgroupCreate(streamOffset, group, args),
                commands -> commands.xgroupCreate(streamOffset, group, args));
    }

    @Override
    public Long xlen(RedisConnectContext redisConnectContext, String key) {
        return LettuceUtils.execByMode(redisConnectContext,
                commands -> commands.xlen(key),
                commands -> commands.xlen(key));
    }
}
