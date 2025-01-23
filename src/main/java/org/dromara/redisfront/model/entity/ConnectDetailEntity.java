package org.dromara.redisfront.model.entity;

import lombok.Data;

@Data
public class ConnectDetailEntity {
    private Integer id;
    private String name;
    private Integer groupId;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String connectMode;
    private String sslConfig;
    private String sshConfig;
}
