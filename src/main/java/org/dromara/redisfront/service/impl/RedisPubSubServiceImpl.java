package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
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
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            var connection = RedisConnectionPoolManager.getClusterConnectPubSub(redisConnectContext);
            var value = connection.sync().publish(channel, message);
            RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
            return value;
        }
        var connection = RedisConnectionPoolManager.getConnectPubSub(redisConnectContext);
        var value = connection.sync().publish(channel, message);
        RedisConnectionPoolManager.closeConnection(redisConnectContext, connection);
        return value;
    }

}
