package org.dromara.redisfront.service;

import org.dromara.redisfront.commons.enums.KeyTypeEnum;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class RedisEditServiceTest {
    private static byte[] bytes(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static RedisEditService.Target stringTarget() {
        return new RedisEditService.Target(KeyTypeEnum.STRING, "key", bytes("old"), null, null, null);
    }

    @Test void invalidTtlIsRejectedBeforeAnyRedisCommand() {
        var calls = new AtomicInteger();
        var edits = new RedisEditService((c, s, k, a) -> { calls.incrementAndGet(); return 1L; });
        for (String input : new String[]{"0", "-2", "", "abc", "9223372036854775807"}) {
            var failure = assertThrows(RedisEditService.EditException.class, () -> RedisEditService.parseTtl(input));
            assertEquals(RedisEditService.Reason.INVALID_TTL, failure.reason());
        }
        assertThrows(RedisEditService.EditException.class, () -> edits.updateMetadata(new RedisConnectContext(), "old", "new", 0L));
        assertEquals(0, calls.get());
        assertEquals(-1L, RedisEditService.parseTtl("-1"));
        assertEquals(60L, RedisEditService.parseTtl(" 60 "));
    }

    @Test void renameRetainsDistinctSourceAndDestinationAndExplicitTtlPolicy() {
        var keys = new AtomicReference<String[]>();
        var policy = new AtomicReference<String>();
        var edits = new RedisEditService((c, s, k, a) -> { keys.set(k); policy.set(new String(a[0], StandardCharsets.UTF_8)); return 1L; });
        edits.updateMetadata(new RedisConnectContext(), "original", "renamed", null);
        assertArrayEquals(new String[]{"original", "renamed"}, keys.get());
        assertEquals("keep", policy.get());
        edits.updateMetadata(new RedisConnectContext(), "renamed", "renamed", -1L);
        assertEquals("persist", policy.get());
    }

    @Test void crossSlotRenameFailsBeforeTheScriptIsSent() {
        var calls = new AtomicInteger();
        var edits = new RedisEditService((c, s, k, a) -> { calls.incrementAndGet(); return 1L; });
        var context = new RedisConnectContext();context.setRedisMode(RedisMode.CLUSTER);
        var failure = assertThrows(RedisEditService.EditException.class, () -> edits.updateMetadata(context, "old", "new", null));
        assertEquals(RedisEditService.Reason.CROSS_SLOT, failure.reason());
        assertEquals(0, calls.get());
        edits.updateMetadata(context, "{same}:old", "{same}:new", null);
        assertEquals(1, calls.get());
    }

    @Test void invalidZsetScoreNeverSendsAMutation() {
        var calls = new AtomicInteger();
        var edits = new RedisEditService((c, s, k, a) -> { calls.incrementAndGet(); return 1L; });
        var target = new RedisEditService.Target(KeyTypeEnum.ZSET, "key", bytes("old"), null, 1.0, null);
        for (String score : new String[]{"NaN", "Infinity", "-Infinity", "bad"}) {
            var failure = assertThrows(RedisEditService.EditException.class, () -> edits.save(new RedisConnectContext(), target, bytes("new"), score));
            assertEquals(RedisEditService.Reason.INVALID_SCORE, failure.reason());
        }
        assertEquals(0, calls.get());
    }

    @Test void scriptFailuresHaveStableReasonsAndAreNotRetried() {
        var reasons = new RedisEditService.Reason[]{RedisEditService.Reason.MISSING, RedisEditService.Reason.CONFLICT,
                RedisEditService.Reason.TARGET_EXISTS, RedisEditService.Reason.WRONG_TYPE, RedisEditService.Reason.PERMISSION};
        long[] codes = {0, -1, -2, -3, -5};
        for (int i = 0; i < codes.length; i++) {
            long result = codes[i];var calls = new AtomicInteger();
            var edits = new RedisEditService((c, s, k, a) -> { calls.incrementAndGet(); return result; });
            var failure = assertThrows(RedisEditService.EditException.class,
                    () -> edits.save(new RedisConnectContext(), stringTarget(), bytes("new"), ""));
            assertEquals(reasons[i], failure.reason());assertEquals(1, calls.get());
        }
    }

    @Test void targetCapturesAnIndependentByteSnapshot() {
        byte[] value = bytes("old");
        var target = new RedisEditService.Target(KeyTypeEnum.STRING, "key", value, null, null, null);
        value[0] = 'X';
        assertArrayEquals(bytes("old"), target.value());
    }
}
