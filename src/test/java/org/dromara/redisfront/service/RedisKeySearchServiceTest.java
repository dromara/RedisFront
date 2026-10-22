package org.dromara.redisfront.service;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.commons.enums.KeySearchMode;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class RedisKeySearchServiceTest {
    private static KeyScanCursor<String> batch(String cursor, boolean finished, String... keys) {
        var result = new KeyScanCursor<String>();result.setCursor(cursor);result.setFinished(finished);result.getKeys().addAll(List.of(keys));return result;
    }
    private static class Source implements RedisKeySearchService.Source {
        final List<KeyScanCursor<String>> batches;
        final List<ScanCursor> cursors = new ArrayList<>();
        String pattern, exactKey;
        int calls;
        Source(KeyScanCursor<String>... batches) { this.batches = List.of(batches); }
        public KeyScanCursor<String> scan(ScanCursor cursor, String pattern, int count) {
            cursors.add(cursor);this.pattern = pattern;return batches.get(calls++);
        }
        public boolean exists(String key) { exactKey = key;return key.equals(" *?[]\\ "); }
    }
    private static RedisKeySearchService.Session session(KeySearchMode mode, String text, Source source) {
        return new RedisKeySearchService.Session(new RedisKeySearchService.Query(mode, text), source, 100);
    }
    @Test void containsEscapesRedisMetacharactersAndKeepsWhitespace() {
        var query = new RedisKeySearchService.Query(KeySearchMode.CONTAINS, " 中文:*?[]\\ ");
        assertEquals("* 中文:\\*\\?\\[\\]\\\\ *", query.pattern());
        assertEquals("*", new RedisKeySearchService.Query(KeySearchMode.CONTAINS, "").pattern());
    }
    @Test void globIsPassedToRedisWithoutClientSideContainsFiltering() {
        var source = new Source(batch("0", true, "user:01", "user:02"));
        var result = session(KeySearchMode.GLOB, "*user:[0-9]?*", source).readPage(10, () -> false, p -> { });
        assertEquals("*user:[0-9]?*", source.pattern);assertEquals(List.of("user:01", "user:02"), result.keys());
    }
    @Test void exactChecksTheLiteralKeyWithoutScanning() {
        var source = new Source();var exact = session(KeySearchMode.EXACT, " *?[]\\ ", source);
        var result = exact.readPage(10, () -> false, p -> { });
        assertEquals(" *?[]\\ ", source.exactKey);assertEquals(List.of(" *?[]\\ "), result.keys());
        assertTrue(result.progress().complete());assertEquals(0, source.calls);
        assertTrue(session(KeySearchMode.EXACT, "missing", source).readPage(10, () -> false, p -> { }).keys().isEmpty());
    }
    @Test void emptyBatchesAndZeroValuedUnfinishedCursorsDoNotEndTheSearch() {
        var first = batch("0", false);var second = batch("48", false);var last = batch("0", true, "found");
        var source = new Source(first, second, last);
        var result = session(KeySearchMode.GLOB, "found*", source).readPage(10, () -> false, p -> { });
        assertEquals(List.of("found"), result.keys());assertTrue(result.progress().complete());
        assertSame(first, source.cursors.get(1));assertSame(second, source.cursors.get(2));assertEquals(3, source.calls);
    }
    @Test void paginationDeduplicatesWithoutDroppingOversizedBatches() {
        var first = batch("8", false, "one", "one", "two", "three");
        var source = new Source(first, batch("0", true, "two", "four"));var session = session(KeySearchMode.CONTAINS, "", source);
        var page = session.readPage(1, () -> false, p -> { });
        assertEquals(List.of("one", "two", "three"), page.keys());assertFalse(page.progress().complete());
        var done = session.readPage(1, () -> false, p -> { });
        assertEquals(List.of("one", "two", "three", "four"), done.keys());assertTrue(done.progress().complete());
    }
    @Test void cancellationRetainsTheReceivedBatchAndResumesTheSameCursor() {
        var first = batch("12", false, "one");var source = new Source(first, batch("0", true, "two"));
        var session = session(KeySearchMode.GLOB, "*", source);var cancelled = new AtomicBoolean();
        var stopped = session.readPage(10, cancelled::get, p -> cancelled.set(true));
        assertEquals(List.of("one"), stopped.keys());assertEquals(1, source.calls);assertFalse(stopped.progress().complete());
        cancelled.set(false);var resumed = session.readPage(10, cancelled::get, p -> { });
        assertEquals(List.of("one", "two"), resumed.keys());assertTrue(resumed.progress().complete());assertSame(first, source.cursors.get(1));
    }
    @Test void hittingTheResultLimitIsDistinctFromCompletingTheScan() {
        var source = new Source(batch("13", false, "one", "two", "three", "four"));
        var session = new RedisKeySearchService.Session(new RedisKeySearchService.Query(KeySearchMode.GLOB, "*"), source, 3);
        var result = session.readPage(10, () -> false, p -> { });
        assertEquals(3, result.keys().size());assertTrue(result.progress().limited());assertFalse(result.progress().complete());
        session.readPage(10, () -> false, p -> { });assertEquals(1, source.calls);
    }
    @Test void oversizedFinalBatchIsNotMislabelledAsACompleteResult() {
        var source = new Source(batch("0", true, "one", "two", "three", "four"));
        var session = new RedisKeySearchService.Session(new RedisKeySearchService.Query(KeySearchMode.GLOB, "*"), source, 3);
        var result = session.readPage(10, () -> false, p -> { });
        assertEquals(3, result.keys().size());assertTrue(result.progress().limited());assertFalse(result.progress().complete());
    }
}
