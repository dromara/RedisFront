package com.redisfront.commons.constant;

import java.awt.*;

public class Enum {

    public enum Connect {
        NORMAL,
        SSH
    }

    public enum KeyTypeEnum {
        STRING("String", new Color(20, 92, 205)),
        LIST("List", new Color(222, 138, 4)),
        HASH("Hash", new Color(56, 176, 63)),
        SET("Set", new Color(3, 148, 166)),
        ZSET("ZSet", new Color(174, 30, 20)),
        JSON("Json", new Color(134, 102, 184)),
        STREAM("Stream", new Color(234, 100, 74));


        private final Color color;
        private final String typeName;

        KeyTypeEnum(String typeName, Color color) {
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
        STANDALONE("MainTabbedPanel.RedisMode.standalone"),
        SENTINEL("MainTabbedPanel.RedisMode.sentinel"),
        CLUSTER("MainTabbedPanel.RedisMode.cluster");

        public final String modeName;

        RedisMode(String modeName) {
            this.modeName = modeName;
        }


    }

}
