package org.dromara.redisfront.commons.util;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.dromara.redisfront.model.ConnectInfo;
import io.lettuce.core.cluster.RedisClusterClient;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.func.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * JSchUtil
 * 32768 - 61000
 *
 * @author Jin
 */
public class JschUtils {
    private static final Logger log = LoggerFactory.getLogger(JschUtils.class);
    static ThreadLocal<Session> sessionThreadLocal = new ThreadLocal<>();

    public static Session createSession(ConnectInfo connectInfo) {
        ConnectInfo.SshInfo sshInfo = connectInfo.getSshInfo();
        if (Fn.isNotEmpty(sshInfo.getPrivateKeyPath()) && Fn.isNotEmpty(sshInfo.getPassword())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), sshInfo.getPassword().getBytes());
        } else if (Fn.isNotEmpty(sshInfo.getPrivateKeyPath())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), null);
        } else {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPassword());
        }
    }

    private static String getRemoteAddress(ConnectInfo connectInfo) {
        var remoteAddress = connectInfo.getHost();
        if (Fn.equal(remoteAddress, "127.0.0.1") || Fn.equal(remoteAddress.toLowerCase(), "localhost")) {
            remoteAddress = connectInfo.getSshInfo().getHost();
        }
        return remoteAddress;
    }


    public static void closeSession() {
        var session = sessionThreadLocal.get();
        if (session != null) {
            session.disconnect();
            sessionThreadLocal.remove();
        }
    }

    public synchronized static void openSession(ConnectInfo connectInfo, RedisClusterClient clusterClient) {
        if (Fn.isNotNull(connectInfo.getSshInfo())) {
            try {
                Session session = sessionThreadLocal.get();
                if (Fn.isNotNull(session)) {
                    session.disconnect();
                }
                session = createSession(connectInfo);
                String remoteAddress = getRemoteAddress(connectInfo);
                session.setTimeout(1000);
                session.connect();
                for (Map.Entry<Integer, Integer> clusterTempPort : connectInfo.getClusterLocalPort().entrySet()) {
                    JschUtil.bindPort(session, remoteAddress, clusterTempPort.getKey(), clusterTempPort.getValue());
                }
                sessionThreadLocal.set(session);
            } catch (Exception e) {
                if (e instanceof JSchException jSchException) {
                    throw new RedisFrontException("SSH主机连接失败 - " + jSchException.getMessage());
                } else {
                    throw new RedisFrontException("SSH端口绑定失败，请重试!", e, false);
                }
            }
        } else {
            clusterClient.getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(connectInfo.getHost()));
        }
    }

    public synchronized static void openSession(ConnectInfo connectInfo) {
        if (Fn.isNotNull(connectInfo.getSshInfo())) {
            try {
                Session session = sessionThreadLocal.get();
                if (Fn.isNotNull(session)) {
                    session.disconnect();
                }
                session = createSession(connectInfo);
                var remoteHost = getRemoteAddress(connectInfo);
                session.setTimeout(1000);
                session.connect();
                JschUtil.bindPort(session, remoteHost, connectInfo.getPort(), connectInfo.getLocalPort());
                sessionThreadLocal.set(session);
            } catch (Exception e) {
                if (e instanceof JSchException jSchException) {
                    throw new RedisFrontException("SSH主机连接失败 - " + jSchException.getMessage());
                } else {
                    throw new RedisFrontException("SSH端口绑定失败，请重试!", e, false);
                }
            }
        }
    }

}
