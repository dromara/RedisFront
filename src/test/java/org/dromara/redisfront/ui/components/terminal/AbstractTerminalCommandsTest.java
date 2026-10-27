package org.dromara.redisfront.ui.components.terminal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AbstractTerminalCommandsTest {

    @Test
    void redisCommands_should_contain_common_commands() throws Exception {
        // 按类读静态字段即可：不要实例化 AbstractTerminal，其构造函数会创建 JWindow，无显示环境会抛 HeadlessException
        Field field = AbstractTerminal.class.getDeclaredField("redisCommands");
        field.setAccessible(true);
        String[] cmds = (String[]) field.get(null);

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
