package org.dromara.redisfront.ui.components.terminal;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AbstractTerminalCommandsTest {

    @Test
    void redisCommands_should_contain_common_commands() throws Exception {
        AbstractTerminal terminal = new AbstractTerminal() {
            @Override
            protected void inputProcessHandler(String input) {
            }

            @Override
            protected RedisConnectContext connectInfo() {
                return new RedisConnectContext();
            }

            @Override
            protected String databaseName() {
                return "0";
            }
        };

        Field field = AbstractTerminal.class.getDeclaredField("redisCommands");
        field.setAccessible(true);
        String[] cmds = (String[]) field.get(terminal);

        Set<String> set = Arrays.stream(cmds).collect(Collectors.toSet());
        Assertions.assertTrue(set.contains("SCAN"));
        Assertions.assertTrue(set.contains("KEYS"));
        Assertions.assertTrue(set.contains("TYPE"));
        Assertions.assertTrue(set.contains("TTL"));
        Assertions.assertTrue(set.contains("PING"));
        Assertions.assertTrue(set.contains("INFO"));
        Assertions.assertTrue(set.contains("SELECT"));
        Assertions.assertTrue(set.contains("MGET"));
        Assertions.assertTrue(set.contains("HGET"));
        Assertions.assertTrue(set.contains("LRANGE"));
        Assertions.assertTrue(set.contains("ZRANGE"));
        Assertions.assertTrue(set.contains(":VIEW"));
    }
}

