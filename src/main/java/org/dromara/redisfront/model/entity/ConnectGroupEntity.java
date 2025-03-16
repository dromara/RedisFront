package org.dromara.redisfront.model.entity;

import lombok.Data;

@Data
public class ConnectGroupEntity {
    private Integer groupId;
    private String groupName;
    private Integer enableSsh;
    private String sshConfig;
}
