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
    private Integer connectedClients;
    private Long commandsProcessed;


    public record NetworkStats(double inputRate, double outputRate, String formatMessage) {
        @Override
        public @NotNull String toString() {
            return String.format(formatMessage, inputRate / 1024, outputRate / 1024);
        }
    }

    public record MemoryUsage(double usedMemory, double usedMemoryRss, double fragmentationRatio, String formatMessage) {
        @Override
        public @NotNull String toString() {
            return String.format(formatMessage, usedMemory / 1024 / 1024, usedMemoryRss / 1024 / 1024, fragmentationRatio * 100);
        }
    }

    public record CpuStats(double systemCpuLoad, double processCpuLoad, String formatMessage) {
        @Override
        public @NotNull String toString() {
            return String.format(formatMessage, systemCpuLoad * 100, processCpuLoad * 100);
        }
    }


}
