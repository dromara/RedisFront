package org.dromara.redisfront.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ExceptionMessageUtilsTest {

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

