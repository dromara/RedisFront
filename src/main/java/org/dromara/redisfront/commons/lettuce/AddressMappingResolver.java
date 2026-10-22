package org.dromara.redisfront.commons.lettuce;

import cn.hutool.core.collection.CollUtil;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.SocketAddressResolver;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.jsch.JschManager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class AddressMappingResolver extends SocketAddressResolver {
    private final RedisConnectContext redisConnectContext;

    public AddressMappingResolver(RedisConnectContext redisConnectContext) {
        this.redisConnectContext = redisConnectContext;
    }

    @Override
    public SocketAddress resolve(RedisURI redisURI) {
        if (redisConnectContext.getConnectTypeMode() == ConnectType.SSH) {
            String key = RedisConnectContext.clusterAddress(redisURI.getHost(), redisURI.getPort());
            Integer port = redisConnectContext.getClusterLocalPort() == null ? null : redisConnectContext.getClusterLocalPort().get(key);
            if (port != null) {
                return new InetSocketAddress("127.0.0.1", port);
            }
            // A newly advertised Cluster node also needs a tunnel; never fall back to a private direct socket.
            if (!redisURI.getHost().equals("127.0.0.1") && !redisURI.getHost().equals("localhost")) {
                int forwarded = JschManager.MANAGER.openNodeForwarding(redisConnectContext, redisURI.getHost(), redisURI.getPort(), "cluster-discovery");
                return new InetSocketAddress("127.0.0.1", forwarded);
            }
        }
        return super.resolve(redisURI);
    }
}
