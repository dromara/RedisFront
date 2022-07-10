package com.redisfront.util;

import com.redisfront.model.ConnectInfo;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.output.*;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;


/**
 * LettuceUtilTest
 *
 * @author Jin
 */
public class LettuceUtilTest {


    @Test
    public void test1() {
        LettuceUtil.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
            if (redisCommands.getStatefulConnection() instanceof StatefulRedisConnectionImpl<String, String> statefulRedisConnection) {
                List<Object> s = redisCommands.dispatch(CommandType.GET, new ArrayOutput<>(statefulRedisConnection.getCodec()), new CommandArgs<>(statefulRedisConnection.getCodec()).addKeys("a"));
                System.out.println(s);
            }
        });
    }

}
