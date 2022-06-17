package cn.devcms.redisfront.common.util;

import java.util.prefs.Preferences;

/**
 * @author Karl Tauber
 */
public class PrefUtil {
    private static Preferences state;

    public static Preferences getState() {
        return state;
    }

    public static void init(String rootPath) {
        state = Preferences.userRoot().node(rootPath);
    }


}
