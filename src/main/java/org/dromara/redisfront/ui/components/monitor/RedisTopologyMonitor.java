package org.dromara.redisfront.ui.components.monitor;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.jsch.JschManager;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.lettuce.RedisTopologyParser;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;

import java.util.*;

/** Read-only topology discovery and selected-node INFO on one background worker. */
public final class RedisTopologyMonitor implements AutoCloseable {
    public record Node(String id, String host, int port, String role, String state, String upstream, String slots) {
        public String address() { return RedisConnectContext.clusterAddress(host, port); }
        @Override public String toString() { return address(); }
    }
    public record Snapshot(List<Node> nodes, Node selected, Map<String, String> status,
                           Map<String, Object> info, String error, long sampledAt) { }

    private final RedisConnectContext base;
    private final String forwardingOwner = UUID.randomUUID().toString();
    private final Map<String, StatefulRedisConnection<String, String>> connections = new HashMap<>();
    private List<Node> knownNodes = List.of();
    private RedisClient client;
    private StatefulRedisSentinelConnection<String, String> sentinel;
    private boolean closed;

    public RedisTopologyMonitor(RedisConnectContext context) { this.base = context; }

    private RedisURI uri(String host, int port, boolean sentinelEndpoint) {
        if (closed) throw new IllegalStateException("Monitor is closed");
        var target = base.clone();
        target.setHost(host);
        target.setPort(port);
        if (sentinelEndpoint) {
            target.setUsername(base.getSentinelUsername());
            target.setPassword(base.getSentinelPassword());
            target.setDatabase(0);
        }
        if (base.getConnectTypeMode() == ConnectType.SSH) {
            target.setLocalHost("127.0.0.1");
            target.setLocalPort(JschManager.MANAGER.openNodeForwarding(base, host, port, forwardingOwner));
        }
        RedisURI uri = LettuceUtils.createRedisURI(target);
        if (client == null) client = LettuceUtils.createUncachedClient(target);
        return uri;
    }

    private StatefulRedisConnection<String, String> connection(Node node) {
        String address = node.address();
        var existing = connections.get(address);
        if (existing != null && existing.isOpen()) return existing;
        var connection = clientFor(node);
        connections.put(address, connection);
        return connection;
    }

    private StatefulRedisConnection<String, String> clientFor(Node node) {
        var uri = uri(node.host(), node.port(), false);
        return client.connect(StringCodec.UTF8, uri);
    }

    private Node seed() { return new Node("-", base.getHost(), base.getPort(), "-", "-", "-", "-"); }

    public Snapshot collect(String selectedAddress) {
        if (closed) throw new IllegalStateException("Monitor is closed");
        org.dromara.redisfront.commons.lettuce.ConnectionRecovery.prepare(base);
        var status = new LinkedHashMap<String, String>();
        List<Node> nodes;
        if (base.getRedisMode() == RedisMode.CLUSTER) {
            var commands = clusterSeed().sync();
            nodes = clusterNodes(commands.clusterNodes());
            RedisBasicService.service.strToMap(commands.clusterInfo()).forEach((k, v) -> status.put(k, String.valueOf(v)));
        } else if (base.getRedisMode() == RedisMode.SENTINEL && base.getSentinelHost() != null) {
            nodes = sentinelNodes(status);
        } else {
            var info = RedisBasicService.service.strToMap(connection(seed()).sync().info("replication"));
            nodes = replicationNodes(base.getHost(), base.getPort(), info);
            if (base.getRedisMode() == RedisMode.SENTINEL) status.put("sentinel_unavailable", "true");
        }
        if (nodes.isEmpty()) throw new IllegalStateException("Topology has no reachable node addresses");
        knownNodes = List.copyOf(nodes);
        Node selected = nodes.stream().filter(n -> n.address().equals(selectedAddress)).findFirst()
                .orElseGet(() -> nodes.stream().filter(n -> base.getRedisMode() == RedisMode.SENTINEL
                        ? n.role().equals("master") : n.address().equals(seed().address())).findFirst().orElse(nodes.getFirst()));
        try {
            var info = RedisBasicService.service.strToMap(connection(selected).sync().info());
            return new Snapshot(knownNodes, selected, Map.copyOf(status), info, "", System.currentTimeMillis());
        } catch (Exception e) {
            return new Snapshot(knownNodes, selected, Map.copyOf(status), Map.of(),
                    ExceptionMessageUtils.bestMessage(e), System.currentTimeMillis());
        }
    }

