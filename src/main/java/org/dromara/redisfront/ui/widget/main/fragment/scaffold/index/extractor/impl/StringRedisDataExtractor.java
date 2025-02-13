package org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.extractor.impl;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisStringService;
import org.dromara.redisfront.ui.widget.main.fragment.scaffold.index.extractor.RedisDataExtractor;

public class StringRedisDataExtractor implements RedisDataExtractor {
    private final RedisConnectContext redisConnectContext;
    private final String key;

    public StringRedisDataExtractor(RedisConnectContext redisConnectContext, String key) {
        this.redisConnectContext = redisConnectContext;
        this.key = key;
    }

    @Override
    public void loadData() {
        var strLen = RedisStringService.service.strlen(redisConnectContext, key);
        var value = RedisStringService.service.get(redisConnectContext, key);
    }

    @Override
    public void setupUI() {

    }
}
