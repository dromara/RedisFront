package org.dromara.redisfront.ui.components.monitor;

import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@ToString
public class RedisUsageInfo {
    private String cpu;
    private String memory;
    private NetworkStats network;
    private MemoryUsage memoryUsage;

    public record NetworkStats(double inputRate, double outputRate) {
        @Override
        public @NotNull String toString() {
            return String.format("输入速率: %.2f KB/s, 输出速率: %.2f KB/s", inputRate / 1024, outputRate / 1024);
        }
    }

    public record MemoryUsage(double usedMemory, double usedMemoryRss, double fragmentationRatio) {
        @Override
        public @NotNull String toString() {
            return String.format("已使用内存: %.2f MB, 已使用内存RSS: %.2f MB, 内存碎片率: %.2f%%", usedMemory / 1024 / 1024, usedMemoryRss / 1024 / 1024, fragmentationRatio * 100);
        }
    }

    public record CpuStats(double systemCpuLoad, double processCpuLoad) {
        @Override
        public @NotNull String toString() {
            return String.format("系统CPU使用率: %.2f%%, 进程CPU使用率: %.2f%%", systemCpuLoad * 100, processCpuLoad * 100);
        }
    }

}
