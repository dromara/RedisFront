package org.dromara.redisfront.commons.utils;

import java.util.Locale;

public final class CommandRedactor {
    private CommandRedactor() { }
    /** Keep command names only: values, scripts, keys and credentials never enter diagnostic logs. */
    public static String commandName(String text) {
        if (text == null || text.isBlank()) return "";
        String name = text.stripLeading().split("\\s+", 2)[0].toUpperCase(Locale.ROOT);
        return name.matches("[A-Z][A-Z0-9_:.-]{0,63}") ? name : "COMMAND";
    }
    public static boolean sensitive(String text) {
        String name = commandName(text);
        return name.equals("AUTH") || name.equals("HELLO") || name.equals("ACL") || name.equals("CONFIG");
    }
}
