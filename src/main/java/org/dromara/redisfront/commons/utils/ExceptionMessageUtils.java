package org.dromara.redisfront.commons.utils;

import com.jcraft.jsch.JSchException;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisConnectionException;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ExceptionMessageUtils {
    private static final Pattern EXCEPTION_PREFIX = Pattern.compile(
            "^(?:(?:[\\w$]+\\.)*[\\w$]*(?:Exception|Error):\\s*)+");

    /** User-facing summary. Keep bestMessage and the original throwable for diagnostic logs. */
    public static String userMessage(Throwable error, Function<String, String> tr) {
        List<Throwable> causes = new ArrayList<>();
        var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable current = error; current != null && seen.add(current); current = current.getCause()) {
            causes.add(current);
        }
        var messages = causes.stream().map(Throwable::getMessage).filter(message -> message != null && !message.isBlank())
                .map(message -> EXCEPTION_PREFIX.matcher(message.strip()).replaceFirst("")).toList();
        String combined = String.join("\n", messages).toLowerCase(Locale.ROOT);
        boolean ssh = causes.stream().anyMatch(JSchException.class::isInstance);
        if (combined.matches("(?s).*(?:^|\\n)(?:wrongpass|noauth)(?:\\s.*|$)")) return tr.apply("Error.redisAuthentication");
        if (ssh && (combined.contains("auth fail") || combined.contains("auth cancel"))) return tr.apply("Error.sshAuthentication");
        if (combined.matches("(?s).*(?:^|\\n)noperm(?:\\s.*|$)")) return tr.apply("Error.permission");
        if (causes.stream().anyMatch(cause -> cause instanceof SocketTimeoutException
                || cause instanceof TimeoutException || cause instanceof RedisCommandTimeoutException)
                || combined.contains("timed out") || combined.contains("connection timeout")) return tr.apply("Error.timeout");
        if (causes.stream().anyMatch(UnknownHostException.class::isInstance)) return tr.apply("Error.unknownHost");
        if (causes.stream().anyMatch(cause -> cause instanceof ConnectException || cause instanceof NoRouteToHostException
                || cause instanceof RedisConnectionException) || combined.contains("unable to connect")
                || combined.contains("connection refused") || combined.contains("no route to host")) {
            return tr.apply("Error.connection");
        }
        // Preserve validation/business messages, but do not expose wrapper names or whole stack traces.
        for (String message : messages) {
            if (message.isBlank()) continue;
            String firstLine = message.lines().filter(line -> !line.isBlank()).findFirst().orElse("").strip();
            firstLine = firstLine.replace("/<unresolved>", "").replace("<unresolved>", "");
            if (firstLine.matches("(?:[\\w$]+\\.)*[\\w$]*(?:Exception|Error)")) continue;
            if (!firstLine.isBlank()) return firstLine.length() > 200 ? firstLine.substring(0, 197) + "…" : firstLine;
        }
        return tr.apply("Error.generic");
    }

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

