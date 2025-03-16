package org.dromara.redisfront.ui.components.scanner;

public interface RedisDataScanner {
    void fetchData(String key);
    void refreshUI();
}
