package org.dromara.redisfront.commons.utils;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.lettuce.core.cluster.RedisClusterClient;
import org.dromara.redisfront.commons.Fn;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.model.context.ConnectContext;

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

    public static Session createSession(ConnectContext connectContext) {
        ConnectContext.SshInfo sshInfo = connectContext.getSshInfo();
        if (Fn.isNotEmpty(sshInfo.getPrivateKeyPath()) && Fn.isNotEmpty(sshInfo.getPassword())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), sshInfo.getPassword().getBytes());
        } else if (Fn.isNotEmpty(sshInfo.getPrivateKeyPath())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), null);
        } else {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPassword());
        }
    }

    private static String getRemoteAddress(ConnectContext connectContext) {
        var remoteAddress = connectContext.getHost();
        if (Fn.equal(remoteAddress, "127.0.0.1") || Fn.equal(remoteAddress.toLowerCase(), "localhost")) {
            remoteAddress = connectContext.getSshInfo().getHost();
        }
        return remoteAddress;
    }

    public static void closeSession(ConnectContext connectContext) {
        Session session = sessionMap.remove(connectContext.getId());
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public static void openSession(ConnectContext connectContext, RedisClusterClient clusterClient) {
        if (Fn.isNotNull(connectContext.getSshInfo())) {
            openSession(connectContext);
        } else {
            clusterClient.getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(connectContext.getHost()));
        }
    }

    public static void openSession(ConnectContext connectContext) {
        if (Fn.isNotNull(connectContext.getSshInfo())) {
            try {
                sessionMap.compute(connectContext.getId(), (_, session) -> {
                    if (session != null && session.isConnected()) {
                        return session;
                    }
                    Session newSession = createSession(connectContext);
                    try {
                        newSession.setTimeout(1000);
                        newSession.connect();
                        String remoteHost = getRemoteAddress(connectContext);
                        JschUtil.bindPort(newSession, remoteHost, connectContext.getPort(), connectContext.getLocalPort());
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
