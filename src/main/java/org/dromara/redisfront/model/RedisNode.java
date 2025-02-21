package org.dromara.redisfront.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.dromara.redisfront.commons.enums.RedisNodeRole;

@Data
@AllArgsConstructor
public class RedisNode {
    private String uri;
    private Integer port;
    private RedisNodeRole role;
}
