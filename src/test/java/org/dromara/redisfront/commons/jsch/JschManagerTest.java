package org.dromara.redisfront.commons.jsch;

import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class JschManagerTest {

    @SuppressWarnings("unchecked")
    @Test
    void closingOneMonitorLeavesOtherOwnersPortsReserved() throws Exception {
        var context = new RedisConnectContext();
        context.setId(-9071);
        var forwardsField = JschManager.class.getDeclaredField("NODE_FORWARDS");
        forwardsField.setAccessible(true);
        var forwards = (Map<String, Map<String, Integer>>) forwardsField.get(null);
        var portsField = JschManager.class.getDeclaredField("PORT_SET");
        portsField.setAccessible(true);
        var ports = (Set<Integer>) portsField.get(null);
        forwards.put("-9071:older", Map.of("host:7000", 32710));
        forwards.put("-9071:newer", Map.of("host:7000", 32711));
        ports.add(32710);ports.add(32711);
        try {
            JschManager.MANAGER.closeNodeForwardings(context, "older");
            Assertions.assertFalse(ports.contains(32710));
            Assertions.assertTrue(ports.contains(32711));
            Assertions.assertTrue(forwards.containsKey("-9071:newer"));
            JschManager.MANAGER.releaseLocalPort(context);
            Assertions.assertFalse(ports.contains(32711));
            Assertions.assertFalse(forwards.containsKey("-9071:newer"));
        } finally {
            JschManager.MANAGER.releaseLocalPort(context);
        }
    }

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
