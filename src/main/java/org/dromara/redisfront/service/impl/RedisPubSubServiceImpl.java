package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisPubSubService;

/**
 * PubSubServiceImpl
 *
 * @author Jin
 */
public class RedisPubSubServiceImpl implements RedisPubSubService {

    @Override
    public Long publish(RedisConnectContext redisConnectContext, String channel, String message) {
        if (Fn.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(redisConnectContext, clusterCommands -> clusterCommands.publish(channel, message));
        } else {
            return LettuceUtils.exec(redisConnectContext, commands -> commands.publish(channel, message));
        }
    }

}
