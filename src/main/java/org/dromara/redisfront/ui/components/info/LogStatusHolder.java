package org.dromara.redisfront.ui.components.info;

public class LogStatusHolder {
    private static final ThreadLocal<Boolean> THREAD_LOCAL = new ThreadLocal<>();

    public static void ignoredLog() {
        LogStatusHolder.THREAD_LOCAL.set(true);
    }

    public static Boolean getIgnoredLog() {
        return LogStatusHolder.THREAD_LOCAL.get();
    }

    public static void clear() {
        LogStatusHolder.THREAD_LOCAL.remove();
    }
}
