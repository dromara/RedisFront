package com.redisfront.constant;

public class Enum {

    public enum Connect {
        NORMAL,
        SSH
    }

    public enum KeyTypeEnum {
        STRING,
        LIST,
        HASH,
        SET,
        ZSET,
        JSON,
        STREAM
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
