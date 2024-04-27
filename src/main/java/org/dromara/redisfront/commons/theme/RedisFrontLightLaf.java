package org.dromara.redisfront.commons.theme;

import com.formdev.flatlaf.FlatIntelliJLaf;

public class RedisFrontLightLaf extends FlatIntelliJLaf {

    public static final String NAME = "RedisFront Light";


    public static boolean setup() {
        return setup(new RedisFrontLightLaf());
    }

    @Override
    public String getName() {
        return NAME;
    }


}
