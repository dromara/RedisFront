package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.commons.util.LettuceUtils;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.service.RedisPubSubService;

/**
 * PubSubServiceImpl
 *
 * @author Jin
 */
public class RedisPubSubServiceImpl implements RedisPubSubService {

    @Override
    public Long publish(ConnectContext connectContext, String channel, String message) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, clusterCommands -> clusterCommands.publish(channel, message));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.publish(channel, message));
        }
    }

}
