package org.dromara.redisfront.commons.jsch;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.lettuce.core.cluster.models.partitions.Partitions;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.net.Socket;

/**
 * JschManager
 */
@Slf4j
public class JschManager {
    private static final Map<Integer, Session> SESSION_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentSkipListSet<Integer> PORT_SET = new ConcurrentSkipListSet<>();
    private static final int MIN_PORT = Integer.parseInt(System.getProperty("jsch.min.port", "32768"));
    private static final int MAX_PORT = Integer.parseInt(System.getProperty("jsch.max.port", "65535"));
    private static final String LOCAL_HOST = "127.0.0.1";
    public final static JschManager MANAGER = new JschManager();
    private final Lock portLock = new ReentrantLock();

    /**
     * 确保 SSH 会话就绪，并把本地端口指向当前的 host:port。
     * <p>
     * 幂等：本地端口只在首次分配，重复调用复用同一端口，不再向 PORT_SET 追加占位，
     * 因此不会累积孤儿转发（旧的实现每调一次就换一个新端口，旧端口既不解绑也不归还）。
     */
    public void openSession(RedisConnectContext redisConnectContext) {
        redisConnectContext.setLocalHost(LOCAL_HOST);
        if (RedisFrontUtils.isNull(redisConnectContext.getLocalPort())) {
            redisConnectContext.setLocalPort(getTempLocalPort());
        }
        Session session = SESSION_MAP.get(redisConnectContext.getId());
        if (RedisFrontUtils.isNull(session) || !session.isConnected()) {
            this.createSession(redisConnectContext);
        } else {
            this.rebindSession(redisConnectContext);
        }
    }

    /**
     * 会话已建立后目标地址发生变化时（哨兵模式定位到 master），把本地端口重新指向新的 host:port。
     * 非 SSH 连接没有会话，方法内部直接跳过。
     */
    public void redirectSession(RedisConnectContext redisConnectContext) {
        Session session = SESSION_MAP.get(redisConnectContext.getId());
        if (RedisFrontUtils.isNull(session) || !session.isConnected()) {
            return;
        }
        bindForward(session, redisConnectContext.getLocalPort(), redisConnectContext.getHost(),
                redisConnectContext.getPort());
    }

    /**
     * 打开集群会话：先建立种子节点隧道，再通过隧道发现拓扑，最后为每个分片分配本地端口并绑定。
     */
    public void openClusterSession(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.notEqual(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)) {
            return;
        }

        // 1. 确保会话与种子节点转发就绪（此刻还没有集群端口表，只绑定种子节点）
        this.openSession(redisConnectContext);

        // 2. 通过隧道发现拓扑
        var partitions = LettuceUtils.getRedisClusterPartitions(redisConnectContext);

        // 3. 端口表必须先落地再绑定，否则 rebindSession 会读到 null
        Map<String, Integer> previous = redisConnectContext.getClusterLocalPort();
        if (CollUtil.isNotEmpty(previous)) {
            previous.values().forEach(PORT_SET::remove);
        }
        redisConnectContext.setClusterLocalPort(buildClusterLocalPort(redisConnectContext, partitions));

