package org.dromara.redisfront.commons.handler;

@FunctionalInterface
public interface ProcessHandler<T> {
    void processHandler(T t);
}
