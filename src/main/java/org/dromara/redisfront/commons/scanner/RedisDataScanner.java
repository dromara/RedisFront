package org.dromara.redisfront.commons.scanner;

public interface RedisDataScanner {
    void fetchData(String key);
    void refreshUI();
}
