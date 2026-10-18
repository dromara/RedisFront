package org.dromara.redisfront.model.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class RedisConnectContextTest {

    /**
     * RedisInfoView 曾经用纯端口号（"7001"）查 clusterLocalPort，而映射的 key 是 "host:port"，
     * 于是永远查不到、紧跟的 .toString() 直接空指针，集群 + SSH 下连标签页都打不开。
     * 这里把 key 格式钉死，三处调用点（JschManager / AddressMappingResolver / RedisInfoView）共用。
     */
    @Test
    void clusterAddress_should_be_host_colon_port() {
        Assertions.assertEquals("10.0.0.1:7001", RedisConnectContext.clusterAddress("10.0.0.1", 7001));
        Assertions.assertNotEquals("7001", RedisConnectContext.clusterAddress("10.0.0.1", 7001));
    }

    @Test
    void clusterLocalPort_lookup_should_find_mapping_built_with_the_same_key_format() {
        Map<String, Integer> clusterLocalPort = new HashMap<>();
        clusterLocalPort.put(RedisConnectContext.clusterAddress("10.0.0.1", 7001), 32768);
        clusterLocalPort.put(RedisConnectContext.clusterAddress("10.0.0.2", 7002), 32769);

        RedisConnectContext context = new RedisConnectContext();
        context.setClusterLocalPort(clusterLocalPort);

        Assertions.assertEquals(32768,
                clusterLocalPort.get(RedisConnectContext.clusterAddress("10.0.0.1", 7001)));
        Assertions.assertNull(clusterLocalPort.get(RedisConnectContext.clusterAddress("10.0.0.3", 7003)));
    }

    /**
     * keyPrefix 是"这次打开连接"的身份，与可变的连接参数解耦。
     * host/port/database/password 在连接生命周期内都会被改写，改写后 key() 必须变但前缀不能变，
     * 否则旧的连接池无法按前缀整组回收。
     */
    @Test
    void key_should_change_while_keyPrefix_stays_stable_across_parameter_rewrites() {
        RedisConnectContext context = new RedisConnectContext();
        String prefix = context.keyPrefix();

        context.setHost("10.0.0.1");
        context.setPort(6379);
        String db0 = context.key();
        context.setDatabase(3);
        String db3 = context.key();
        context.setHost("10.0.0.9");
        context.setPort(6380);
        String master = context.key();

        Assertions.assertNotEquals(db0, db3);
        Assertions.assertNotEquals(db3, master);
        Assertions.assertEquals(prefix, context.keyPrefix());
        Assertions.assertTrue(db0.startsWith(prefix));
        Assertions.assertTrue(db3.startsWith(prefix));
        Assertions.assertTrue(master.startsWith(prefix));
    }

    @Test
    void distinct_connections_should_have_distinct_keyPrefixes() {
        Assertions.assertNotEquals(new RedisConnectContext().keyPrefix(),
                new RedisConnectContext().keyPrefix());
    }

    /**
     * RedisFrontTerminal 会 clone 连接上下文，clone 是浅拷贝，必须带上同一个 uid，
     * 这样终端里 SELECT 切库产生的连接池才能跟着主标签页一起回收。
     */
    @Test
    void clone_should_share_keyPrefix_but_not_key() {
        RedisConnectContext main = new RedisConnectContext();
        RedisConnectContext terminal = main.clone();

        Assertions.assertEquals(main.keyPrefix(), terminal.keyPrefix());

        terminal.setDatabase(5);
        Assertions.assertNotEquals(main.key(), terminal.key());
        Assertions.assertTrue(terminal.key().startsWith(main.keyPrefix()));
    }

    @Test
    void keyPrefix_should_survive_a_null_uid_left_by_deserialization() {
        RedisConnectContext context = new RedisConnectContext();
        context.setUid(null);

        Assertions.assertNotNull(context.keyPrefix());
        Assertions.assertTrue(context.key().startsWith(context.keyPrefix()));
    }
}
