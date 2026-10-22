package org.dromara.redisfront.commons.lettuce;

import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.CommandType;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ReadOnlyCommandsTest {
    @Test void writesScriptsAndDispatchAreBlockedBeforeTheyReachTheConnection() {
        var context = new RedisConnectContext();context.setSetting(new RedisConnectContext.SettingInfo(1000, ":", 3000, 3000));
        context.getSetting().setReadOnly(true);var calls = new AtomicInteger();
        @SuppressWarnings("unchecked") var raw = (RedisCommands<String, String>) Proxy.newProxyInstance(RedisCommands.class.getClassLoader(),
                new Class<?>[]{RedisCommands.class}, (p, m, a) -> { calls.incrementAndGet();return null; });
        var guarded = ReadOnlyCommands.wrap(context, raw, RedisCommands.class);
        assertThrows(IllegalStateException.class, () -> guarded.set("key", "value"));
        assertThrows(IllegalStateException.class, () -> guarded.del("key"));
        assertThrows(IllegalStateException.class, () -> guarded.dispatch(CommandType.EVAL, new ArrayOutput<>(StringCodec.UTF8)));
        assertThrows(IllegalStateException.class, () -> guarded.dispatch(CommandType.CONFIG, new ArrayOutput<>(StringCodec.UTF8)));
        assertEquals(0, calls.get());guarded.get("key");assertEquals(1, calls.get());
        context.clone().getSetting().setReadOnly(false);guarded.set("key", "value");assertEquals(2, calls.get());
    }
    @Test void sentinelFallbackEndpointsAcceptIpv6AndRejectInvalidPorts() {
        assertEquals(2, ConnectionRecovery.parseEndpoints("127.0.0.1:26379,[::1]:26380;127.0.0.1:26379").size());
        assertEquals("::1", ConnectionRecovery.parseEndpoints("[::1]:26380").getFirst().host());
        for (String text : new String[]{"bad", "host:0", "host:65536", ":26379", "host:abc"}) {
            assertThrows(IllegalArgumentException.class, () -> ConnectionRecovery.parseEndpoints(text));
        }
    }
}
