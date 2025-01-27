package org.dromara.redisfront.model;
import lombok.Data;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@ToString
public class RedisUsageInfo {
    private String cpu;
    private String memory;
    private NetworkStats network;

    public record NetworkStats(double inputRate, double outputRate) {
        @Override
        public @NotNull String toString() {
            return String.format("输入速率: %.2f KB/s, 输出速率: %.2f KB/s", inputRate/ 1024, outputRate/ 1024);
        }
    }
}
