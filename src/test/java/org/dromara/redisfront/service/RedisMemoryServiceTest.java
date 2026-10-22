package org.dromara.redisfront.service;

import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisConnectionException;
import org.dromara.redisfront.model.MemoryUsageResult;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RedisMemoryServiceTest {
    @Test void analysisDeduplicatesKeysPreservesLongAndDistinguishesMissingAndZero() {
        var updates = new ArrayList<RedisMemoryService.Update>();var calls = new ArrayList<String>();
        var result = RedisMemoryService.analyze(List.of("a", "a", "missing", "empty", "b"), key -> {
            calls.add(key);return switch (key) { case "missing" -> null;case "empty" -> 0L;default -> 3_000_000_000L; };
        }, () -> false, updates::add);
        assertEquals(List.of("a", "missing", "empty", "b"), calls);
        assertEquals(4, result.total());assertEquals(4, result.processed());assertEquals(3, result.measured());
        assertEquals(1, result.missing());assertEquals(6_000_000_000L, result.bytes());
        assertEquals(MemoryUsageResult.Status.MISSING, updates.get(1).result().status());
        assertEquals(0L, updates.get(2).result().bytes());assertFalse(result.cancelled());
    }

    @Test void cancellationKeepsTheCompletedMeasurementAndStopsBeforeTheNextCommand() {
        var cancelled = new AtomicBoolean();var calls = new AtomicInteger();
        var result = RedisMemoryService.analyze(List.of("a", "b", "c"), key -> { calls.incrementAndGet();return 80L; }, cancelled::get,
                update -> cancelled.set(true));
        assertEquals(1, calls.get());assertEquals(1, result.processed());assertEquals(80, result.bytes());assertTrue(result.cancelled());
    }

    @Test void unsupportedServerAndDeniedPermissionsStopWithoutTryingEveryKey() {
        for (String message : List.of("ERR unknown command 'MEMORY'", "NOPERM this user cannot run memory|usage", "NOAUTH Authentication required.")) {
            var calls = new AtomicInteger();var updates = new ArrayList<RedisMemoryService.Update>();
            var result = RedisMemoryService.analyze(List.of("a", "b"), key -> { calls.incrementAndGet();throw new RedisCommandExecutionException(message); }, () -> false, updates::add);
            assertEquals(1, calls.get());assertEquals(1, result.processed());assertNotNull(result.stopReason());
            assertEquals(0, result.bytes());assertNull(updates.getFirst().result().bytes());
            assertEquals(message.contains("unknown") ? 1 : 0, result.unsupported());
        }
    }

    @Test void networkFailureStopsButAKeySpecificFailureDoesNotHideOtherKeys() {
        var calls = new AtomicInteger();
        var failed = RedisMemoryService.analyze(List.of("a", "b"), key -> { calls.incrementAndGet();throw new RedisConnectionException("Unable to connect"); }, () -> false, update -> { });
        assertEquals(1, calls.get());assertEquals(1, failed.failed());assertNotNull(failed.stopReason());
        var partial = RedisMemoryService.analyze(List.of("bad", "good"), key -> {
            if (key.equals("bad")) throw new RedisCommandExecutionException("ERR unable to inspect this key");return 128L;
        }, () -> false, update -> { });
        assertEquals(2, partial.processed());assertEquals(1, partial.failed());assertEquals(1, partial.measured());assertEquals(128, partial.bytes());
        assertNull(partial.stopReason());
    }
}
