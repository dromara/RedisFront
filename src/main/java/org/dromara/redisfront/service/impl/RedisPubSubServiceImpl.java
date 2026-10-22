package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.lettuce.LettuceUtils;
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
        org.dromara.redisfront.commons.lettuce.ReadOnlyCommands.requireAllowed(redisConnectContext, "PUBLISH");
        byte[] body = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return LettuceUtils.execByMode(redisConnectContext, commands -> commands.publish(channel, body),
                commands -> commands.publish(channel, body));
    }

}
