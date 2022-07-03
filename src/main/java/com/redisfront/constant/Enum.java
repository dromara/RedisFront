package com.redisfront.constant;

import java.awt.*;

public class Enum {

    public enum Connect {
        NORMAL,
        SSH
    }

    public enum KeyTypeEnum {
        STRING(Color.MAGENTA),
        LIST(Color.ORANGE),
        HASH(Color.red),
        SET(Color.blue),
        ZSET(Color.pink),
        JSON(Color.orange),
        STREAM(Color.MAGENTA);


        private final Color color;

        KeyTypeEnum(Color color) {
            this.color = color;
        }

        public Color color() {
            return color;
        }
    }

    public enum RedisMode {
        STANDALONE("单机模式"),
        SENTINEL("哨兵模式"),
        CLUSTER("集群模式");

        public final String modeName;

        RedisMode(String modeName) {
            this.modeName = modeName;
        }


    }

}
