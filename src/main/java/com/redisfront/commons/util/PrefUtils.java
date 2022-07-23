package com.redisfront.commons.util;

import java.util.prefs.Preferences;

/**
 * @author Karl Tauber
 */
public class PrefUtils {

    private PrefUtils() {
    }
    private static Preferences state;

    public static Preferences getState() {
        return state;
    }

    public static void init(String rootPath) {
        state = Preferences.userRoot().node(rootPath);
    }


}
