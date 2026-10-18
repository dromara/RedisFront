package org.dromara.redisfront.commons.jsch;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class JschManagerTest {

    private static RedisConnectContext clusterContext(Map<String, Integer> clusterLocalPort) {
        RedisConnectContext context = new RedisConnectContext();
        context.setRedisMode(RedisMode.CLUSTER);
        context.setClusterLocalPort(clusterLocalPort);
        return context;
    }

    /**
     * openClusterSession 先调 openSession 再生成端口表，因此 rebindSession 会在
     * clusterLocalPort 仍为 null 时被触发。此处必须退回种子节点分支而不是空指针。
     */
    @Test
    void isClusterBinding_should_not_npe_when_clusterLocalPort_is_null() {
        Assertions.assertFalse(JschManager.isClusterBinding(clusterContext(null)));
    }

    @Test
    void isClusterBinding_should_be_false_when_clusterLocalPort_is_empty() {
        Assertions.assertFalse(JschManager.isClusterBinding(clusterContext(Map.of())));
    }

    @Test
    void isClusterBinding_should_be_true_when_clusterLocalPort_has_entries() {
        Assertions.assertTrue(JschManager.isClusterBinding(clusterContext(Map.of("10.0.0.1:7001", 32768))));
    }

    @Test
    void isClusterBinding_should_be_false_for_non_cluster_mode() {
        RedisConnectContext standalone = new RedisConnectContext();
        standalone.setRedisMode(RedisMode.STANDALONE);
        Assertions.assertFalse(JschManager.isClusterBinding(standalone));

        RedisConnectContext sentinel = new RedisConnectContext();
        sentinel.setRedisMode(RedisMode.SENTINEL);
        Assertions.assertFalse(JschManager.isClusterBinding(sentinel));
    }

    /**
     * AddConnectDialog.openActionPerformed 的 catch 分支会在 testConnect 已经关闭过会话后
     * 再次 closeSession，releaseLocalPort 必须可重复执行（ConcurrentSkipListSet 不接受 null key）。
     */
    @Test
    void releaseLocalPort_should_be_idempotent_when_localPort_is_null() {
        RedisConnectContext context = new RedisConnectContext();

        Assertions.assertDoesNotThrow(() -> JschManager.MANAGER.releaseLocalPort(context));
        Assertions.assertDoesNotThrow(() -> JschManager.MANAGER.releaseLocalPort(context));
    }

    @Test
    void releaseLocalPort_should_clear_ports_so_next_open_allocates_a_new_one() {
        RedisConnectContext context = new RedisConnectContext();
        context.setLocalPort(32768);
        context.setClusterLocalPort(Map.of("10.0.0.1:7001", 32769));

        JschManager.MANAGER.releaseLocalPort(context);

        Assertions.assertNull(context.getLocalPort());
        Assertions.assertNull(context.getClusterLocalPort());
    }
}
