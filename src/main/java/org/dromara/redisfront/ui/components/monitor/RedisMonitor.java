package org.dromara.redisfront.ui.components.monitor;

import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.info.LogStatusHolder;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;
import java.util.Map;

public class RedisMonitor {
    private final RedisFrontWidget owner;
    private final RedisConnectContext context;
    private final RedisMetricSampler sampler = new RedisMetricSampler();

    public RedisMonitor(RedisFrontWidget owner, RedisConnectContext context) {
        this.owner = owner;
        this.context = context;
    }

    public RedisUsageInfo getUsageInfo() {
        Map<String, Object> info = fetchInfo();
        var sample = sampler.sample(info, System.nanoTime(), System.currentTimeMillis());
        var usage = new RedisUsageInfo();
        usage.setCpu(String.format("%.2f%%", sample.cpuPercent()));
        usage.setMemory(String.valueOf(info.getOrDefault("used_memory_human", "0B")));
        usage.setNetwork(new RedisUsageInfo.NetworkStats(sample.inputRate(), sample.outputRate(),
                owner.$tr("RedisUsageInfo.NetworkStats.text")));
        usage.setMemoryUsage(new RedisUsageInfo.MemoryUsage(sample.usedMemory(), sample.rssMemory(),
                sample.fragmentationRatio(), owner.$tr("RedisUsageInfo.MemoryUsage.text")));
        usage.setConnectedClients((int) sample.connectedClients());
        usage.setCommandsProcessed(sample.commandsProcessed());
        return usage;
    }

    private Map<String, Object> fetchInfo() {
        try {
            LogStatusHolder.ignoredLog();
            return RedisBasicService.service.getInfo(context);
        } finally {
            LogStatusHolder.clear();
        }
    }

    public RedisUsageInfo.MemoryUsage memoryUsageInfo() { return getUsageInfo().getMemoryUsage(); }
    public RedisUsageInfo.NetworkStats calculateNetworkRate() { return getUsageInfo().getNetwork(); }
    protected double calculateCpuUsage() { return Double.parseDouble(getUsageInfo().getCpu().replace("%", "")); }
}
