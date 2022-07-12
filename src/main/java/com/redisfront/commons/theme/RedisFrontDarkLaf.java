package com.redisfront.commons.theme;

import com.formdev.flatlaf.FlatDarculaLaf;

public class RedisFrontDarkLaf extends FlatDarculaLaf {

    public static final String NAME = "RedisFront Dark";


    public static boolean setup() {
        return setup(new RedisFrontDarkLaf());
    }

    @Override
    public String getName() {
        return NAME;
    }


}
