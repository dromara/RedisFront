package org.dromara.redisfront.ui.components.scanner.core;

import org.dromara.redisfront.ui.components.scanner.RedisDataScanner;
import org.dromara.redisfront.ui.components.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.turbo.Turbo2;
import org.dromara.redisfront.model.value.RedisValueItem;
import org.dromara.redisfront.service.RedisStringService;

public class StringRedisDataScanner implements RedisDataScanner {
    private final RedisConnectContext redisConnectContext;
    private final ScanDataRefreshHandler<Turbo2<Long, RedisValueItem>> consumer;
    private Long strLen;
    private byte[] value;

    public StringRedisDataScanner(RedisConnectContext redisConnectContext, ScanDataRefreshHandler<Turbo2<Long, RedisValueItem>> consumer) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
    }

    @Override
    public void fetchData(String fetchKey) {
        strLen = RedisStringService.service.strlen(redisConnectContext, fetchKey);
        value = RedisStringService.service.get(redisConnectContext, fetchKey);
    }

    @Override
    public void refreshUI() {
        consumer.accept(new Turbo2<>(strLen, new RedisValueItem(value)));
    }
}
