package org.dromara.redisfront.commons.constant;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.dialect.PropsUtil;

import java.io.File;

/**
 * Constant
 *
 * @author Jin
 */

public class Constants {

    public static final String APP_NAME = "RedisFront";
    public static final String APP_THEME_PACKAGE = "org.dromara.redisfront.theme";
    public static final String APP_RESOURCE_BUNDLE = "org.dromara.redisfront.RedisFront";

    public static final String APP_VERSION = PropsUtil.get("application.properties").getProperty("version", "2024.1");
    public static final String APP_COPYRIGHT = PropsUtil.get("application.properties").getProperty("copyright", "RedisFront");

    public static final String SQL_CREATE_CONNECT_DETAIL = """
            create table connect_detail
            (
                id           integer not null
                    constraint connect_detail_pk
                        primary key autoincrement,
                name         TEXT,
                group_id     integer,
                host         TEXT,
                port         integer,
                username     TEXT,
                password     TEXT,
                connect_mode TEXT,
                redis_mode TEXT,
                setting   TEXT,
                enable_ssl integer,
                ssl_config   TEXT,
                ssh_config   TEXT
            );
            """;
    public static final String SQL_CREATE_CONNECT_GROUP = """
            create table connect_group
            (
                group_id   integer not null
                    constraint connect_group_pk
                        primary key autoincrement,
                group_name TEXT,
                enable_ssh integer default 0,
                ssh_config TEXT
            );
            """;


    public static final String KEY_THEME = "theme1";
    public static final String KEY_THEME_SELECT_INDEX = "selectThemeIndex";

    public static final String KEY_FONT_NAME = "fontName";

    public static final String KEY_FONT_SIZE = "fontSize";
    public static final String KEY_SSH_TIMEOUT = "sshTimeout";
    public static final String KEY_REDIS_TIMEOUT = "redisTimeout";

    public static final String KEY_KEY_SEPARATOR = "keySeparator";

    public static final String KEY_KEY_MAX_LOAD_NUM = "keyMaxLoadNum";

    public static final String KEY_APP_DATABASE_INIT = "appDatabaseInit";

    public static final String KEY_LANGUAGE = "language";

    @Deprecated
    public static final String DATA_PATH = FileUtil.getUserHomePath() + File.separator + "redis-front";
    @Deprecated
    public static final String CONFIG_DATA_PATH = DATA_PATH + File.separator + "config";

}