    private StatefulRedisConnection<String, String> clusterSeed() {
        var candidates = new LinkedHashMap<String, Node>();
        knownNodes.stream().filter(n -> n.state().equals("connected"))
                .forEach(n -> candidates.put(n.address(), n));
        candidates.putIfAbsent(seed().address(), seed());
        RuntimeException error = null;
        for (Node node : candidates.values()) {
            try {
                var c = connection(node);
                c.sync().ping();
                return c;
            } catch (RuntimeException e) { error = e; }
        }
        throw Objects.requireNonNull(error);
    }

    private List<Node> sentinelNodes(Map<String, String> status) {
        RuntimeException failure = null;
        for (var endpoint : org.dromara.redisfront.commons.lettuce.ConnectionRecovery.sentinels(base)) {
            try {
                if (sentinel != null) sentinel.close();
                var uri = uri(endpoint.host(), endpoint.port(), true);
                sentinel = client.connectSentinel(StringCodec.UTF8, uri);
                sentinel.sync().ping();failure = null;break;
            } catch (RuntimeException error) { failure = error; }
        }
        if (failure != null) throw failure;
        var commands = sentinel.sync();
        String name = base.getSentinelMasterName();
        if (name == null) name = commands.masters().getFirst().get("name");
        var master = commands.master(name);
        status.put("sentinel_master", name);
        status.put("sentinel_quorum", master.getOrDefault("quorum", "-"));
        status.put("sentinel_known", String.valueOf(1 + Long.parseLong(master.getOrDefault("num-other-sentinels", "0"))));
        status.put("sentinel_flags", master.getOrDefault("flags", "-"));
        try {
            String quorum = commands.dispatch(CommandType.SENTINEL, new StatusOutput<>(StringCodec.UTF8),
                    new CommandArgs<>(StringCodec.UTF8).add("CKQUORUM").add(name));
            status.put("sentinel_ckquorum", quorum);
        } catch (RuntimeException e) { status.put("sentinel_ckquorum", ExceptionMessageUtils.bestMessage(e)); }
        String masterAddress = master.get("ip") + ":" + master.get("port");
        var nodes = new ArrayList<Node>();
        nodes.add(sentinelNode(master, "master", "-"));
        commands.replicas(name).forEach(replica -> nodes.add(sentinelNode(replica, "slave", masterAddress)));
        return nodes;
    }

    private static Node sentinelNode(Map<String, String> data, String role, String upstream) {
        return new Node(data.getOrDefault("runid", "-"), data.get("ip"), Integer.parseInt(data.get("port")),
                role, data.getOrDefault("flags", "-"), upstream, "-");
    }

    public static List<Node> clusterNodes(String text) {
        var raw = RedisTopologyParser.parseClusterNodes(text);
        var addresses = new HashMap<String, String>();
        raw.forEach(n -> addresses.put(n.id(), RedisConnectContext.clusterAddress(n.host(), n.port())));
        return raw.stream().map(n -> new Node(n.id(), n.host(), n.port(),
                Arrays.asList(n.flags().split(",")).contains("master") ? "master" : "slave",
                n.flags().contains("fail") ? n.flags() : n.state(), addresses.getOrDefault(n.master(), "-"), n.slot()))
                .sorted(Comparator.comparing(Node::address)).toList();
    }

    public static List<Node> replicationNodes(String host, int port, Map<String, Object> info) {
        var nodes = new ArrayList<Node>();
        String role = String.valueOf(info.getOrDefault("role", "-"));
        String upstream = info.containsKey("master_host") ? info.get("master_host") + ":" + info.get("master_port") : "-";
        nodes.add(new Node("-", host, port, role, String.valueOf(info.getOrDefault("master_link_status", "online")), upstream, "-"));
        info.forEach((key, value) -> {
            if (!key.matches("slave\\d+")) return;
            var parts = new HashMap<String, String>();
            for (String part : value.toString().split(",")) {
                String[] pair = part.split("=", 2);
                if (pair.length == 2) parts.put(pair[0], pair[1]);
            }
            if (parts.containsKey("ip") && parts.containsKey("port")) nodes.add(new Node("-", parts.get("ip"),
                    Integer.parseInt(parts.get("port")), "slave", parts.getOrDefault("state", "-"), host + ":" + port, "-"));
        });
        return nodes;
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        try {
            connections.values().forEach(StatefulRedisConnection::close);
            connections.clear();
            if (sentinel != null) sentinel.close();
        } finally {
            try {
                if (client != null) client.shutdown();
            } finally {
                if (base.getConnectTypeMode() == ConnectType.SSH) JschManager.MANAGER.closeNodeForwardings(base, forwardingOwner);
            }
        }
    }
}
