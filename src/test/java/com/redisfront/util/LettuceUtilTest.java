package com.redisfront.util;

import com.redisfront.model.ConnectInfo;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.output.ByteArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.junit.jupiter.api.Test;


/**
 * LettuceUtilTest
 *
 * @author Jin
 */
public class LettuceUtilTest {


    @Test
    public void test1() {
        LettuceUtil.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
            String info = redisCommands.info();
            System.out.println(info);
            if (redisCommands.getStatefulConnection() instanceof StatefulRedisConnectionImpl<String, String> statefulRedisConnection) {
                byte[] s = redisCommands.dispatch(CommandType.SET, new ByteArrayOutput<>(statefulRedisConnection.getCodec()), new CommandArgs<>(statefulRedisConnection.getCodec()).addKey("AAAAAAAL").addValue("FFFFFFFFFFFFFF"));
                String ss = new String(s);
                System.out.println(ss);
            }
        });
    }

}
