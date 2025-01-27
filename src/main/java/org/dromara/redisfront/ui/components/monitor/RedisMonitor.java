package org.dromara.redisfront.ui.components.monitor;

import org.dromara.redisfront.Fn;
import org.dromara.redisfront.model.RedisUsageInfo;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisMonitor {
    private final ConnectContext context;
    private double lastUserCpu;
    private double lastSysCpu;
    private long lastCpuCheckTime;

    private double lastInputBytes;
    private double lastOutputBytes;
    private long lastIoCheckTime;

    public RedisMonitor(ConnectContext context) {
        this.context = context;
        this.initializeCpu();
        this.initializeIO();
    }

    private void initializeCpu() {
        Map<String, Object> cpuInfo = RedisBasicService.service.getCpuInfo(context);
        lastUserCpu = Double.parseDouble(cpuInfo.get("used_cpu_user").toString());
        lastSysCpu = Double.parseDouble(cpuInfo.get("used_cpu_sys").toString());
        lastCpuCheckTime = System.nanoTime();
    }

    private void initializeIO() {
        Map<String, Object> currentStats = RedisBasicService.service.getStatInfo(context);
        lastInputBytes = Double.parseDouble(currentStats.get("total_net_input_bytes").toString());
        lastOutputBytes = Double.parseDouble(currentStats.get("total_net_output_bytes").toString());
        lastIoCheckTime = System.nanoTime();
    }

    public RedisUsageInfo getUsageInfo() {
        RedisUsageInfo redisUsageInfo = new RedisUsageInfo();
        redisUsageInfo.setCpu(String.format("%.2f", calculateCpuUsage()) + "%");
        redisUsageInfo.setMemory(memoryInfo());
        redisUsageInfo.setNetwork(calculateNetworkRate());
        return redisUsageInfo;
    }

    private String memoryInfo() {
        Map<String, Object> memoryInfo = RedisBasicService.service.getMemoryInfo(context);
        if(Fn.isEmpty(memoryInfo)){
            return "00M";
        }
        return memoryInfo.get("used_memory_human").toString();
    }

    public RedisUsageInfo.NetworkStats calculateNetworkRate() {
        // 获取当前网络统计
        Map<String, Object> currentStats = RedisBasicService.service.getStatInfo(context);
        double currentInputBytes = Double.parseDouble(currentStats.get("total_net_input_bytes").toString());
        double currentOutputBytes = Double.parseDouble(currentStats.get("total_net_output_bytes").toString());

        // 计算时间差（单位：秒）
        long currentTime = System.nanoTime();
        double elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTime - lastIoCheckTime);

        // 检查时间差是否有效
        if (elapsedSeconds <= 0) {
            return new RedisUsageInfo.NetworkStats(0, 0); // 时间差无效，返回 0
        }

        // 计算字节数差值
        double inputDelta = currentInputBytes - lastInputBytes;
        double outputDelta = currentOutputBytes - lastOutputBytes;

        // 计算速率（字节/秒）
        double inputRate = inputDelta / elapsedSeconds;
        double outputRate = outputDelta / elapsedSeconds;

        // 保存当前值
        lastInputBytes = currentInputBytes;
        lastOutputBytes = currentOutputBytes;

        return new RedisUsageInfo.NetworkStats(inputRate, outputRate);
    }

    protected double calculateCpuUsage() {
        Map<String, Object> currentCpu = RedisBasicService.service.getCpuInfo(context);
        double currentUser = Double.parseDouble(currentCpu.get("used_cpu_user").toString());
        double currentSys = Double.parseDouble(currentCpu.get("used_cpu_sys").toString());

        // 计算时间差（单位：秒）
        long currentTime = System.nanoTime();
        double elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTime - lastCpuCheckTime);

        if (elapsedSeconds <= 0) {
            return 0.0;
        }

        // 计算CPU时间差
        double userDelta = currentUser - lastUserCpu;
        double sysDelta = currentSys - lastSysCpu;
        double totalDelta = userDelta + sysDelta;

        // 保存当前值
        lastUserCpu = currentUser;
        lastSysCpu = currentSys;
        lastCpuCheckTime = currentTime;

        // 计算CPU使用率（假设单核）
        return (totalDelta / elapsedSeconds) * 100;
    }
}