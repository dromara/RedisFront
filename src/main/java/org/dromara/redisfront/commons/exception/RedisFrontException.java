package org.dromara.redisfront.commons.exception;

import java.util.function.Supplier;

public class RedisFrontException extends RuntimeException implements Supplier<Object> {

    private final Boolean showMessage;

    public Boolean showMessage() {
        return showMessage;
    }

    public RedisFrontException(String message) {
        super(message);
        this.showMessage = false;
    }

    public RedisFrontException(String message, Boolean showMessage) {
        super(message);
        this.showMessage = showMessage;
    }

    public RedisFrontException(String message, Throwable cause, Boolean showMessage) {
        super(message, cause);
        this.showMessage = showMessage;
    }

    public RedisFrontException(Throwable cause, Boolean showMessage) {
        super(cause);
        this.showMessage = showMessage;
    }

    public RedisFrontException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Boolean showMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.showMessage = showMessage;
    }

    @Override
    public Object get() {
        return null;
    }
}
