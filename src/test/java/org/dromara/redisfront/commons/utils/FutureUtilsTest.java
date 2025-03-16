package org.dromara.redisfront.commons.utils;

import org.junit.jupiter.api.Test;

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

}
