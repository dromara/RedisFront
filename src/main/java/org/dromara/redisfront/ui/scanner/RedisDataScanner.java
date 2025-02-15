package org.dromara.redisfront.ui.scanner;

public interface RedisDataScanner {
    void fetchData(String key);
    void refreshUI();
}
