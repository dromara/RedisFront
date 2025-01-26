package org.dromara.redisfront.commons.utils;


import cn.hutool.core.date.StopWatch;
import io.lettuce.core.*;
import io.lettuce.core.output.*;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.dromara.redisfront.model.context.ConnectContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * LettuceUtilTest
 *
 * @author Jin
 */
public class LettuceUtilsTest {

    @Test
    public void test5() {

    }

        @Test
    public void test1() {
        StopWatch watch = StopWatch.create("T1");
            watch.start();
            try {
                LettuceUtils.run(new ConnectContext(), redisCommands -> {
                    if (redisCommands.getStatefulConnection() instanceof StatefulRedisConnectionImpl<String, String> statefulRedisConnection) {
                        List<Object> s = redisCommands.dispatch(CommandType.GET, new ArrayOutput<>(statefulRedisConnection.getCodec()), new CommandArgs<>(statefulRedisConnection.getCodec()).addKeys("a"));
                        System.out.println(s);
                    }
                });
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

            watch.stop();
            System.out.println(watch.prettyPrint(TimeUnit.SECONDS));
    }

    //    @Test
    public void test3() {
        String[] list = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "l", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        for (int i = 0; i < 26; i++) {
            int finalI = i;
            String key = list[finalI] + ":" + finalI;
            LettuceUtils.run(new ConnectContext(), redisCommands -> {
                redisCommands.set(key, String.valueOf(finalI));
            });
            for (int j = 0; j < 200; j++) {
                String key2 = key + ":" + j;
                int finalJ = j;
                LettuceUtils.run(new ConnectContext(), redisCommands -> {
                    redisCommands.set(key2, String.valueOf(finalJ));
                });

                for (int k = 0; k < 50; k++) {
                    String key3 = key2 + ":" + k;
                    int finalK = k;
                    LettuceUtils.run(new ConnectContext(), redisCommands -> {
                        redisCommands.set(key3, String.valueOf(finalK));
                    });
                }
            }
        }

    }

    //    @Test
    public void test2() {
        LettuceUtils.run(new ConnectContext(), redisCommands -> {
            ScanArgs scanArgs = new ScanArgs();
            scanArgs.limit(5);

            ValueScanCursor<String> valueScanCursor = redisCommands.sscan("aaa", scanArgs);

            ValueScanCursor<String> valueScanCursor2 = redisCommands.sscan("aaa", new ScanCursor(valueScanCursor.getCursor(), valueScanCursor.isFinished()), scanArgs);

            ValueScanCursor<String> valueScanCursor3 = redisCommands.sscan("aaa", new ScanCursor(valueScanCursor2.getCursor(), valueScanCursor2.isFinished()), scanArgs);


            System.out.println();
        });
    }

}
