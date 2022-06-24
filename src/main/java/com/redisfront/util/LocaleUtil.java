package com.redisfront.util;

import com.redisfront.constant.Constant;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * ResourceBounds
 *
 * @author Jin
 */
public class LocaleUtil {

    static ResourceBundle bundle;

    public static void init() {
        var languageTag = PrefUtil.getState().get(Constant.KEY_LANGUAGE, Locale.SIMPLIFIED_CHINESE.toLanguageTag());
        Locale.setDefault(Locale.forLanguageTag(languageTag));
        bundle = ResourceBundle.getBundle("com.redisfront.RedisFront");
    }

    public static BundleInfo getMenu(String prefix) {
        var name = bundle.getString(prefix.concat(".Title"));
        var mnemonicStr = bundle.getString(prefix.concat(".Mnemonic"));
        var mnemonic = Fn.isEmpty(mnemonicStr) ? 0 : (int) mnemonicStr.charAt(0);
        var desc = bundle.getString(prefix.concat(".Desc"));
        return new BundleInfo(name, mnemonic, desc);
    }

    public static BundleInfo get(String prefix) {
        var name = bundle.getString(prefix.concat(".Title"));
        var desc = bundle.getString(prefix.concat(".Desc"));
        return new BundleInfo(name, 0, desc);
    }

    public static class BundleInfo {

        public BundleInfo(String title, Integer mnemonic, String desc) {
            this.title = title;
            this.mnemonic = mnemonic;
            this.desc = desc;
        }

        private String title;
        private Integer mnemonic;
        private String desc;

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
