package org.dromara.redisfront.commons.lettuce;

import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.commons.pool.RedisConnectionFactory;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LettuceUtilsSafetyTest {

    @Test
    void getRedisClient_should_not_throw_when_enableSsl_true_and_sslInfo_null() {
        RedisConnectContext ctx = new RedisConnectContext();
        ctx.setId(10001);
        ctx.setSetting(new RedisConnectContext.SettingInfo(1000, ":", 3000, 3000));
        ctx.setEnableSsl(true);
        ctx.setSslInfo(null);

        Assertions.assertDoesNotThrow(() -> LettuceUtils.getRedisClient(ctx));

        LettuceUtils.close(ctx);
    }

    @Test
    void run_should_return_connection_to_pool_even_when_consumer_throws() throws Exception {
        RedisConnectContext ctx = new RedisConnectContext();
        ctx.setId(10002);

        AtomicBoolean returned = new AtomicBoolean(false);
        StatefulRedisConnection<String, byte[]> connection = (StatefulRedisConnection<String, byte[]>) Proxy.newProxyInstance(
                StatefulRedisConnection.class.getClassLoader(),
                new Class<?>[]{StatefulRedisConnection.class},
                (proxy, method, args) -> null
        );

        GenericObjectPool<StatefulRedisConnection<String, byte[]>> pool = new GenericObjectPool<>(new RedisConnectionFactory<>(() -> connection)) {
            @Override
            public StatefulRedisConnection<String, byte[]> borrowObject() {
                return connection;
            }

            @Override
            public void returnObject(StatefulRedisConnection<String, byte[]> obj) {
                if (obj == connection) {
                    returned.set(true);
                }
            }
        };

        Map<String, GenericObjectPool<StatefulRedisConnection<String, byte[]>>> pools = normalPools();
        String key = ctx.key();
        pools.put(key, pool);

        try {
            Assertions.assertThrows(RuntimeException.class, () -> LettuceUtils.run(ctx, ignore -> {
                throw new RuntimeException("boom");
            }));
            Assertions.assertTrue(returned.get());
        } finally {
            pools.remove(key);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, GenericObjectPool<StatefulRedisConnection<String, byte[]>>> normalPools() throws Exception {
        Field field = RedisConnectionPoolManager.class.getDeclaredField("NORMAL_POOLS");
        field.setAccessible(true);
        return (Map<String, GenericObjectPool<StatefulRedisConnection<String, byte[]>>>) field.get(null);
    }
}
