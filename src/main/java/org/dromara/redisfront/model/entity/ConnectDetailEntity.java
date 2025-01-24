package org.dromara.redisfront.model.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.model.ConnectInfo;

@Data
public class ConnectDetailEntity {
    private Integer id;
    private String name;
    private Integer groupId;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer enableSsl;
    private String connectMode;
    private String sslConfig;
    private String sshConfig;

    public ConnectInfo toConnectInfo() {
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setId(id);
        connectInfo.setTitle(name);
        connectInfo.setHost(host);
        connectInfo.setPort(port);
        connectInfo.setUsername(username);
        connectInfo.setPassword(password);
        connectInfo.setConnectTypeMode(Enums.ConnectType.of(connectMode));
        if (StrUtil.isNotEmpty(sslConfig)) {
            JSONUtil.toBean(sslConfig, ConnectInfo.SslInfo.class);
        }
        if (StrUtil.isNotEmpty(sshConfig)) {
            JSONUtil.toBean(sshConfig, ConnectInfo.SshInfo.class);
        }
        return connectInfo;
    }
}
