package org.dromara.redisfront.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.jcraft.jsch.JSchException;
import io.lettuce.core.RedisConnectionException;
import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.commons.exception.RedisFrontException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ExceptionMessageUtilsTest {
    private final ResourceBundle chinese = ResourceBundle.getBundle(Constants.APP_RESOURCE_BUNDLE, Locale.SIMPLIFIED_CHINESE);

    private String message(Throwable error) {
        return ExceptionMessageUtils.userMessage(error, chinese::getString);
    }

    @Test
    void userMessage_should_summarize_nested_connection_failure_without_class_names() {
        var connection = new RedisConnectionException("Unable to connect to redis.internal/<unresolved>:6379", new ConnectException("Connection refused"));
        var wrapped = new ExecutionException(new RedisFrontException(connection, false));
        Assertions.assertEquals(chinese.getString("Error.connection"), message(wrapped));
        Assertions.assertFalse(message(wrapped).contains("Exception"));
        Assertions.assertFalse(message(wrapped).contains("<unresolved>"));
    }

    @Test
    void unresolved_socket_notation_alone_does_not_mean_dns_failed() {
        var error = new RedisFrontException("org.dromara.redisfront.commons.exception.RedisFrontException: Unable to connect to redis.internal/<unresolved>:6379");
        Assertions.assertEquals(chinese.getString("Error.connection"), message(error));
        Assertions.assertEquals(chinese.getString("Error.connection"), message(new RedisConnectionException("Unable to connect to noauth.wrongpass.noperm.internal:6379")));
    }

    @Test
    void authentication_failure_should_take_precedence_over_connection_wrapper() {
        var error = new RedisConnectionException("Unable to connect", new RedisFrontException("WRONGPASS invalid username-password pair or user is disabled."));
        Assertions.assertEquals(chinese.getString("Error.redisAuthentication"), message(error));
        Assertions.assertEquals(chinese.getString("Error.redisAuthentication"), message(new RedisFrontException("NOAUTH Authentication required.")));
        Assertions.assertEquals(chinese.getString("Error.sshAuthentication"), message(new JSchException("Auth fail for methods 'publickey,password'")));
    }

    @Test
    void missing_ssh_algorithms_should_explain_how_to_repair_the_runtime() {
        String expected = chinese.getString("Error.sshCryptoUnavailable");
        var cause = new NoSuchAlgorithmException("Algorithm ECDH not available");
        Assertions.assertEquals(expected, message(new ExecutionException(new JSchException("SSH handshake failed", cause))));
        Assertions.assertEquals(expected, message(cause));
        Assertions.assertEquals(expected, message(new JSchException("java.security.NoSuchAlgorithmException: Algorithm ECDH not available")));
        Assertions.assertEquals(expected, message(new NoSuchAlgorithmException("EC KeyPairGenerator not available")));
    }

    @Test
    void actual_timeout_and_dns_causes_should_remain_distinguishable() {
        Assertions.assertEquals(chinese.getString("Error.timeout"), message(new RedisConnectionException("Unable to connect", new SocketTimeoutException("Read timed out"))));
        Assertions.assertEquals(chinese.getString("Error.unknownHost"), message(new RedisConnectionException("Unable to connect", new UnknownHostException("redis.invalid"))));
    }

    @Test
    void validation_messages_should_survive_async_wrappers() {
        String validation = "请先填写连接名称。";
        var error = new CompletionException(new RedisFrontException(validation));
        Assertions.assertEquals(validation, message(error));
        Assertions.assertEquals("example.com: not a Java exception", message(new IllegalArgumentException("example.com: not a Java exception")));
    }

    @Test
    void permission_failure_should_be_actionable_without_raw_server_details() {
        Assertions.assertEquals(chinese.getString("Error.permission"), message(new RedisFrontException("NOPERM this user has no permissions to run the 'set' command")));
    }

    @Test
    void fallback_should_remove_nested_prefixes_and_keep_messages_bounded() {
        String text = "The operation could not be completed. ".repeat(12);
        var error = new IllegalStateException("java.util.concurrent.ExecutionException: java.lang.IllegalArgumentException: " + text + "\n\tat hidden.internal.Class.method(Class.java:7)");
        String result = message(error);
        Assertions.assertTrue(result.startsWith("The operation could not be completed."));
        Assertions.assertTrue(result.length() <= 200);
        Assertions.assertFalse(result.contains("hidden.internal"));
        Assertions.assertFalse(result.contains("Exception"));
    }

    @Test
    void missing_messages_and_cyclic_causes_should_use_generic_text() {
        Assertions.assertEquals(chinese.getString("Error.generic"), message(null));
        Assertions.assertEquals(chinese.getString("Error.generic"), message(new NullPointerException()));
        var one = new RuntimeException("");
        var two = new RuntimeException("");
        one.initCause(two);
        two.initCause(one);
        Assertions.assertEquals(chinese.getString("Error.generic"), message(new CompletionException(one)));
    }

    @Test
    void user_message_should_follow_the_selected_language() {
        var english = ResourceBundle.getBundle(Constants.APP_RESOURCE_BUNDLE, Locale.ENGLISH);
        Assertions.assertEquals(english.getString("Error.connection"), ExceptionMessageUtils.userMessage(new ConnectException("Connection refused"), english::getString));
    }

    @Test
    void bestMessage_should_pick_first_non_blank_in_chain() {
        RuntimeException e = new RuntimeException("", new IllegalStateException("root"));
        Assertions.assertEquals("root", ExceptionMessageUtils.bestMessage(e));
    }

    @Test
    void unwrap_should_unwrap_execution_exception() {
        RuntimeException root = new RuntimeException("root");
        ExecutionException ex = new ExecutionException(root);
        Assertions.assertSame(root, ExceptionMessageUtils.unwrap(ex));
    }

    @Test
    void unwrap_should_unwrap_completion_exception() {
        RuntimeException root = new RuntimeException("root");
        CompletionException ex = new CompletionException(root);
        Assertions.assertSame(root, ExceptionMessageUtils.unwrap(ex));
    }
}

