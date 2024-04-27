package org.dromara.redisfront.commons.theme;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class RedisFrontMacLightLaf extends FlatMacLightLaf {

    public static final String NAME = "MacOS Light";


    public static boolean setup() {
        return setup(new RedisFrontMacLightLaf());
    }

    @Override
    public String getName() {
        return NAME;
    }


}
