package com.redisfront.commons.util;

import com.redisfront.commons.constant.Const;
import com.redisfront.commons.func.Fn;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ResourceBounds
 *
 * @author Jin
 */
public class LocaleUtils {

    private static Method cachedGetBundleMethod = null;

    static {
        var languageTag = PrefUtils.getState().get(Const.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
        Locale.setDefault(Locale.forLanguageTag(languageTag));
    }

    public static String getMessageFromBundle(String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = LocaleUtils.class;
            if (cachedGetBundleMethod == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                cachedGetBundleMethod = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) cachedGetBundleMethod.invoke(null, "com/redisfront/RedisFront", thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("com/redisfront/RedisFront");
        }
        return bundle.getString(key);
    }

    private LocaleUtils() {
    }

    public static BundleInfo getMenu(String prefix) {
        var name = getMessageFromBundle(prefix.concat(".Title"));
        var mnemonicStr = getMessageFromBundle(prefix.concat(".Mnemonic"));
        var mnemonic = Fn.isEmpty(mnemonicStr) ? 0 : (int) mnemonicStr.charAt(0);
        var desc = getMessageFromBundle(prefix.concat(".Desc"));
        return new BundleInfo(name, mnemonic, desc);
    }

    public static BundleInfo get(String prefix) {
        var name = getMessageFromBundle(prefix.concat(".Title"));
        var desc = getMessageFromBundle(prefix.concat(".Desc"));
        return new BundleInfo(name, 0, desc);
    }

    public static class BundleInfo {

        public BundleInfo(String title, Integer mnemonic, String desc) {
            this.title = title;
            this.mnemonic = mnemonic;
            this.desc = desc;
        }

        private final String title;
        private final Integer mnemonic;
        private final String desc;

        public String title() {
            return title;
        }

        public int mnemonic() {
            return mnemonic;
        }

        public String desc() {
            return desc;
        }
    }

}
