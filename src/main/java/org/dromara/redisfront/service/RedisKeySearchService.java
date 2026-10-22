package org.dromara.redisfront.service;

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import org.dromara.redisfront.commons.enums.KeySearchMode;
import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** One immutable query and its actual cursor, including Lettuce's multi-node Cluster cursor. */
public final class RedisKeySearchService {
    public static final int MAX_RESULTS = 300_000;

    public record Query(KeySearchMode mode, String text) {
        public Query { Objects.requireNonNull(mode);Objects.requireNonNull(text); }
        public boolean exact() { return mode == KeySearchMode.EXACT && !text.isEmpty(); }
        public String pattern() {
            if (text.isEmpty()) return "*";
            return mode == KeySearchMode.GLOB ? text : mode == KeySearchMode.CONTAINS
                    ? "*" + escapeLiteral(text) + "*" : escapeLiteral(text);
        }
    }
    public record Progress(int matches, long batches, boolean complete, boolean limited) { }
    public record Snapshot(List<String> keys, Progress progress) { }
    interface Source {
        KeyScanCursor<String> scan(ScanCursor cursor, String pattern, int count);
        boolean exists(String key);
    }
    private RedisKeySearchService() { }

    public static String escapeLiteral(String text) {
        var result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '*' || ch == '?' || ch == '[' || ch == ']' || ch == '\\') result.append('\\');
            result.append(ch);
        }
        return result.toString();
    }

    public static Session start(RedisConnectContext context, Query query) {
        RedisConnectContext target = context.clone();
        return new Session(query, new Source() {
            public KeyScanCursor<String> scan(ScanCursor cursor, String pattern, int count) {
                return RedisBasicService.service.scan(target, cursor, new ScanArgs().match(pattern).limit(count));
            }
            public boolean exists(String key) {
                return LettuceUtils.execByMode(target, c -> c.exists(key), c -> c.exists(key)) > 0;
            }
        }, MAX_RESULTS);
    }

    public static final class Session {
        private final Query query;
        private final Source source;
        private final int maximum;
        private final LinkedHashSet<String> matches = new LinkedHashSet<>();
        private ScanCursor cursor = ScanCursor.INITIAL;
        private long batches;
        private boolean complete, limited;

        Session(Query query, Source source, int maximum) {
            this.query = Objects.requireNonNull(query);this.source = source;
            if (maximum < 1) throw new IllegalArgumentException("Invalid result limit");
            this.maximum = maximum;
        }
        public Query query() { return query; }
        public Progress progress() { return new Progress(matches.size(), batches, complete, limited); }
        public Snapshot snapshot() { return new Snapshot(List.copyOf(matches), progress()); }

        /** Cancellation is cooperative: a received batch is retained before the next command is stopped. */
        public Snapshot readPage(int pageSize, BooleanSupplier cancelled, Consumer<Progress> progress) {
            if (pageSize < 1 || pageSize > 10_000) throw new IllegalArgumentException("Page size must be 1..10000");
            if (complete || limited || cancelled.getAsBoolean()) return snapshot();
            if (query.exact()) {
                if (source.exists(query.text())) matches.add(query.text());
                complete = true;progress.accept(progress());return snapshot();
            }
            int target = Math.min(maximum, matches.size() + pageSize);
            while (!complete && !limited && matches.size() < target && !cancelled.getAsBoolean()) {
                KeyScanCursor<String> next = source.scan(cursor, query.pattern(), Math.min(pageSize, 1000));
                // Do not reconstruct this cursor from its string: Cluster also stores the remaining nodes.
                cursor = next;batches++;
                boolean truncated = false;
                for (String key : next.getKeys()) {
                    if (matches.contains(key)) continue;
                    if (matches.size() >= maximum) { truncated = true;break; }
                    matches.add(key);
                }
                complete = next.isFinished() && !truncated;
                limited = truncated || !complete && matches.size() >= maximum;
                progress.accept(progress());
            }
            return snapshot();
        }
    }
}
