package org.dromara.redisfront.commons.lettuce;

import cn.hutool.core.collection.CollUtil;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.SocketAddressResolver;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class AddressMappingResolver extends SocketAddressResolver {
    private final RedisConnectContext redisConnectContext;

    public AddressMappingResolver(RedisConnectContext redisConnectContext) {
        this.redisConnectContext = redisConnectContext;
    }


    @Override
    public SocketAddress resolve(RedisURI redisURI) {
        if (redisConnectContext.getSshInfo() != null && CollUtil.isNotEmpty(redisConnectContext.getClusterLocalPort())) {
            Integer port = redisConnectContext.getClusterLocalPort().get(redisURI.getPort());
            if (port != null) {
                return new InetSocketAddress("127.0.0.1", port);
            }
        }
        return super.resolve(redisURI);
    }
}