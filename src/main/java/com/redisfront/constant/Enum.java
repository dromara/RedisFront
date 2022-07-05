package com.redisfront.constant;

import java.awt.*;

public class Enum {

    public enum Connect {
        NORMAL,
        SSH
    }

    public enum KeyTypeEnum {
        STRING("String",new Color(20,92,205)),
        LIST("List",new Color(241,163,37)),
        HASH("Hash",new Color(56,176,63)),
        SET("Set",new Color(3,184,207)),
        ZSET("ZSet",new Color(189,123,70)),
        JSON("Json",new Color(134,102,184)),
        STREAM("Stream",new Color(234,100,74));


        private final Color color;
        private final String typeName;

        KeyTypeEnum(String typeName,Color color) {
            this.color = color;
            this.typeName = typeName;
        }

        public String typeName() {
            return typeName;
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
