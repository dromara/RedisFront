package org.dromara.redisfront.ui.components.jsch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.utils.JschUtils;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JschManager
 */
@Slf4j
public class JschManager implements AutoCloseable {
    private static final Map<Integer, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentSkipListSet<Integer> PORT_SET = new ConcurrentSkipListSet<>();
    private static final int MIN_PORT = Integer.parseInt(System.getProperty("jsch.min.port", "32768"));
    private static final int MAX_PORT = Integer.parseInt(System.getProperty("jsch.max.port", "65535"));
    private static final String LOCAL_HOST = "127.0.0.1";
    public final static JschManager MANAGER = new JschManager();
    private final Lock portLock = new ReentrantLock();

    public void openSession(RedisConnectContext redisConnectContext) {
        redisConnectContext.setLocalHost(LOCAL_HOST);
        int tempLocalPort = getTempLocalPort();
        redisConnectContext.setLocalPort(tempLocalPort);
        if (SESSION_MAP.get(redisConnectContext.getId()) == null || !SESSION_MAP.get(redisConnectContext.getId()).isConnected()) {
            this.createSession(redisConnectContext);
        } else {
            this.rebindSession(redisConnectContext);
        }
    }

    public void openClusterSession(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            Map<Integer, Integer> clusterTempPort = new HashMap<>();
            for (Integer port : LettuceUtils.getRedisClusterPartitionPorts(redisConnectContext)) {
                int localPort = getTempLocalPort();
                clusterTempPort.put(port, localPort);
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
                        throw new RedisFrontException("SSH 连接失败 - " + e.getMessage(), e, false);
                    } finally {
                        if (!newSession.isConnected()) {
                            newSession.disconnect();
                        }
                    }
                });
            } catch (Exception e) {
                log.error("创建会话失败", e);
                throw new RedisFrontException(e.getMessage(), e, false);
            }
        }
    }

    public void closeSession(RedisConnectContext redisConnectContext) {
        RedisConnectionPoolManager.cleanupContextPool(redisConnectContext);
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
                    redisConnectContext.getClusterLocalPort().forEach((remotePort, localPort) -> {
                        String remoteHost = getRemoteAddress(redisConnectContext);
                        JschUtil.bindPort(session, remoteHost, remotePort, localPort);
                    });
                } else {
                    String remoteHost = getRemoteAddress(redisConnectContext);
                    Integer localPort = redisConnectContext.getLocalPort();
                    boolean boundPort = JschUtil.bindPort(session, remoteHost, redisConnectContext.getPort(), localPort);
                }
                return session;
            }
            return null;
        });
    }

    private int getTempLocalPort() {
        portLock.lock();
        try {
            for (int i = MIN_PORT; i <= MAX_PORT; i++) {
                if (PORT_SET.add(i)) {
                    return i;
                }
            }
            throw new RedisFrontException("无可用端口");
        } finally {
            portLock.unlock();
        }
    }

    private void removeTmpLocalPort(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.equal(RedisMode.CLUSTER, redisConnectContext.getRedisMode())) {
            if (CollUtil.isNotEmpty(redisConnectContext.getClusterLocalPort())) {
                redisConnectContext.getClusterLocalPort().forEach((_, v) -> PORT_SET.remove(v));
            }
        } else {
            PORT_SET.remove(redisConnectContext.getLocalPort());
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
        // 实现具体的关闭逻辑
    }
}
