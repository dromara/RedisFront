package org.dromara.redisfront.commons.pool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RedisConnectionPoolManagerTest {

    @Test
    void commandLogInfo_should_not_include_args() {
        String info = RedisConnectionPoolManager.commandLogInfo("AUTH", "mypassword");
        Assertions.assertEquals("AUTH", info);
    }
}

