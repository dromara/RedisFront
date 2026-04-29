package org.dromara.redisfront.commons.utils;

import org.dromara.redisfront.commons.lettuce.LettuceUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * LettuceUtilTest
 *
 * @author Jin
 */
public class LettuceUtilsTest {

    @Test
    public void createRedisURI_should_not_throw_when_setting_present() {
        RedisConnectContext ctx = new RedisConnectContext();
        ctx.setId(1);
        ctx.setSetting(new RedisConnectContext.SettingInfo(1000, ":", 3000, 3000));
        Assertions.assertNotNull(LettuceUtils.createRedisURI(ctx));
    }

}
