package org.dromara.redisfront.commons.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandRedactorTest {
    @Test void logsRetainOnlyTheCommandName() {
        assertEquals("AUTH", CommandRedactor.commandName(" \tAUTH user do-not-log-this\n"));
        assertEquals("SET", CommandRedactor.commandName("set sensitive-key sensitive-value"));
        assertEquals("", CommandRedactor.commandName(null));
        assertEquals("COMMAND", CommandRedactor.commandName("'invalid command' secret"));
    }
    @Test void credentialCommandsNeverBelongInTerminalHistory() {
        for (String command : new String[]{"AUTH pass", "hello 3 AUTH user pass", "ACL SETUSER name >password", "CONFIG SET requirepass password"}) {
            assertTrue(CommandRedactor.sensitive(command));
        }
        assertFalse(CommandRedactor.sensitive("GET key"));
    }
}
