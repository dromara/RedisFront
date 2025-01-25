package org.dromara.redisfront.commons.handler;
@Deprecated
@FunctionalInterface
public interface ProcessHandler<T> {
    void processHandler(T t);
}
