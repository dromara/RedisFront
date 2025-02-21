package org.dromara.redisfront.model.context;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;

import java.io.Serializable;
import java.util.Map;

/**
 * ConnectInfo
 *
 * @author Jin
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedisConnectContext implements Serializable, Cloneable {

    private int id;
    private String title;
    private String host = "127.0.0.1";
    private Integer port = 6379;
    private String localHost;
    private Integer localPort;
    private String username;
    private String password;
    private Integer database = 0;
    private Boolean enableSsl = false;
    private ConnectType connectTypeMode;
    private RedisMode redisMode;
    private SettingInfo setting;
    private SslInfo sslInfo;
    private SshInfo sshInfo;
    private Map<Integer, Integer> clusterLocalPort;

    public ConnectDetailEntity toEntity() {
        ConnectDetailEntity entity = new ConnectDetailEntity();
        entity.setName(title);
        entity.setHost(host);
        entity.setPort(port);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setEnableSsl(enableSsl ? 1 : 0);
        entity.setConnectMode(connectTypeMode.name());
        entity.setSetting(JSONUtil.toJsonStr(setting));
        entity.setSshConfig(JSONUtil.toJsonStr(sshInfo));
        entity.setSslConfig(JSONUtil.toJsonStr(sslInfo));
        return entity;
    }

    @Override
    public RedisConnectContext clone() {
        try {
            return (RedisConnectContext) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SettingInfo implements Serializable {
        private Integer loadKeyNum;
        private String keySeparator;
        private Integer redisTimeout;
        private Integer sshTimeout;
        private Boolean rewriteHost;

        public Boolean getRewriteHost() {
            return rewriteHost != null && rewriteHost;
        }

        @Override
        public String toString() {
            return RedisFrontUtils.toJson(this);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SshInfo implements Serializable {
        private String privateKeyPath;
        private String user;
        private String host;
        private Integer port;
        private String password;

        @Override
        public String toString() {
            return RedisFrontUtils.toJson(this);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class SslInfo implements Serializable {
        private String privateKeyFilePath;
        private String publicKeyFilePath;
        private String grantFilePath;
        private String password;

        @Override
        public String toString() {
            return RedisFrontUtils.toJson(this);
        }
    }

}
