package org.dromara.redisfront.ui.components.scanner.context;

import java.util.LinkedHashMap;
import java.util.Map;

public class RedisScanContextManager<T> {
    private final Map<String, RedisScanContext<T>> contexts = new LinkedHashMap<>();
    
    public RedisScanContext<T> getContext(String key) {
        return contexts.computeIfAbsent(key, ignore -> new RedisScanContext<>());
    }
    
    public void reset(String key) {
        contexts.put(key, new RedisScanContext<>());
    }

    public void rename(String oldKey, String newKey) {
        if (oldKey.equals(newKey)) return;
        RedisScanContext<T> context = contexts.remove(oldKey);
        contexts.remove(newKey);
        if (context != null) contexts.put(newKey, context);
    }

}
