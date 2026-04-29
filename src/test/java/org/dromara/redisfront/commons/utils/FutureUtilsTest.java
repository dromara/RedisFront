package org.dromara.redisfront.commons.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FutureUtilsTest {


    @Test
    public void test5() throws InterruptedException {
        FutureUtils.runAsync(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException("111111111");
        }, throwable -> System.out.println("捕获到异常：" + throwable.getMessage()));

        Thread.sleep(50);
    }

    @Test
    public void runAsync_should_use_redisfront_executor_threads() throws Exception {
        AtomicReference<String> name = new AtomicReference<>();
        FutureUtils.runAsync(() -> name.set(Thread.currentThread().getName())).get(3, TimeUnit.SECONDS);
        Assertions.assertNotNull(name.get());
        Assertions.assertTrue(name.get().startsWith("RedisFrontWorker-"));
    }

}
