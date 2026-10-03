package org.dromara.redisfront.ui.components.monitor;

import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.info.LogStatusHolder;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RedisMonitor {
    private final RedisFrontWidget owner;
    private final RedisConnectContext context;
    private double lastUserCpu;
    private double lastSysCpu;
    private long lastCpuCheckTime;

    private double lastInputBytes;
    private double lastOutputBytes;
    private long lastIoCheckTime;

    public RedisMonitor(RedisFrontWidget owner, RedisConnectContext context) {
        this.owner = owner;
        this.context = context;
        this.initializeCpu();
        this.initializeIO();
    }

    private void initializeCpu() {
        try {
            LogStatusHolder.ignoredLog();
            Map<String, Object> cpuInfo = RedisBasicService.service.getCpuInfo(context);
            LogStatusHolder.clear();
            lastUserCpu = Double.parseDouble(cpuInfo.get("used_cpu_user").toString());
            lastSysCpu = Double.parseDouble(cpuInfo.get("used_cpu_sys").toString());
            lastCpuCheckTime = System.nanoTime();
        } finally {
            LogStatusHolder.clear();
        }
    }

    private void initializeIO() {
        try {
            LogStatusHolder.ignoredLog();
            Map<String, Object> currentStats = RedisBasicService.service.getStatInfo(context);
            LogStatusHolder.clear();
            lastInputBytes = Double.parseDouble(currentStats.get("total_net_input_bytes").toString());
            lastOutputBytes = Double.parseDouble(currentStats.get("total_net_output_bytes").toString());
            lastIoCheckTime = System.nanoTime();
        } finally {
            LogStatusHolder.clear();
        }
    }

    /**
     * 一次 INFO 采集全部指标，替代原先按 section 拆分的多次网络往返
     */
    public RedisUsageInfo getUsageInfo() {
        Map<String, Object> info = fetchInfo();
        RedisUsageInfo redisUsageInfo = new RedisUsageInfo();
        redisUsageInfo.setCpu(String.format("%.2f", calculateCpuUsage(info)) + "%");
        redisUsageInfo.setMemory(memoryInfo(info));
        redisUsageInfo.setNetwork(calculateNetworkRate(info));
        redisUsageInfo.setConnectedClients(Integer.valueOf(info.get("connected_clients").toString()));
        redisUsageInfo.setCommandsProcessed(Long.valueOf(info.get("total_commands_processed").toString()));
        return redisUsageInfo;
    }

    private Map<String, Object> fetchInfo() {
        try {
            LogStatusHolder.ignoredLog();
            return RedisBasicService.service.getInfo(context);
        } finally {
            LogStatusHolder.clear();
        }
    }

    private String memoryInfo(Map<String, Object> info) {
        if (RedisFrontUtils.isEmpty(info)) {
            return "00M";
        }
        return info.get("used_memory_human").toString();
    }

    public RedisUsageInfo.MemoryUsage memoryUsageInfo() {
        try {
            LogStatusHolder.ignoredLog();
            Map<String, Object> memoryInfo = RedisBasicService.service.getMemoryInfo(context);
            LogStatusHolder.clear();
            if (RedisFrontUtils.isEmpty(memoryInfo)) {
                return new RedisUsageInfo.MemoryUsage(0, 0, 0, owner.$tr("RedisUsageInfo.MemoryUsage.text"));
            }
            return new RedisUsageInfo.MemoryUsage(
                    Double.parseDouble(memoryInfo.get("used_memory").toString()) / (1024 * 1024),
                    Double.parseDouble(memoryInfo.get("used_memory_rss").toString()) / (1024 * 1024),
                    (Double.parseDouble(memoryInfo.get("used_memory").toString()) / (1024 * 1024)) / (Double.parseDouble(memoryInfo.get("used_memory_rss").toString()) / (1024 * 1024))
                    , owner.$tr("RedisUsageInfo.MemoryUsage.text")
            );
        } finally {
            LogStatusHolder.clear();
        }
    }

    public RedisUsageInfo.NetworkStats calculateNetworkRate() {
        return calculateNetworkRate(fetchInfo());
    }

    private RedisUsageInfo.NetworkStats calculateNetworkRate(Map<String, Object> currentStats) {
        double currentInputBytes = Double.parseDouble(currentStats.get("total_net_input_bytes").toString());
        double currentOutputBytes = Double.parseDouble(currentStats.get("total_net_output_bytes").toString());
        long currentTime = System.nanoTime();
        double elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTime - lastIoCheckTime);

        if (elapsedSeconds <= 0) {
            return new RedisUsageInfo.NetworkStats(0, 0, owner.$tr("RedisUsageInfo.NetworkStats.text"));
        }

        double inputDelta = currentInputBytes - lastInputBytes;
        double outputDelta = currentOutputBytes - lastOutputBytes;
        double inputRate = inputDelta / elapsedSeconds;
        double outputRate = outputDelta / elapsedSeconds;

        lastInputBytes = currentInputBytes;
        lastOutputBytes = currentOutputBytes;

        return new RedisUsageInfo.NetworkStats(inputRate, outputRate, owner.$tr("RedisUsageInfo.NetworkStats.text"));
    }

    protected double calculateCpuUsage() {
        return calculateCpuUsage(fetchInfo());
    }

    private double calculateCpuUsage(Map<String, Object> currentCpu) {
        double currentUser = Double.parseDouble(currentCpu.get("used_cpu_user").toString());
        double currentSys = Double.parseDouble(currentCpu.get("used_cpu_sys").toString());

        long currentTime = System.nanoTime();
        double elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTime - lastCpuCheckTime);

        if (elapsedSeconds <= 0) {
            return 0.0;
        }

        double userDelta = currentUser - lastUserCpu;
        double sysDelta = currentSys - lastSysCpu;
        double totalDelta = userDelta + sysDelta;

        lastUserCpu = currentUser;
        lastSysCpu = currentSys;
        lastCpuCheckTime = currentTime;

        // 计算CPU使用率（假设单核）
        return (totalDelta / elapsedSeconds) * 100;
    }
}
