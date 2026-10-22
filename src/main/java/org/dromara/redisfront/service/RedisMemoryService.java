package org.dromara.redisfront.service;

import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisConnectionException;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.commons.utils.ExceptionMessageUtils;
import org.dromara.redisfront.model.MemoryUsageResult;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.function.*;

import static org.dromara.redisfront.model.MemoryUsageResult.Status.*;

/** Server-side per-key memory estimates. Never fetch values or iterate collection members. */
public final class RedisMemoryService {
    public static final int SAMPLES = 5;
    private static final Logger log = LoggerFactory.getLogger(RedisMemoryService.class);
    private RedisMemoryService() { }

    public record Summary(int total, int processed, int measured, int missing, int failed, int unsupported,
                          long bytes, boolean cancelled, String stopReason) { }
    public record Update(String key, MemoryUsageResult result, Summary progress) { }

    public static Long usage(RedisConnectContext context, String key) {
        // Lettuce 6.5 exposes the default form; Redis defaults to SAMPLES 5.
        return LettuceUtils.execByMode(context, c -> c.memoryUsage(key), c -> c.memoryUsage(key));
    }

    public static Summary analyze(RedisConnectContext context, Collection<String> keys, BooleanSupplier cancelled, Consumer<Update> updates) {
        return analyze(keys, key -> usage(context, key), cancelled, updates);
    }

    static Summary analyze(Collection<String> keys, Function<String, Long> reader, BooleanSupplier cancelled, Consumer<Update> updates) {
        var snapshot = new LinkedHashSet<>(keys);
        int processed = 0, measured = 0, missing = 0, failed = 0, unsupported = 0;
        long bytes = 0;
        String stopReason = null;
        for (String key : snapshot) {
            if (cancelled.getAsBoolean()) break;
            MemoryUsageResult result;
            boolean stop = false;
            try {
                Long value = reader.apply(key);
                if (value == null) {
                    result = new MemoryUsageResult(MISSING, null, System.currentTimeMillis(), null);
                    missing++;
                } else {
                    result = MemoryUsageResult.measured(value);
                    bytes = Math.addExact(bytes, value);
                    measured++;
                }
            } catch (RuntimeException error) {
                String detail = ExceptionMessageUtils.bestMessage(error);
                String messages = causeMessages(error);
                boolean unknownCommand = messages.contains("unknown command") && messages.contains("memory")
                        || messages.contains("unknown subcommand") && messages.contains("usage");
                boolean notSupported = unknownCommand || messages.contains("memory") && messages.contains("not supported");
                result = new MemoryUsageResult(notSupported ? UNSUPPORTED : FAILED, null, System.currentTimeMillis(), detail);
                if (notSupported) unsupported++; else failed++;
                stop = unknownCommand || messages.contains("noperm") || messages.contains("noauth") || messages.contains("wrongpass")
                        || connectionFailure(error);
                if (stop) stopReason = detail;
                log.debug("Memory analysis failed", error);
            }
            processed++;
            var progress = new Summary(snapshot.size(), processed, measured, missing, failed, unsupported, bytes, false, stopReason);
            updates.accept(new Update(key, result, progress));
            if (stop) break;
        }
        return new Summary(snapshot.size(), processed, measured, missing, failed, unsupported, bytes,
                cancelled.getAsBoolean() && processed < snapshot.size(), stopReason);
    }

    private static String causeMessages(Throwable error) {
        var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        var text = new StringBuilder();
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) text.append(cause.getMessage()).append('\n');
        return text.toString().toLowerCase(Locale.ROOT);
    }

    private static boolean connectionFailure(Throwable error) {
        var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof RedisConnectionException || cause instanceof RedisCommandTimeoutException
                    || cause instanceof ConnectException || cause instanceof SocketTimeoutException) return true;
        }
        return false;
    }
}
