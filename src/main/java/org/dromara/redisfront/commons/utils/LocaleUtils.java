package org.dromara.redisfront.commons.utils;


import org.dromara.redisfront.Fn;

import java.lang.reflect.Method;
import java.util.ResourceBundle;

/**
 * ResourceBounds
 *
 * @author Jin
 */
public class LocaleUtils {

    private static Method cachedGetBundleMethod = null;


    public static String getMessageFromBundle(String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = LocaleUtils.class;
            if (cachedGetBundleMethod == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                cachedGetBundleMethod = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) cachedGetBundleMethod.invoke(null, "org/dromara/redisfront/RedisFront", thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("org/dromara/redisfront/RedisFront");
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
