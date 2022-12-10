package com.redisfront.service.impl;

import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisPubSubService;
import io.lettuce.core.pubsub.RedisPubSubListener;

/**
 * PubSubServiceImpl
 *
 * @author Jin
 */
public class RedisPubSubServiceImpl implements RedisPubSubService {

    @Override
    public Long publish(ConnectInfo connectInfo, String channel, String message) {
        if (Fn.equal(connectInfo.redisModeEnum(), Enum.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectInfo, clusterCommands -> clusterCommands.publish(channel, message));
        } else {
            return LettuceUtils.exec(connectInfo, commands -> commands.publish(channel, message));
        }
    }
}
