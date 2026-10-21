package org.dromara.redisfront.ui.components.monitor;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RedisMetricSamplerTest {
    private Map<String,Object> info(String run, long bytes, long commands, double cpu) {
        var values=new HashMap<String,Object>();
        values.put("run_id",run);values.put("total_net_input_bytes",bytes);
        values.put("total_net_output_bytes",bytes*2);values.put("total_commands_processed",commands);
        values.put("used_cpu_user",cpu);values.put("used_cpu_sys",cpu);
        return values;
    }

    @Test void consecutiveFractionalSamplesUseTheirOwnIntervals() {
        var sampler=new RedisMetricSampler();
        assertEquals(0,sampler.sample(info("a",1000,100,1),1_000_000_000L,0).inputRate());
        var second=sampler.sample(info("a",3000,150,1.1),1_500_000_000L,500);
        var third=sampler.sample(info("a",5000,200,1.2),2_000_000_000L,1000);
        assertEquals(4000,second.inputRate());assertEquals(4000,third.inputRate());
        assertEquals(8000,third.outputRate());assertEquals(100,third.qps());
        assertEquals(40,third.cpuPercent(),0.00001);
    }

    @Test void restartAndCounterResetDoNotProduceSpikesOrNegativeRates() {
        var sampler=new RedisMetricSampler();
        sampler.sample(info("a",1000,100,1),1_000_000_000L,0);
        var restarted=sampler.sample(info("b",9000,900,9),2_000_000_000L,1000);
        assertEquals(0,restarted.inputRate());assertEquals(0,restarted.qps());
        var reset=sampler.sample(info("b",100,10,0.1),3_000_000_000L,2000);
        assertEquals(0,reset.inputRate());assertEquals(0,reset.cpuPercent());
        assertEquals(100,sampler.sample(info("b",200,20,0.2),4_000_000_000L,3000).inputRate());
    }

    @Test void memoryStaysInBytesAndUsesTheServerFragmentationRatio() {
        var values=info("a",0,0,0);
        values.put("used_memory","2097152");values.put("used_memory_rss","6291456");
        values.put("mem_fragmentation_ratio","2.75");values.put("maxmemory","67108864");
        var sample=new RedisMetricSampler().sample(values,1,0);
        assertEquals(2097152,sample.usedMemory());assertEquals(6291456,sample.rssMemory());
        assertEquals(2.75,sample.fragmentationRatio());assertEquals(67108864,sample.maxMemory());
    }

    @Test void missingCountersAndNonPositiveIntervalsAreSafe() {
        var sampler=new RedisMetricSampler();
        sampler.sample(Map.of(),100,0);
        assertEquals(0,sampler.sample(Map.of(),100,0).qps());
        assertEquals(0,sampler.sample(Map.of("used_memory","unavailable"),99,0).usedMemory());
    }
}
