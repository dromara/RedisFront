package org.dromara.redisfront.commons.pool;

public enum CleanMode {
    ALL,
    CLUSTER_ONLY,
    SENTINEL_ONLY,
    CONTEXT,
    NORMAL_ONLY
}
