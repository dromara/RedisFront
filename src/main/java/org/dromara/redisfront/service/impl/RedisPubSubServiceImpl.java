package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.commons.util.LettuceUtils;
import org.dromara.redisfront.model.ConnectInfo;
import org.dromara.redisfront.service.RedisPubSubService;

/**
 * PubSubServiceImpl
 *
 * @author Jin
 */
public class RedisPubSubServiceImpl implements RedisPubSubService {

    @Override
    public Long publish(ConnectInfo connectInfo, String channel, String message) {
        if (Fn.equal(connectInfo.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, clusterCommands -> clusterCommands.publish(channel, message));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.publish(channel, message));
        }
    }

}
