package com.redisfront.commons.constant;

import java.io.File;

/**
 * Constant
 *
 * @author Jin
 */

public class Const {

    public static final String KEY_THEME = "theme";
    public static final String KEY_THEME_SELECT_INDEX = "selectThemeIndex";

    public static final String KEY_FONT_NAME = "fontName";

    public static final String KEY_FONT_SIZE = "fontSize";

    public static final String KEY_KEY_SEPARATOR = "keySeparator";

    public static final String KEY_KEY_MAX_LOAD_NUM = "keyMaxLoadNum";

    public static final String KEY_APP_DATABASE_INIT = "appDatabaseInit";

    public static final String KEY_LANGUAGE = "language";

    public static final String DATA_PATH = System.getProperty("user.home") + File.separator + "redis-front";

    public static final String LOG_FILE_PATH = Const.DATA_PATH + File.separator + "logs" + File.separator + "redis-front.log";

    public static final String DERBY_LOG_FILE = Const.DATA_PATH + File.separator + "derby" + File.separator + "derby.log";
    public static final String DERBY_LOG_FILE_PATH = Const.DATA_PATH + File.separator + "derby";
    public static final String DERBY_DATA_PATH = Const.DATA_PATH + File.separator + "derby" + File.separator + "data;";

    public static final String LOG_FILE = "LOG_FILE";

    public static final String PACKAGE_NAME = "com.redisfront";
    public static final String ROOT_PATH = "/redis-front";
}
