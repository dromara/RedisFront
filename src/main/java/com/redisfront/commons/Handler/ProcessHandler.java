package com.redisfront.commons.Handler;

@FunctionalInterface
public interface ProcessHandler<T> {
    void processHandler(T t);
}
