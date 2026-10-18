package org.dromara.redisfront.commons.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 验证连接池按"连接身份"整组回收，而不是只回收当前 key 对应的那一个池。
 * <p>
 * 背景：key() 是连接参数的快照哈希，而 host/port/database/password 在连接生命周期内都可能变
 * （哨兵定位 master、切换数据库、终端 SELECT、重新输入密码）。每一次变化都会产生新 key，
 * 旧池如果没人按前缀清理，就会一直驻留直到进程退出。
 * <p>
 * NORMAL_POOLS 是进程内共享的静态 map，同 JVM 的其它用例也可能往里塞东西，
 * 所以这里一律按 key 断言，并把自己插入的条目清掉，避免互相干扰。
 */
public class RedisConnectionPoolManagerCleanupTest {

    @Test
    void cleanupContextPool_should_release_every_pool_created_under_earlier_keys_of_the_same_connection()
            throws Exception {
        RedisConnectContext context = new RedisConnectContext();
        Map<String, GenericObjectPool<Object>> pools = normalPools();
        List<String> injected = new ArrayList<>();

        try {
            // 哨兵分支：host/port 被改写为 master
            context.setHost("10.0.0.1");
            context.setPort(26379);
            injected.add(context.key());
            pools.put(injected.get(0), fakePool("sentinel"));

            context.setHost("10.0.0.9");
            context.setPort(6379);
            injected.add(context.key());
            pools.put(injected.get(1), fakePool("master"));

            // 切换数据库
            context.setDatabase(3);
            injected.add(context.key());
            pools.put(injected.get(2), fakePool("db3"));

            injected.forEach(key -> Assertions.assertTrue(pools.containsKey(key)));

            RedisConnectionPoolManager.cleanupContextPool(context);

            injected.forEach(key -> Assertions.assertFalse(pools.containsKey(key),
                    "同一连接下所有 key 的池都应被回收"));
            Assertions.assertTrue(pools.keySet().stream()
                    .noneMatch(key -> key.startsWith(context.keyPrefix())));
        } finally {
            injected.forEach(pools::remove);
        }
    }

    @Test
    void cleanupContextPool_should_not_touch_pools_of_other_connections() throws Exception {
        RedisConnectContext first = new RedisConnectContext();
        RedisConnectContext second = new RedisConnectContext();
        Map<String, GenericObjectPool<Object>> pools = normalPools();

        String firstKey = first.key();
        String secondKey = second.key();
        pools.put(firstKey, fakePool("first"));
        pools.put(secondKey, fakePool("second"));
        try {
            RedisConnectionPoolManager.cleanupContextPool(first);

            Assertions.assertFalse(pools.containsKey(firstKey));
            Assertions.assertTrue(pools.containsKey(secondKey));
        } finally {
            pools.remove(firstKey);
            pools.remove(secondKey);
        }
    }

    @Test
    void cleanupContextPool_should_release_pools_created_by_the_cloned_terminal_context() throws Exception {
        RedisConnectContext main = new RedisConnectContext();
        RedisConnectContext terminal = main.clone();
        // 终端里执行 SELECT 5，用的是 clone 出来的上下文
        terminal.setDatabase(5);

        Map<String, GenericObjectPool<Object>> pools = normalPools();
        String mainKey = main.key();
        String terminalKey = terminal.key();
        pools.put(mainKey, fakePool("main-db0"));
        pools.put(terminalKey, fakePool("terminal-db5"));
        try {
            RedisConnectionPoolManager.cleanupContextPool(main);

            Assertions.assertFalse(pools.containsKey(mainKey));
            Assertions.assertFalse(pools.containsKey(terminalKey), "终端 clone 出的池应跟着主标签页一起回收");
        } finally {
            pools.remove(mainKey);
            pools.remove(terminalKey);
        }
    }

    @Test
    void cleanPoolMap_by_exact_key_should_only_remove_that_pool() throws Exception {
        RedisConnectContext context = new RedisConnectContext();
        Map<String, GenericObjectPool<Object>> pools = normalPools();

        context.setDatabase(0);
        String db0 = context.key();
        context.setDatabase(1);
        String db1 = context.key();
        pools.put(db0, fakePool("db0"));
        pools.put(db1, fakePool("db1"));
        try {
            // 借还失败时只回收出错的那一类池
            RedisConnectionPoolManager.cleanPoolMap(pools, db0);

            Assertions.assertFalse(pools.containsKey(db0));
            Assertions.assertTrue(pools.containsKey(db1));
        } finally {
            pools.remove(db0);
            pools.remove(db1);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, GenericObjectPool<Object>> normalPools() throws Exception {
        Field field = RedisConnectionPoolManager.class.getDeclaredField("NORMAL_POOLS");
        field.setAccessible(true);
        return (Map<String, GenericObjectPool<Object>>) field.get(null);
    }

    private static GenericObjectPool<Object> fakePool(String name) {
        return new GenericObjectPool<>(new BasePooledObjectFactory<Object>() {
            @Override
            public Object create() {
                return new Object();
            }

            @Override
            public PooledObject<Object> wrap(Object obj) {
                return new DefaultPooledObject<>(obj);
            }

            @Override
            public String toString() {
                return name;
            }
        }, new GenericObjectPoolConfig<>());
    }
}
