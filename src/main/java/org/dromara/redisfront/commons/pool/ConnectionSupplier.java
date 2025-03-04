package org.dromara.redisfront.commons.pool;

@FunctionalInterface
public interface ConnectionSupplier<T> {
    T get() throws Exception;
}
