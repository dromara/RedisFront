package org.dromara.redisfront.service;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class RedisValueLoaderTest {
    private static class Source implements RedisValueLoader.Source {
        final byte[] value;
        long readStart, readStop;
        Source(int size) { value = new byte[size];Arrays.fill(value, (byte) 'x'); }
        public long size() { return value.length; }
        public byte[] range(long start, long stop) {
            readStart = start;readStop = stop;
            return Arrays.copyOfRange(value, (int) Math.min(start, value.length), (int) Math.min(stop + 1, value.length));
        }
    }
    @Test void ordinaryValuesAreCompleteWithoutAnUnboundedGet() {
        var source = new Source(100);var result = RedisValueLoader.load(source, 0, false);
        assertFalse(result.partial());assertArrayEquals(source.value, result.value());assertEquals(0, source.readStart);
        assertTrue(source.readStop < RedisValueLoader.AUTO_LIMIT);
    }
    @Test void largeValuesAreBoundedAndPagingPreservesByteOffsets() {
        var source = new Source(RedisValueLoader.AUTO_LIMIT + 1);
        var result = RedisValueLoader.load(source, RedisValueLoader.CHUNK_SIZE, false);
        assertTrue(result.partial());assertEquals(RedisValueLoader.CHUNK_SIZE, result.value().length);
        assertEquals(RedisValueLoader.CHUNK_SIZE, result.offset());assertEquals(result.offset(), source.readStart);
    }
    @Test void fullLoadingHasAHardLimitAndDoesNotReadOversizedValues() {
        var source = new Source(1) { @Override public long size() { return RedisValueLoader.FULL_LIMIT + 1L; } };
        assertThrows(IllegalArgumentException.class, () -> RedisValueLoader.load(source, 0, true));
        assertEquals(0, source.readStop);
    }
    @Test void growingValueCannotTurnATruncatedResponseIntoAnEditableSnapshot() {
        var source = new Source(100) {
            int reads;
            @Override public long size() { return ++reads == 1 ? 100 : RedisValueLoader.FULL_LIMIT * 2L; }
        };
        var result = RedisValueLoader.load(source, 0, false);
        assertTrue(result.partial());assertEquals(100, result.value().length);
    }
}
