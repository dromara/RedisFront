package org.dromara.redisfront.service;

import io.lettuce.core.*;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.CommandRedactor;
import org.dromara.redisfront.model.context.RedisConnectContext;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;

public final class RedisDiagnosticsService {
    public record CommandStat(String command, long calls, double totalMicros, double averageMicros) { }
    public record LargeKey(String key, String type, long bytes, long ttl) { }
    public record SlowEntry(String id, String timestamp, String micros, String command) { }
    public record BatchResult(int attempted, long removed, boolean cancelled) { }
    private RedisDiagnosticsService() { }

    public static List<CommandStat> commandStats(RedisConnectContext context) {
        String info = LettuceUtils.execByMode(context, c -> c.info("commandstats"), c -> c.info("commandstats"));
        return parseCommandStats(info);
    }
    public static List<CommandStat> parseCommandStats(String text) {
        var result = new ArrayList<CommandStat>();
        for (String line : text.split("\\R")) {
            if (!line.startsWith("cmdstat_") || !line.contains(":")) continue;
            int colon = line.indexOf(':');var values = new HashMap<String, String>();
            for (String item : line.substring(colon + 1).split(",")) {
                String[] pair = item.split("=", 2);if (pair.length == 2) values.put(pair[0], pair[1]);
            }
            try { result.add(new CommandStat(line.substring(8, colon), Long.parseLong(values.getOrDefault("calls", "0")),
                    Double.parseDouble(values.getOrDefault("usec", "0")), Double.parseDouble(values.getOrDefault("usec_per_call", "0")))); }
            catch (NumberFormatException ignored) { }
        }
        result.sort(Comparator.comparingDouble(CommandStat::totalMicros).reversed());return List.copyOf(result);
    }
    public static List<SlowEntry> slowlog(RedisConnectContext context) {
        List<Object> entries = LettuceUtils.execByMode(context, c -> c.slowlogGet(100), c -> c.slowlogGet(100));
        var result = new ArrayList<SlowEntry>();
        for (Object entry : entries) if (entry instanceof List<?> row && row.size() >= 4) {
            Object args = row.get(3);
            String command = args instanceof List<?> list && !list.isEmpty() ? asText(list.getFirst()) : "COMMAND";
            result.add(new SlowEntry(asText(row.get(0)), asText(row.get(1)), asText(row.get(2)), CommandRedactor.commandName(command)));
        }
        return List.copyOf(result);
    }
    private static String asText(Object value) { return value instanceof byte[] b ? new String(b, StandardCharsets.UTF_8) : String.valueOf(value); }

    public static List<String> sampleKeys(RedisConnectContext context, String pattern, int limit, BooleanSupplier cancelled) {
        if (limit < 1 || limit > 10_000) throw new IllegalArgumentException("Sample limit must be 1..10000");
        var keys = new LinkedHashSet<String>();ScanCursor cursor = ScanCursor.INITIAL;
        for (int page = 0; page < 1000 && !cancelled.getAsBoolean(); page++) {
            var next = RedisBasicService.service.scan(context, cursor, new ScanArgs().match(pattern).limit(100));
            for (String key : next.getKeys()) { keys.add(key);if (keys.size() >= limit) break; }
            cursor = next;
            if (cursor.isFinished() || keys.size() >= limit) break;
        }
        return List.copyOf(keys);
    }
    public static List<LargeKey> largeKeys(RedisConnectContext context, String pattern, BooleanSupplier cancelled, IntConsumer progress) {
        List<String> keys = sampleKeys(context, pattern, 1000, cancelled);
        var result = new ArrayList<LargeKey>();int visited = 0;
        for (String key : keys) {
            if (cancelled.getAsBoolean()) break;
            Long size = RedisMemoryService.usage(context, key);
            if (size != null) result.add(new LargeKey(key, RedisBasicService.service.type(context, key), size, RedisBasicService.service.ttl(context, key)));
            progress.accept(++visited);
        }
        result.sort(Comparator.comparingLong(LargeKey::bytes).reversed());return List.copyOf(result);
    }
    public static BatchResult unlink(RedisConnectContext context, List<String> keys, BooleanSupplier cancelled, IntConsumer progress) {
        return unlink(keys, cancelled, progress, key -> LettuceUtils.execByMode(context, c -> c.unlink(key), c -> c.unlink(key)));
    }
    static BatchResult unlink(List<String> keys, BooleanSupplier cancelled, IntConsumer progress, ToLongFunction<String> remove) {
        List<String> snapshot = List.copyOf(new LinkedHashSet<>(keys));int attempted = 0;long removed = 0;
        for (String key : snapshot) {
            if (cancelled.getAsBoolean()) break;
            removed += remove.applyAsLong(key);progress.accept(++attempted);
        }
        return new BatchResult(attempted, removed, attempted < snapshot.size());
    }
}
