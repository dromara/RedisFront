package com.redisfront.commons.util;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.cluster.RedisClusterClient;
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
        if (Fn.isNotEmpty(connectInfo.sshConfig().getPrivateKeyPath()) && Fn.isNotEmpty(connectInfo.sshConfig().getPassword())) {
            return JschUtil.createSession(connectInfo.sshConfig().getHost(), connectInfo.sshConfig().getPort(), connectInfo.sshConfig().getUser(), connectInfo.sshConfig().getPrivateKeyPath(), connectInfo.sshConfig().getPassword().getBytes());
        } else if (Fn.isNotEmpty(connectInfo.sshConfig().getPrivateKeyPath())) {
            return JschUtil.createSession(connectInfo.sshConfig().getHost(), connectInfo.sshConfig().getPort(), connectInfo.sshConfig().getUser(), connectInfo.sshConfig().getPrivateKeyPath(), null);
        } else {
            return JschUtil.createSession(connectInfo.sshConfig().getHost(), connectInfo.sshConfig().getPort(), connectInfo.sshConfig().getUser(), connectInfo.sshConfig().getPassword());
        }
    }

    private static String getRemoteAddress(ConnectInfo connectInfo) {
        var remoteAddress = connectInfo.host();
        if (Fn.equal(remoteAddress, "127.0.0.1") || Fn.equal(remoteAddress.toLowerCase(), "localhost")) {
            remoteAddress = connectInfo.sshConfig().getHost();
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
        if (Fn.isNotNull(connectInfo.sshConfig())) {
            try {
                Session session = sessionThreadLocal.get();
                if (Fn.isNotNull(session)) {
                    session.disconnect();
                }
                session = createSession(connectInfo);
                String remoteAddress = getRemoteAddress(connectInfo);
                session.setTimeout(PrefUtils.getState().getInt(Const.KEY_SSH_TIMEOUT, 1000));
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
            clusterClient.getPartitions().forEach(redisClusterNode -> redisClusterNode.getUri().setHost(connectInfo.host()));
        }
    }

    public synchronized static void openSession(ConnectInfo connectInfo) {
        if (Fn.isNotNull(connectInfo.sshConfig())) {
            try {
                Session session = sessionThreadLocal.get();
                if (Fn.isNotNull(session)) {
                    session.disconnect();
                }
                session = createSession(connectInfo);
                var remoteHost = getRemoteAddress(connectInfo);
                session.setTimeout(PrefUtils.getState().getInt(Const.KEY_SSH_TIMEOUT, 1000));
                session.connect();
                JschUtil.bindPort(session, remoteHost, connectInfo.port(), connectInfo.getLocalPort());
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
