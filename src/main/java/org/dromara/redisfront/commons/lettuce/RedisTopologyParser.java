package org.dromara.redisfront.commons.lettuce;

import org.dromara.redisfront.model.ClusterNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RedisTopologyParser {
    private RedisTopologyParser() { }

    public static List<ClusterNode> parseClusterNodes(String text) {
        var nodes = new ArrayList<ClusterNode>();
        for (String line : text.split("\\R")) {
            String[] fields = line.trim().split("\\s+");
            if (fields.length < 8 || line.startsWith("#")) continue;
            String endpoint = fields[1].split(",", 2)[0].split("@", 2)[0];
            int colon = endpoint.lastIndexOf(':');
            if (colon <= 0) continue;
            try {
                String host = endpoint.substring(0, colon);
                if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length() - 1);
                nodes.add(new ClusterNode().setId(fields[0]).setIpAndPort(fields[1])
                        .setFlags(fields[2]).setMaster(fields[3]).setPing(fields[4]).setPong(fields[5])
                        .setEpoch(fields[6]).setState(fields[7]).setHost(host)
                        .setPort(Integer.parseInt(endpoint.substring(colon + 1)))
                        .setSlot(fields.length > 8 ? String.join(" ", Arrays.copyOfRange(fields, 8, fields.length)) : "-"));
            } catch (NumberFormatException ignored) {
                // A malformed address must not hide the other nodes.
            }
        }
        return nodes;
    }
}
