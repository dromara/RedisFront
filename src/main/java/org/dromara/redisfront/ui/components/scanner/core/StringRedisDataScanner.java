package org.dromara.redisfront.ui.components.scanner.core;

import org.dromara.redisfront.ui.components.scanner.RedisDataScanner;
import org.dromara.redisfront.ui.components.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.turbo.Turbo2;
import org.dromara.redisfront.model.value.RedisValueItem;
import org.dromara.redisfront.service.RedisStringService;
import org.dromara.redisfront.service.RedisValueLoader;

public class StringRedisDataScanner implements RedisDataScanner {
    private final RedisConnectContext redisConnectContext;
    private final ScanDataRefreshHandler<Turbo2<Long, RedisValueItem>> consumer;
    private Long strLen;
    private byte[] value;
    private RedisValueLoader.Snapshot snapshot;

    public RedisValueLoader.Snapshot getSnapshot() { return snapshot; }

    public StringRedisDataScanner(RedisConnectContext redisConnectContext, ScanDataRefreshHandler<Turbo2<Long, RedisValueItem>> consumer) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
    }

    @Override
    public void fetchData(String fetchKey) {
        fetchChunk(fetchKey, 0, false);
    }

    public void fetchChunk(String key, long offset, boolean full) {
        snapshot = RedisValueLoader.load(redisConnectContext, key, offset, full);
        strLen = snapshot.total();value = snapshot.value();
    }

    @Override
    public void refreshUI() {
        consumer.accept(new Turbo2<>(strLen, new RedisValueItem(value)));
    }
}
