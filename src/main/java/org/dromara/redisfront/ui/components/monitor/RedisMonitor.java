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

    public RedisUsageInfo getUsageInfo() {
        try {
            RedisUsageInfo redisUsageInfo = new RedisUsageInfo();
            redisUsageInfo.setCpu(String.format("%.2f", calculateCpuUsage()) + "%");
            redisUsageInfo.setMemory(memoryInfo());
            redisUsageInfo.setNetwork(calculateNetworkRate());
            LogStatusHolder.ignoredLog();
            Object object = RedisBasicService.service.getClientInfo(context).get("connected_clients");
            LogStatusHolder.clear();
            redisUsageInfo.setConnectedClients(Integer.valueOf(object.toString()));
            LogStatusHolder.ignoredLog();
            object = RedisBasicService.service.getStatInfo(context).get("total_commands_processed");
            LogStatusHolder.clear();
            redisUsageInfo.setCommandsProcessed(Long.valueOf(object.toString()));
            return redisUsageInfo;
        } finally {
            LogStatusHolder.clear();
        }
    }

    private String memoryInfo() {
        try {
            LogStatusHolder.ignoredLog();
            Map<String, Object> memoryInfo = RedisBasicService.service.getMemoryInfo(context);
            LogStatusHolder.clear();
            if (RedisFrontUtils.isEmpty(memoryInfo)) {
                return "00M";
            }
            return memoryInfo.get("used_memory_human").toString();
        } finally {
            LogStatusHolder.clear();
        }
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
        try {
            // 获取当前网络统计
            LogStatusHolder.ignoredLog();
            Map<String, Object> currentStats = RedisBasicService.service.getStatInfo(context);
            LogStatusHolder.clear();
            double currentInputBytes = Double.parseDouble(currentStats.get("total_net_input_bytes").toString());
            double currentOutputBytes = Double.parseDouble(currentStats.get("total_net_output_bytes").toString());
            currentStats.clear();
            // 计算时间差（单位：秒）
            long currentTime = System.nanoTime();
            double elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTime - lastIoCheckTime);

            // 检查时间差是否有效
            if (elapsedSeconds <= 0) {
                return new RedisUsageInfo.NetworkStats(0, 0, owner.$tr("RedisUsageInfo.NetworkStats.text"));
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

            return new RedisUsageInfo.NetworkStats(inputRate, outputRate, owner.$tr("RedisUsageInfo.NetworkStats.text"));
        } finally {
            LogStatusHolder.clear();
        }
    }

    protected double calculateCpuUsage() {
        try {
            LogStatusHolder.ignoredLog();
            Map<String, Object> currentCpu = RedisBasicService.service.getCpuInfo(context);
            LogStatusHolder.clear();
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
        } finally {
            LogStatusHolder.clear();
        }
    }
}
