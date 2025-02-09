package org.dromara.redisfront.model.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.model.context.RedisConnectContext;

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
    private String setting;
    private String sslConfig;
    private String sshConfig;

    public RedisConnectContext getConnectContext() {
        RedisConnectContext redisConnectContext = new RedisConnectContext();
        redisConnectContext.setId(id);
        redisConnectContext.setTitle(name);
        redisConnectContext.setHost(host);
        redisConnectContext.setPort(port);
        redisConnectContext.setUsername(username);
        redisConnectContext.setPassword(password);
        redisConnectContext.setConnectTypeMode(ConnectType.of(connectMode));
        if (StrUtil.isNotEmpty(setting)) {
            RedisConnectContext.SettingInfo settingInfo = JSONUtil.toBean(setting, RedisConnectContext.SettingInfo.class);
            redisConnectContext.setSetting(settingInfo);
        }
        if (StrUtil.isNotEmpty(sslConfig)) {
            RedisConnectContext.SslInfo sslInfo = JSONUtil.toBean(sslConfig, RedisConnectContext.SslInfo.class);
            redisConnectContext.setSslInfo(sslInfo);
        }
        if (StrUtil.isNotEmpty(sshConfig)) {
            RedisConnectContext.SshInfo sshInfo = JSONUtil.toBean(sshConfig, RedisConnectContext.SshInfo.class);
            redisConnectContext.setSshInfo(sshInfo);
        }
        return redisConnectContext;
    }
}
