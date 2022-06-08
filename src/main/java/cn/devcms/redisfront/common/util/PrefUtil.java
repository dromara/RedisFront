package cn.devcms.redisfront.common.util;

import cn.devcms.redisfront.common.constant.Constant;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.LoggingFacade;

import javax.swing.*;
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
