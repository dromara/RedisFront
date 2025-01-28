package org.dromara.redisfront.model.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.model.context.ConnectContext;

@Data
public class ConnectDetailEntity {
    private Integer id;
    private String name;
    private Integer groupId = -1;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer enableSsl;
    private String connectMode;
    private String sslConfig;
    private String sshConfig;

    public ConnectContext getConnectContext() {
        ConnectContext connectContext = new ConnectContext();
        connectContext.setId(id);
        connectContext.setTitle(name);
        connectContext.setHost(host);
        connectContext.setPort(port);
        connectContext.setUsername(username);
        connectContext.setPassword(password);
        connectContext.setConnectTypeMode(ConnectType.of(connectMode));
        if (StrUtil.isNotEmpty(sslConfig)) {
            JSONUtil.toBean(sslConfig, ConnectContext.SslInfo.class);
        }
        if (StrUtil.isNotEmpty(sshConfig)) {
            JSONUtil.toBean(sshConfig, ConnectContext.SshInfo.class);
        }
        return connectContext;
    }
}
