package org.dromara.redisfront.ui.components.monitor;

import java.util.Map;
import java.util.Objects;

/** Stateful counter deltas for one Redis process. Times are monotonic nanoseconds. */
public final class RedisMetricSampler {
    private Map<String, Object> previous;
    private long previousTime;

    public record Sample(long timestamp, double cpuPercent, double inputRate, double outputRate,
                         double usedMemory, double rssMemory, double fragmentationRatio,
                         long connectedClients, long commandsProcessed, double qps, double maxMemory) {
    }

    public synchronized Sample sample(Map<String, Object> info, long now, long timestamp) {
        double seconds = previous == null ? 0 : (now - previousTime) / 1_000_000_000.0;
        boolean sameProcess = previous != null && Objects.equals(info.get("run_id"), previous.get("run_id"));
        double input = rate(info, "total_net_input_bytes", seconds, sameProcess);
        double output = rate(info, "total_net_output_bytes", seconds, sameProcess);
        double qps = rate(info, "total_commands_processed", seconds, sameProcess);
        double cpu = rate(info, "used_cpu_user", seconds, sameProcess)
                + rate(info, "used_cpu_sys", seconds, sameProcess);
        previous = Map.copyOf(info);
        previousTime = now;
        return new Sample(timestamp, cpu * 100, input, output, number(info, "used_memory"),
                number(info, "used_memory_rss"), number(info, "mem_fragmentation_ratio"),
                (long) number(info, "connected_clients"), (long) number(info, "total_commands_processed"),
                qps, number(info, "maxmemory"));
    }

    private double rate(Map<String, Object> info, String key, double seconds, boolean sameProcess) {
        if (!sameProcess || seconds <= 0 || !info.containsKey(key) || !previous.containsKey(key)) return 0;
        return Math.max(0, number(info, key) - number(previous, key)) / seconds;
    }

    public static double number(Map<String, ?> info, String key) {
        try {
            return Double.parseDouble(Objects.toString(info.get(key), "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
