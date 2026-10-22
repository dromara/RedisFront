package org.dromara.redisfront.service;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class RedisDiagnosticsServiceTest {
    @Test void latencyRankingHandlesNewInfoFieldsAndSkipsMalformedCounters() {
        var stats = RedisDiagnosticsService.parseCommandStats("# Commandstats\r\ncmdstat_get:calls=20,usec=100,usec_per_call=5,rejected_calls=0\n"
                + "cmdstat_set:calls=2,usec=200,usec_per_call=100\ncmdstat_bad:calls=oops\n");
        assertEquals(List.of("set", "get"), stats.stream().map(RedisDiagnosticsService.CommandStat::command).toList());
        assertEquals(20, stats.get(1).calls());assertEquals(100, stats.getFirst().averageMicros());
    }
    @Test void cancellingBatchStopsBeforeTheNextMutationAndKeepsAccurateCounts() {
        var cancelled = new AtomicBoolean();var removed = new ArrayList<String>();
        var result = RedisDiagnosticsService.unlink(List.of("one", "two", "three"), cancelled::get,
                progress -> cancelled.set(true), key -> { removed.add(key);return 1; });
        assertEquals(List.of("one"), removed);assertEquals(1, result.removed());assertEquals(1, result.attempted());assertTrue(result.cancelled());
    }
    @Test void batchUsesAnIndependentDeduplicatedSnapshot() {
        var keys = new ArrayList<>(List.of("one", "one", "two"));var removed = new ArrayList<String>();
        var result = RedisDiagnosticsService.unlink(keys, () -> false, progress -> keys.clear(), key -> { removed.add(key);return 1; });
        assertEquals(List.of("one", "two"), removed);assertEquals(2, result.removed());assertFalse(result.cancelled());
    }
}
