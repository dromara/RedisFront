package com.redisfront.exception;

public class RedisFrontException extends RuntimeException {

    private final Boolean showMessage;

    public Boolean showMessage() {
        return showMessage;
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
}
