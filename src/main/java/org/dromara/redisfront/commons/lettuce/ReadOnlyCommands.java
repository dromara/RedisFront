package org.dromara.redisfront.commons.lettuce;

import org.dromara.redisfront.model.context.RedisConnectContext;
import java.lang.reflect.*;
import java.util.*;

/** Fail-closed client-side protection; use a Redis ACL account for server-side isolation. */
public final class ReadOnlyCommands {
    private static final Set<String> READS = Set.of(("ping echo info time select dbsize type exists ttl pttl expiretime pexpiretime "
            + "get mget getrange strlen getbit bitcount bitpos hget hmget hgetall hlen hkeys hvals hexists hstrlen hscan "
            + "lrange llen lindex lpos scard sismember smismember smembers sscan sinter sunion sdiff "
            + "zrange zrangebyscore zrevrange zrevrangebyscore zcard zcount zlexcount zscore zmscore zrank zrevrank zscan "
            + "xrange xrevrange xlen xinfoStream xinfoGroups xinfoConsumers scan randomkey dump memoryUsage "
            + "slowlogGet slowlogLen clusterInfo clusterNodes clusterSlots clusterShards clusterMyId "
            + "command commandInfo commandCount configGet clientGetname clientList clientInfo isOpen masters master replicas sentinels getMasterAddrByName").toLowerCase(Locale.ROOT).split(" "));

    private ReadOnlyCommands() { }
    public static boolean isReadOnly(RedisConnectContext context) {
        return context.getSetting() != null && context.getSetting().isReadOnly();
    }
    public static boolean permits(String command) { return READS.contains(command.toLowerCase(Locale.ROOT)); }
    public static void requireAllowed(RedisConnectContext context, String command) {
        if (isReadOnly(context) && !permits(command)) {
            throw new IllegalStateException(ResourceBundle.getBundle("org.dromara.redisfront.RedisFront")
                    .getString("Safety.readOnlyBlocked") + " " + command.toUpperCase(Locale.ROOT));
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T wrap(RedisConnectContext context, T commands, Class<?> api) {
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, (proxy, method, args) -> {
            if (method.getDeclaringClass() != Object.class) {
                String command = method.getName();
                if (command.equals("dispatch")) command = args[0].toString();
                requireAllowed(context, command);
            }
            try { return method.invoke(commands, args); }
            catch (InvocationTargetException e) { throw e.getCause(); }
        });
    }
}
