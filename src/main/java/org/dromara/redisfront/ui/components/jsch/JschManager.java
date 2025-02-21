package org.dromara.redisfront.ui.components.jsch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.lettuce.core.cluster.models.partitions.RedisClusterNode;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.JschUtils;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * JschManager
 * 32768 - 61000
 */
public class JschManager implements AutoCloseable {
    private static final Map<Integer, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentSkipListSet<Integer> PORT_SET = new ConcurrentSkipListSet<>();
    private static final int MIN_PORT = 32768;
    private static final int MAX_PORT = 65535;
    private static final String LOCAL_HOST = "127.0.0.1";
    public final static JschManager MANAGER = new JschManager();

    public void openSession(RedisConnectContext redisConnectContext) {
        redisConnectContext.setLocalHost(LOCAL_HOST);
        redisConnectContext.setLocalPort(getTempLocalPort());
        if (SESSION_MAP.get(redisConnectContext.getId()) == null || !SESSION_MAP.get(redisConnectContext.getId()).isConnected()) {
            this.createSession(redisConnectContext);
        } else {
            this.rebindSession(redisConnectContext);
        }
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            Map<Integer, Integer> clusterTempPort = new HashMap<>();
            for (RedisClusterNode partition : LettuceUtils.getRedisClusterPartitions(redisConnectContext)) {
                var remotePort = partition.getUri().getPort();
                int port = getTempLocalPort();
                clusterTempPort.put(remotePort, port);
            }
            redisConnectContext.setClusterLocalPort(clusterTempPort);
            this.rebindSession(redisConnectContext);
        }
    }


    private void createSession(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.isNotNull(redisConnectContext.getSshInfo())) {
            try {
                SESSION_MAP.compute(redisConnectContext.getId(), (_, session) -> {
                    if (session != null && session.isConnected()) {

                        return session;
                    }
                    Session newSession = JschUtils.createSession(redisConnectContext);
                    try {
                        newSession.setTimeout(redisConnectContext.getSetting().getSshTimeout());
                        newSession.connect();
                        String remoteHost = getRemoteAddress(redisConnectContext);
                        JschUtil.bindPort(newSession, remoteHost, redisConnectContext.getPort(), redisConnectContext.getLocalPort());
                        return newSession;
                    } catch (JSchException e) {
                        throw new RedisFrontException("SSH 连接失败 - " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                throw new RedisFrontException(e.getMessage(), e, false);
            }
        }
    }

    public void closeSession(RedisConnectContext redisConnectContext) {
        Session session = SESSION_MAP.remove(redisConnectContext.getId());
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        removeTmpLocalPort(redisConnectContext);
    }

    private void rebindSession(RedisConnectContext redisConnectContext) {
        SESSION_MAP.compute(redisConnectContext.getId(), (_, session) -> {
            if (session != null && session.isConnected()) {
                if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
                    redisConnectContext.getClusterLocalPort().forEach(((remotePort, localPort) -> {
                        String remoteHost = getRemoteAddress(redisConnectContext);
                        JschUtil.bindPort(session, remoteHost, remotePort, localPort);
                    }));
                } else {
                    String remoteHost = getRemoteAddress(redisConnectContext);
                    JschUtil.bindPort(session, remoteHost, redisConnectContext.getPort(), redisConnectContext.getLocalPort());
                }
                return session;
            }
            return null;
        });
    }

    private int getTempLocalPort() {
        int port;
        do {
            port = RandomUtil.randomInt(MIN_PORT, MAX_PORT);
        } while (!PORT_SET.add(port));
        return port;
    }

    private void removeTmpLocalPort(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.equal(RedisMode.CLUSTER, redisConnectContext.getRedisMode())) {
            if (CollUtil.isNotEmpty(redisConnectContext.getClusterLocalPort())) {
                redisConnectContext.getClusterLocalPort().forEach((_, v) -> PORT_SET.remove(v));
            }
        } else {
            if (CollUtil.isNotEmpty(PORT_SET)) {
                PORT_SET.remove(redisConnectContext.getLocalPort());
            }
        }
    }

    private static String getRemoteAddress(RedisConnectContext redisConnectContext) {
        var remoteAddress = redisConnectContext.getHost();
        if (RedisFrontUtils.equal(remoteAddress, "127.0.0.1") || RedisFrontUtils.equal(remoteAddress.toLowerCase(), "localhost")) {
            remoteAddress = redisConnectContext.getSshInfo().getHost();
        }
        return remoteAddress;
    }

    @Override
    public void close() throws Exception {

    }
}
