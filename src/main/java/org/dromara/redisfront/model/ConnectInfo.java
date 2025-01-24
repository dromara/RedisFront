package org.dromara.redisfront.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
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
public class ConnectInfo implements Serializable, Cloneable {
    private int id;
    private String title;
    private String host = "127.0.0.1";
    private Integer port = 6379;
    private String localHost;
    private Integer localPort;
    private Map<Integer, Integer> clusterLocalPort;
    private String username;
    private String password;
    private Integer database;
    private Boolean enableSsl = false;
    private Enums.ConnectType connectTypeMode;
    private Enums.RedisMode redisMode;
    private SslInfo sslInfo;
    private SshInfo sshInfo;

    public ConnectDetailEntity toEntity() {
        ConnectDetailEntity entity = new ConnectDetailEntity();
        entity.setId(id);
        entity.setName(title);
        entity.setHost(host);
        entity.setPort(port);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setEnableSsl(enableSsl ? 0 : 1);

        return entity;
    }

    @Override
    public ConnectInfo clone() {
        try {
            return (ConnectInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
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
            return Fn.toJson(this);
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
            return Fn.toJson(this);
        }
    }

}
