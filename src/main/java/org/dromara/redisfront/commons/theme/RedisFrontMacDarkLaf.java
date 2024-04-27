package org.dromara.redisfront.commons.theme;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;

public class RedisFrontMacDarkLaf extends FlatMacDarkLaf {

    public static final String NAME = "MacOS Dark";


    public static boolean setup() {
        return setup(new RedisFrontMacDarkLaf());
    }

    @Override
    public String getName() {
        return NAME;
    }


}
