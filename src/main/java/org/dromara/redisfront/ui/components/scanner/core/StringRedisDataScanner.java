package org.dromara.redisfront.ui.components.scanner.core;

import org.dromara.redisfront.ui.components.scanner.RedisDataScanner;
import org.dromara.redisfront.ui.components.scanner.handler.ScanDataRefreshHandler;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.turbo.Turbo2;
import org.dromara.redisfront.service.RedisStringService;

public class StringRedisDataScanner implements RedisDataScanner {
    private final RedisConnectContext redisConnectContext;
    private final ScanDataRefreshHandler<Turbo2<Long, String>> consumer;
    private Long strLen;
    private String value;

    public StringRedisDataScanner(RedisConnectContext redisConnectContext, ScanDataRefreshHandler<Turbo2<Long, String>> consumer) {
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
        consumer.accept(new Turbo2<>(strLen, value));
    }
}
