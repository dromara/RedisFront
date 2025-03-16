package org.dromara.redisfront.commons.enums;

public enum RedisMode {
        STANDALONE("MainTabbedPanel.RedisMode.standalone"),
        SENTINEL("MainTabbedPanel.RedisMode.sentinel"),
        CLUSTER("MainTabbedPanel.RedisMode.cluster");

        public final String modeName;

        RedisMode(String modeName) {
            this.modeName = modeName;
        }

        public static RedisMode of(String modeName) {
            for (RedisMode redisMode : values()) {
                if (redisMode.modeName.equals(modeName)) {
                    return redisMode;
                }
            }
            return STANDALONE;
        }
    }