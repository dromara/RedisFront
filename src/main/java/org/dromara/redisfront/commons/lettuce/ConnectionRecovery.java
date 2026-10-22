package org.dromara.redisfront.commons.lettuce;

import org.dromara.redisfront.commons.enums.*;
import org.dromara.redisfront.commons.jsch.JschManager;
import org.dromara.redisfront.commons.pool.RedisConnectionPoolManager;
import org.dromara.redisfront.model.context.RedisConnectContext;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Refreshes routing before a command; failed commands are never replayed. */
public final class ConnectionRecovery {
    private static final Map<String, State> STATES = new ConcurrentHashMap<>();
    private static final long INTERVAL_NANOS = 3_000_000_000L;
    private static final class State {
        long checked;
        String host;
        Integer port;
        final Set<Endpoint> sentinels = new LinkedHashSet<>();
        final Map<Endpoint, io.lettuce.core.RedisClient> clients = new HashMap<>();
        Endpoint lastHealthy;
    }
    public record Endpoint(String host, int port) {
        public Endpoint { if (host == null || host.isBlank() || port < 1 || port > 65535) throw new IllegalArgumentException("Invalid endpoint"); }
    }
    private ConnectionRecovery() { }

    public static void prepare(RedisConnectContext context) {
        var state = STATES.computeIfAbsent(context.keyPrefix(), ignored -> new State());
        synchronized (state) {
            if (state.host != null && context.getRedisMode() == RedisMode.SENTINEL) {
                context.setHost(state.host);context.setPort(state.port);
            }
            if (context.getConnectTypeMode() == ConnectType.SSH && !JschManager.MANAGER.isConnected(context)) {
                JschManager.MANAGER.openSession(context);
                RedisConnectionPoolManager.cleanupContextPool(context);LettuceUtils.close(context);
                state.checked = 0;
            }
            if (context.getRedisMode() != RedisMode.SENTINEL || context.getSentinelHost() == null) return;
            long now = System.nanoTime();
            if (state.checked != 0 && now - state.checked < INTERVAL_NANOS) return;
            state.sentinels.add(new Endpoint(context.getSentinelHost(), context.getSentinelPort()));
            if (context.getSetting() != null) state.sentinels.addAll(parseEndpoints(context.getSetting().getSentinelEndpoints()));
            RuntimeException failure = null;
            var candidates = new ArrayList<>(state.sentinels);
            if (state.lastHealthy != null) { candidates.remove(state.lastHealthy);candidates.addFirst(state.lastHealthy); }
            for (Endpoint endpoint : candidates) {
                var probe = context.clone();
                probe.setHost(endpoint.host());probe.setPort(endpoint.port());probe.setDatabase(0);
                probe.setUsername(context.getSentinelUsername());probe.setPassword(context.getSentinelPassword());
                if (context.getConnectTypeMode() == ConnectType.SSH) {
                    probe.setLocalHost("127.0.0.1");
                    probe.setLocalPort(JschManager.MANAGER.openNodeForwarding(context, endpoint.host(), endpoint.port(), "recovery"));
                }
                var client = state.clients.computeIfAbsent(endpoint, ignored -> LettuceUtils.createUncachedClient(probe));
                try (var connection = client.connectSentinel(io.lettuce.core.codec.StringCodec.UTF8, LettuceUtils.createRedisURI(probe))) {
                    var commands = connection.sync();
                    String name = context.getSentinelMasterName();
                    if (name == null) name = commands.masters().getFirst().get("name");
                    var master = commands.master(name);
                    if (master.getOrDefault("flags", "").contains("s_down") || master.getOrDefault("flags", "").contains("o_down")) {
                        throw new IllegalStateException("Sentinel master is unavailable; waiting for failover");
                    }
                    String host = master.get("ip");int port = Integer.parseInt(master.get("port"));
                    try {
                        var peers = commands.dispatch(io.lettuce.core.protocol.CommandType.SENTINEL,
                                new io.lettuce.core.output.ArrayOutput<>(io.lettuce.core.codec.StringCodec.UTF8),
                                new io.lettuce.core.protocol.CommandArgs<>(io.lettuce.core.codec.StringCodec.UTF8).add("SENTINELS").add(name));
                        for (Object peer : peers) if (peer instanceof List<?> values) {
                            var data = new HashMap<String, String>();
                            for (int i = 0; i + 1 < values.size(); i += 2) data.put(String.valueOf(values.get(i)), String.valueOf(values.get(i + 1)));
                            if (data.containsKey("ip") && data.containsKey("port")) state.sentinels.add(new Endpoint(data.get("ip"), Integer.parseInt(data.get("port"))));
                        }
                    }
                    catch (RuntimeException ignored) { /* A restricted ACL may still allow MASTER lookup. */ }
                    boolean changed = !host.equals(context.getHost()) || port != context.getPort();
                    if (changed) {
                        RedisConnectionPoolManager.cleanupContextPool(context);LettuceUtils.close(context);
                        context.setHost(host);context.setPort(port);
                        JschManager.MANAGER.redirectSession(context);
                    }
                    context.setSentinelMasterName(name);
                    state.host = host;state.port = port;state.checked = now;state.lastHealthy = endpoint;
                    return;
                } catch (RuntimeException e) { failure = e; }
            }
            state.checked = 0;
            throw new IllegalStateException("No Sentinel endpoint could discover a healthy master", failure);
        }
    }
    public static List<Endpoint> parseEndpoints(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("[,;\\s]+" )).map(part -> {
            int separator = part.lastIndexOf(':');
            if (separator <= 0) throw new IllegalArgumentException("Expected Sentinel host:port");
            String host = part.substring(0, separator);
            if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length() - 1);
            return new Endpoint(host, Integer.parseInt(part.substring(separator + 1)));
        }).distinct().toList();
    }
    public static void invalidate(RedisConnectContext context) {
        var state = STATES.get(context.keyPrefix());
        if (state != null) synchronized (state) { state.checked = 0; }
    }
    public static void close(RedisConnectContext context) {
        var state = STATES.remove(context.keyPrefix());
        if (state != null) synchronized (state) {
            state.clients.values().forEach(client -> { try { client.shutdown(); } catch (RuntimeException ignored) { } });
            state.clients.clear();
        }
    }

    public static List<Endpoint> sentinels(RedisConnectContext context) {
        var state = STATES.get(context.keyPrefix());
        if (state == null) return List.of(new Endpoint(context.getSentinelHost(), context.getSentinelPort()));
        synchronized (state) {
            var endpoints = new ArrayList<>(state.sentinels);
            if (state.lastHealthy != null) { endpoints.remove(state.lastHealthy);endpoints.addFirst(state.lastHealthy); }
            return List.copyOf(endpoints);
        }
    }
}
