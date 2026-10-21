package org.dromara.redisfront.commons.lettuce;

import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.monitor.RedisTopologyMonitor;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RedisTopologyParserTest {
    @Test void normalClusterRowsAndMultipleSlotRangesAreRetained() {
        String text="m1 10.0.0.1:7000@17000 myself,master - 0 0 1 connected 0-100 200-300\n"
                +"r1 10.0.0.2:7001@17001 slave m1 0 0 1 connected\n";
        var nodes=RedisTopologyMonitor.clusterNodes(text);
        assertEquals(2,nodes.size());assertEquals("master",nodes.getFirst().role());
        assertEquals("0-100 200-300",nodes.getFirst().slots());
        assertEquals("10.0.0.1:7000",nodes.get(1).upstream());
    }

    @Test void ipv6HostnameMetadataAndFailuresAreParsed() {
        var nodes=RedisTopologyParser.parseClusterNodes("# comment\n"
                +"node [2001:db8::1]:7000@17000,redis.local master - 0 0 2 connected 0-16383\n"
                +"broken invalid master - 0 0 2 disconnected\n");
        assertEquals(1,nodes.size());assertEquals("2001:db8::1",nodes.getFirst().host());
        assertEquals(7000,nodes.getFirst().port());
        var failed=RedisTopologyMonitor.clusterNodes("node 10.0.0.1:7000@17000 master,fail - 0 0 1 disconnected 0-16383");
        assertTrue(failed.getFirst().state().contains("fail"));
    }

    @Test void replicationInfoBuildsMasterAndReplicaAddresses() {
        var nodes=RedisTopologyMonitor.replicationNodes("172.21.0.14",6380,Map.of(
                "role","master","slave0","ip=172.21.0.14,port=6381,state=online,offset=10,lag=0",
                "slave1","ip=172.21.0.14,port=6382,state=online,offset=10,lag=0"));
        assertEquals(3,nodes.size());assertEquals("master",nodes.getFirst().role());
        assertTrue(nodes.stream().skip(1).allMatch(n -> n.upstream().equals("172.21.0.14:6380")));
    }

    @Test void infoValuesContainingColonsAreNotTruncated() {
        var info=RedisBasicService.service.strToMap("# Replication\r\nmaster_host:2001:db8::1\r\nrole:slave\r\n");
        assertEquals("2001:db8::1",info.get("master_host"));
    }
}
