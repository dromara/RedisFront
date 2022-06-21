package com.redisfront.model;

import com.redisfront.common.enums.ConnectEnum;
import com.redisfront.common.enums.RedisModeEnum;
import com.redisfront.common.func.Fn;

import java.io.Serializable;

/**
 * ConnectInfo
 *
 * @author Jin
 */
public class ConnectInfo implements Serializable {

    private int id;
    private String title;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer database;
    private Boolean ssl;
    private ConnectEnum connectMode;

    private RedisModeEnum redisModeEnum;
    private SSLConfig sslConfig;
    private SSHConfig sshConfig;

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, ConnectEnum connectMode) {
        this(title, host, port, username, password, database, ssl, connectMode, null, null);
    }

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, ConnectEnum connectMode, SSHConfig sshConfig) {
        this(title, host, port, username, password, database, ssl, connectMode, null, sshConfig);
    }

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, ConnectEnum connectMode, SSLConfig sslConfig) {
        this(title, host, port, username, password, database, ssl, connectMode, sslConfig, null);
    }

    public static class SSHConfig {
        String privateKeyPath;
        String user;
        Integer port;
        String password;

        public SSHConfig(String privateKeyPath, String user, Integer port, String password) {
            this.privateKeyPath = privateKeyPath;
            this.user = user;
            this.port = port;
            this.password = password;
        }

        public String privateKeyPath() {
            return privateKeyPath;
        }

        public SSHConfig setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
            return this;
        }

        public String user() {
            return user;
        }

        public SSHConfig setUser(String user) {
            this.user = user;
            return this;
        }

        public Integer port() {
            return port;
        }

        public SSHConfig setPort(Integer port) {
            this.port = port;
            return this;
        }

        public String password() {
            return password;
        }

        public SSHConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public String toString() {
            return Fn.toJson(this);
        }
    }

    public ConnectInfo() {
    }

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, ConnectEnum connectMode, SSLConfig sslConfig, SSHConfig sshConfig) {
        this.title = title;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
        this.ssl = ssl;
        this.connectMode = connectMode;
        this.sslConfig = sslConfig;
        this.sshConfig = sshConfig;
    }

    public static class SSLConfig {
        String privateKeyFilePath;
        String publicKeyFilePath;
        String grantFilePath;
        String password;

        public SSLConfig(String privateKeyFilePath, String publicKeyFilePath, String grantFilePath, String password) {
            this.privateKeyFilePath = privateKeyFilePath;
            this.publicKeyFilePath = publicKeyFilePath;
            this.grantFilePath = grantFilePath;
            this.password = password;
        }

        public String privateKeyFilePath() {
            return privateKeyFilePath;
        }

        public SSLConfig setPrivateKeyFilePath(String privateKeyFilePath) {
            this.privateKeyFilePath = privateKeyFilePath;
            return this;
        }

        public String publicKeyFilePath() {
            return publicKeyFilePath;
        }

        public SSLConfig setPublicKeyFilePath(String publicKeyFilePath) {
            this.publicKeyFilePath = publicKeyFilePath;
            return this;
        }

        public String grantFilePath() {
            return grantFilePath;
        }

        public SSLConfig setGrantFilePath(String grantFilePath) {
            this.grantFilePath = grantFilePath;
            return this;
        }

        public String password() {
            return password;
        }

        public SSLConfig setPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public String toString() {
            return Fn.toJson(this);
        }
    }

    public String title() {
        return title;
    }

    public ConnectInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String host() {
        return host;
    }

    public ConnectInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer port() {
        return port;
    }

    public ConnectInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String user() {
        return Fn.isEmpty(username) ? null : username;
    }

    public ConnectInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return Fn.isEmpty(password) ? null : password;
    }

    public ConnectInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer database() {
        return Fn.isNull(database) ? 0 : database;
    }

    public ConnectInfo setDatabase(Integer database) {
        this.database = database;
        return this;
    }

    public Boolean ssl() {
        return ssl;
    }

    public ConnectInfo setSsl(Boolean ssl) {
        this.ssl = ssl;
        return this;
    }

    public ConnectEnum connectMode() {
        return connectMode;
    }

    public ConnectInfo setConnectMode(ConnectEnum connectMode) {
        this.connectMode = connectMode;
        return this;
    }

    public SSLConfig sslConfig() {
        return sslConfig;
    }

    public ConnectInfo setSslConfig(SSLConfig sslConfig) {
        this.sslConfig = sslConfig;
        return this;
    }

    public SSHConfig sshConfig() {
        return sshConfig;
    }

    public ConnectInfo setSshConfig(SSHConfig sshConfig) {
        this.sshConfig = sshConfig;
        return this;
    }

    public int id() {
        return id;
    }

    public ConnectInfo setId(int id) {
        this.id = id;
        return this;
    }

    public String username() {
        return username;
    }

    public RedisModeEnum redisModeEnum() {
        return redisModeEnum;
    }

    public ConnectInfo setRedisModeEnum(RedisModeEnum redisModeEnum) {
        this.redisModeEnum = redisModeEnum;
        return this;
    }
}
