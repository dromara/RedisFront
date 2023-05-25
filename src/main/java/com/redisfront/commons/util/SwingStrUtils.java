package com.redisfront.commons.util;

/**
 * Swing 组件文本处理类
 *
 * <p>
 * 由于 swing textarea 组件存在 bug，大量无效字符将导致死循环，这里单独创建工具类进行处理
 * </p>
 *
 * @auther cch
 **/
public class SwingStrUtils {

    private static final int UNKNOWN_CHAR = 65533;

    public static String formatTextArea(String text) {
        if (!isAllUnknownChar(text)) {
            return text;
        }
        int unknownMaxLen = 1024;
        return text.length() > unknownMaxLen ? text.substring(0, unknownMaxLen) + "..." : text;
    }

    public static boolean isAllUnknownChar(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != UNKNOWN_CHAR) {
                return false;
            }
        }
        return true;
    }
}