        // 4. 应用全部节点转发，已正确绑定的端口会被 bindForward 自动跳过
        this.rebindSession(redisConnectContext);
    }

    /**
     * 生成 host:port -> 本地端口 的映射。种子节点复用 openSession 已分配的端口，其余分片各分配一个。
     */
    private @NotNull Map<String, Integer> buildClusterLocalPort(RedisConnectContext redisConnectContext,
            Partitions partitions) {
        Map<String, Integer> clusterLocalPort = new HashMap<>();

        clusterLocalPort.put(
                RedisConnectContext.clusterAddress(redisConnectContext.getHost(), redisConnectContext.getPort()),
                redisConnectContext.getLocalPort());

        for (var partition : partitions) {
            clusterLocalPort.computeIfAbsent(
                    RedisConnectContext.clusterAddress(partition.getUri().getHost(), partition.getUri().getPort()),
                    ignore -> getTempLocalPort());
        }
        return clusterLocalPort;
    }

    private void createSession(RedisConnectContext redisConnectContext) {
        if (RedisFrontUtils.isNotNull(redisConnectContext.getSshInfo())) {
            try {
                SESSION_MAP.compute(redisConnectContext.getId(), (ignore, session) -> {
                    if (session != null && session.isConnected()) {
                        return session;
                    }
                    Session newSession = JschUtils.createSession(redisConnectContext);
                    try {
                        newSession.setTimeout(redisConnectContext.getSetting().getSshTimeout());
                        newSession.connect();
                        JschUtil.bindPort(newSession, getRemoteAddress(redisConnectContext),
                                redisConnectContext.getPort(), redisConnectContext.getLocalPort());
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
        if (RedisFrontUtils.isNotNull(session) && session.isConnected()) {
            // JSch 在 disconnect 时会注销该会话上的全部本地端口转发
            session.disconnect();
        }
        releaseLocalPort(redisConnectContext);
    }

    private void rebindSession(RedisConnectContext redisConnectContext) {
        Session session = SESSION_MAP.get(redisConnectContext.getId());
        if (RedisFrontUtils.isNull(session) || !session.isConnected()) {
            return;
        }
        Map<String, Integer> clusterLocalPort = redisConnectContext.getClusterLocalPort();
        if (isClusterBinding(redisConnectContext)) {
            clusterLocalPort.forEach((address, localPort) -> {
                String[] parts = address.split(":");
                bindForward(session, localPort, parts[0], Integer.parseInt(parts[1]));
            });
            return;
        }
        bindForward(session, redisConnectContext.getLocalPort(), getRemoteAddress(redisConnectContext),
                redisConnectContext.getPort());
    }

    /**
     * 是否走集群端口表绑定。集群模式下端口表尚未生成时必须退回种子节点分支，
     * 否则 rebindSession 会拿到 null 的 clusterLocalPort 而抛空指针。
     */
    static boolean isClusterBinding(RedisConnectContext redisConnectContext) {
        return RedisFrontUtils.equal(redisConnectContext.getRedisMode(), RedisMode.CLUSTER)
                && CollUtil.isNotEmpty(redisConnectContext.getClusterLocalPort());
    }

    /**
     * 把 session 上的 localPort 转发到 remoteHost:remotePort。
     * 已指向同一目标时跳过；目标变化时先注销再注册，否则 JSch 会报 local port already registered。
     */
    private static void bindForward(Session session, Integer localPort, String remoteHost, Integer remotePort) {
        if (RedisFrontUtils.isNull(localPort) || RedisFrontUtils.isNull(remoteHost)
                || RedisFrontUtils.isNull(remotePort)) {
            return;
        }
        int lport = localPort;
        String target = remoteHost + ":" + remotePort;
        if (isForwarding(session, lport, target)) {
            return;
        }
        unbindForward(session, lport);
        JschUtil.bindPort(session, remoteHost, remotePort, lport);
    }

    private static boolean isForwarding(Session session, int localPort, String target) {
        try {
            for (String forwarding : session.getPortForwardingL()) {
                // getPortForwardingL 返回的条目形如 "lport:remoteHost:remotePort"
                if (forwarding.equals(localPort + ":" + target)) {
                    return true;
                }
            }
        } catch (JSchException e) {
            log.debug("读取端口转发列表失败", e);
        }
        return false;
    }

    private static void unbindForward(Session session, int localPort) {
        try {
            session.delPortForwardingL(localPort);
        } catch (JSchException e) {
            // 端口尚未注册时 JSch 会抛异常，属于预期情况
            log.debug("注销本地端口 {} 的转发失败（可能尚未注册）", localPort, e);
        }
    }

    /**
     * 归还本地端口占位，并清掉 ctx 上的端口信息，
     * 保证下次 openSession 重新分配而不是复用已随会话断开而释放的端口。
     */
    void releaseLocalPort(RedisConnectContext redisConnectContext) {
        Map<String, Integer> clusterLocalPort = redisConnectContext.getClusterLocalPort();
        if (CollUtil.isNotEmpty(clusterLocalPort)) {
            clusterLocalPort.values().forEach(PORT_SET::remove);
            redisConnectContext.setClusterLocalPort(null);
        }
        if (RedisFrontUtils.isNotNull(redisConnectContext.getLocalPort())) {
            PORT_SET.remove(redisConnectContext.getLocalPort());
        }
        redisConnectContext.setLocalPort(null);
    }

    private int getTempLocalPort() {
        portLock.lock();
        try {
            for (int i = MIN_PORT; i <= MAX_PORT; i++) {
                if (isPortAvailable(i) && PORT_SET.add(i)) {
                    return i;
                }
            }
            throw new RedisFrontException("无可用端口");
        } finally {
            portLock.unlock();
        }
    }

    private boolean isPortAvailable(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (Exception ignored) {
            return true;
        }
    }

    private static String getRemoteAddress(RedisConnectContext redisConnectContext) {
        return redisConnectContext.getHost();
    }

}
