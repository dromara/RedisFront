package org.dromara.redisfront.model;

import java.io.Serializable;

public record MemoryUsageResult(Status status, Long bytes, long timestamp, String error) implements Serializable {
    public enum Status { UNANALYZED, ANALYZING, COMPLETE, MISSING, FAILED, UNSUPPORTED, CANCELLED }

    public MemoryUsageResult {
        if (status == null || status == Status.COMPLETE && (bytes == null || bytes < 0)
                || status != Status.COMPLETE && bytes != null) throw new IllegalArgumentException("Invalid memory measurement");
    }

    public static MemoryUsageResult state(Status status) { return new MemoryUsageResult(status, null, 0, null); }
    public static MemoryUsageResult measured(long bytes) { return new MemoryUsageResult(Status.COMPLETE, bytes, System.currentTimeMillis(), null); }
}
