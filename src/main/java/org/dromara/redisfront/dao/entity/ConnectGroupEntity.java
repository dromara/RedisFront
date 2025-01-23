package org.dromara.redisfront.dao.entity;

import lombok.Data;

@Data
public class ConnectGroupEntity {
    private String groupId;
    private String groupName;
    private Integer enableSsh;
    private String sshConfig;
}
