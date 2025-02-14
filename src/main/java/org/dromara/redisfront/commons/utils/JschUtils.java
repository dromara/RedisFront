package org.dromara.redisfront.commons.utils;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.lettuce.core.cluster.RedisClusterClient;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSchUtil
 * 32768 - 61000
 *
 * @author Jin
 */
public class JschUtils {
    private static final Map<Integer, Session> sessionMap = new ConcurrentHashMap<>();

    public static Session createSession(RedisConnectContext redisConnectContext) {
        RedisConnectContext.SshInfo sshInfo = redisConnectContext.getSshInfo();
        if (RedisFrontUtils.isNotEmpty(sshInfo.getPrivateKeyPath()) && RedisFrontUtils.isNotEmpty(sshInfo.getPassword())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), sshInfo.getPassword().getBytes());
        } else if (RedisFrontUtils.isNotEmpty(sshInfo.getPrivateKeyPath())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), null);
        } else {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPassword());
        }
    }

    private static String getRemoteAddress(RedisConnectContext redisConnectContext) {
        var remoteAddress = redisConnectContext.getHost();
        if (RedisFrontUtils.equal(remoteAddress, "127.0.0.1") || RedisFrontUtils.equal(remoteAddress.toLowerCase(), "localhost")) {
            remoteAddress = redisConnectContext.getSshInfo().getHost();
        }
        return remoteAddress;
    }

    public static void closeSession(RedisConnectContext redisConnectContext) {
        Session session = sessionMap.remove(redisConnectContext.getId());
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public static void openSession(RedisConnectContext redisConnectContext, RedisClusterClient clusterClient) {
        if (RedisFrontUtils.isNotNull(redisConnectContext.getSshInfo())) {
            openSession(redisConnectContext);
        } else {
            clusterClient.getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(redisConnectContext.getHost()));
        }
    }

    public static void openSession(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.isNotNull(redisConnectContext.getSshInfo())) {
            try {
                sessionMap.compute(redisConnectContext.getId(), (_, session) -> {
                    if (session != null && session.isConnected()) {
                        return session;
                    }
                    Session newSession = createSession(redisConnectContext);
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
                throw new RedisFrontException("SSH 端口绑定失败，请重试!", e, false);
            }
        }
    }

}
