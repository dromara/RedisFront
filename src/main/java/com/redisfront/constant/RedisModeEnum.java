package com.redisfront.constant;

public enum RedisModeEnum {
    STANDALONE("单机"),
    SENTINEL("哨兵"),
    CLUSTER("集群");

    public String modeName;

    RedisModeEnum(String modeName) {
        this.modeName = modeName;
    }


}
