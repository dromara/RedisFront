package org.dromara.redisfront.service;

import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;

/** Bounded GETRANGE keeps a large or rapidly growing String from exhausting the editor. */
public final class RedisValueLoader {
    public static final int AUTO_LIMIT = 1024 * 1024;
    public static final int CHUNK_SIZE = 64 * 1024;
    public static final int FULL_LIMIT = 16 * 1024 * 1024;
    public record Snapshot(byte[] value, long total, long offset, boolean partial) { }
    interface Source { long size();byte[] range(long start, long stop); }
    private RedisValueLoader() { }
    public static Snapshot load(RedisConnectContext context, String key, long offset, boolean full) {
        return load(new Source() {
            public long size() { return RedisStringService.service.strlen(context, key); }
            public byte[] range(long start, long stop) {
                return LettuceUtils.execByMode(context, c -> c.getrange(key, start, stop), c -> c.getrange(key, start, stop));
            }
        }, offset, full);
    }
    static Snapshot load(Source source, long offset, boolean full) {
        if (offset < 0) throw new IllegalArgumentException("Invalid preview offset");
        long size = source.size();
        if (full && size > FULL_LIMIT) throw new IllegalArgumentException("Full editor loading is limited to 16 MiB; use chunk preview");
        int limit = full ? FULL_LIMIT : size <= AUTO_LIMIT && offset == 0 ? AUTO_LIMIT : CHUNK_SIZE;
        byte[] value = source.range(offset, Math.addExact(offset, limit) - 1);
        if (value == null) throw new IllegalStateException("The key no longer exists");
        long after = source.size();
        return new Snapshot(value, after, offset, offset != 0 || value.length < after || after != size);
    }
}
