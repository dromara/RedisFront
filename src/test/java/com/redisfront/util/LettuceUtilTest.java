package com.redisfront.util;

import com.redisfront.commons.util.LettuceUtil;
import com.redisfront.model.ConnectInfo;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.ValueScanCursor;
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

    @Test
    public void test2() {
        LettuceUtil.run(new ConnectInfo().setHost("127.0.0.1").setPort(6379).setSsl(false), redisCommands -> {
            ScanArgs scanArgs = new ScanArgs();
            scanArgs.limit(5);

            ValueScanCursor<String> valueScanCursor = redisCommands.sscan("aaa", scanArgs);

            ValueScanCursor<String> valueScanCursor2 = redisCommands.sscan("aaa", new ScanCursor(valueScanCursor.getCursor(), valueScanCursor.isFinished()), scanArgs);

            ValueScanCursor<String> valueScanCursor3 = redisCommands.sscan("aaa", new ScanCursor(valueScanCursor2.getCursor(), valueScanCursor2.isFinished()), scanArgs);


            System.out.println();
        });
    }

}
