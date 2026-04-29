package org.dromara.redisfront.commons.utils;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ExceptionMessageUtils {

    public static Throwable unwrap(Throwable t) {
        Throwable current = t;
        while (current instanceof ExecutionException || current instanceof CompletionException) {
            Throwable cause = current.getCause();
            if (cause == null) {
                break;
            }
            current = cause;
        }
        return current;
    }

    public static Throwable rootCause(Throwable t) {
        Throwable current = unwrap(t);
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current == null ? t : current;
    }

    public static String bestMessage(Throwable t) {
        Throwable current = unwrap(t);
        while (current != null) {
            String msg = current.getMessage();
            if (msg != null && !msg.isBlank()) {
                return msg;
            }
            current = current.getCause();
        }
        return t == null ? "" : String.valueOf(t);
    }
}

