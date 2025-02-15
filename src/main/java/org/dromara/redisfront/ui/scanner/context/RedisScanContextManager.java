package org.dromara.redisfront.ui.scanner.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class RedisScanContextManager<T> {
    private final Map<String, RedisScanContext<T>> contexts = new LinkedHashMap<>();
    
    public RedisScanContext<T> getContext(String key) {
        return contexts.computeIfAbsent(key, _ -> new RedisScanContext<>());
    }
    
    public void reset(String key) {
        contexts.put(key, new RedisScanContext<>());
    }

}
