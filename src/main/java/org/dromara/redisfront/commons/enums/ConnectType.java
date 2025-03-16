package org.dromara.redisfront.commons.enums;

public enum ConnectType {
        NORMAL,
        SSH;

        public static ConnectType of(String type) {
            for (ConnectType connectType : values()) {
                if (connectType.name().equals(type)) {
                    return connectType;
                }
            }
            return NORMAL;
        }
    }