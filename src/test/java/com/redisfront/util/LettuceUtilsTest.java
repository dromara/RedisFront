package com.redisfront.util;

import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.*;
import io.lettuce.core.output.*;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.junit.jupiter.api.Test;

import java.util.List;


/**
 * LettuceUtilTest
 *
 * @author Jin
 */
public class LettuceUtilsTest {

    @Test
    public void test5() {

    }

    //    @Test
    public void test1() {
        LettuceUtils.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
            if (redisCommands.getStatefulConnection() instanceof StatefulRedisConnectionImpl<String, String> statefulRedisConnection) {
                List<Object> s = redisCommands.dispatch(CommandType.GET, new ArrayOutput<>(statefulRedisConnection.getCodec()), new CommandArgs<>(statefulRedisConnection.getCodec()).addKeys("a"));
                System.out.println(s);
            }
        });
    }

    //    @Test
    public void test3() {
        String[] list = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "l", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        for (int i = 0; i < 26; i++) {
            int finalI = i;
            String key = list[finalI] + ":" + finalI;
            LettuceUtils.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
                redisCommands.set(key, String.valueOf(finalI));
            });
            for (int j = 0; j < 200; j++) {
                String key2 = key + ":" + j;
                int finalJ = j;
                LettuceUtils.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
                    redisCommands.set(key2, String.valueOf(finalJ));
                });

                for (int k = 0; k < 50; k++) {
                    String key3 = key2 + ":" + k;
                    int finalK = k;
                    LettuceUtils.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
                        redisCommands.set(key3, String.valueOf(finalK));
                    });
                }
            }
        }

    }

    //    @Test
    public void test2() {
        LettuceUtils.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
            ScanArgs scanArgs = new ScanArgs();
            scanArgs.limit(5);

            ValueScanCursor<String> valueScanCursor = redisCommands.sscan("aaa", scanArgs);

            ValueScanCursor<String> valueScanCursor2 = redisCommands.sscan("aaa", new ScanCursor(valueScanCursor.getCursor(), valueScanCursor.isFinished()), scanArgs);

            ValueScanCursor<String> valueScanCursor3 = redisCommands.sscan("aaa", new ScanCursor(valueScanCursor2.getCursor(), valueScanCursor2.isFinished()), scanArgs);


            System.out.println();
        });
    }

}
