package org.dromara.redisfront.ui.scanner;

import lombok.Getter;
import lombok.Setter;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.model.turbo.Turbo2;
import org.dromara.redisfront.service.RedisStringService;
import org.dromara.redisfront.ui.scanner.RedisDataScanner;

import java.util.function.Consumer;

public class StringRedisDataScanner implements RedisDataScanner {
    private final RedisConnectContext redisConnectContext;
    private final Consumer<Turbo2<Long, String>> consumer;
    private Long strLen;
    private String value;
    @Setter
    @Getter
    private String key;

    public StringRedisDataScanner(RedisConnectContext redisConnectContext, String key, Consumer<Turbo2<Long, String>> consumer) {
        this.redisConnectContext = redisConnectContext;
        this.consumer = consumer;
        this.key = key;
    }

    @Override
    public void fetchData(String fetchKey) {
        strLen = RedisStringService.service.strlen(redisConnectContext, key);
        value = RedisStringService.service.get(redisConnectContext, key);
    }

    @Override
    public void refreshUI() {
        consumer.accept(new Turbo2<>(strLen, value));
    }
}
