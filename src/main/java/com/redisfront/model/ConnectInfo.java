package com.redisfront.model;

import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;

import java.io.Serializable;

/**
 * ConnectInfo
 *
 * @author Jin
 */
public class ConnectInfo implements Serializable,Cloneable {

    private int id;
    private String title;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private Integer database;
    private Boolean ssl;
    private Enum.Connect connectMode;
    private Enum.RedisMode redisMode;
    private SSLConfig sslConfig;
    private SSHConfig sshConfig;

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, Enum.Connect connectMode) {
        this(title, host, port, username, password, database, ssl, connectMode, null, null);
    }

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, Enum.Connect connectMode, SSHConfig sshConfig) {
        this(title, host, port, username, password, database, ssl, connectMode, null, sshConfig);
    }

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, Enum.Connect connectMode, SSLConfig sslConfig) {
        this(title, host, port, username, password, database, ssl, connectMode, sslConfig, null);
    }

    @Override
    public ConnectInfo clone() {
        try {
            return (ConnectInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class SSHConfig implements Serializable {
        private String privateKeyPath;
        private String user;
        private String host;
        private Integer port;
        private String password;

        public SSHConfig(String privateKeyPath, String user, String host, Integer port, String password) {
            this.privateKeyPath = privateKeyPath;
            this.user = user;
            this.host = host;
            this.port = port;
            this.password = password;
        }

        public String host() {
            return host;
        }

        public SSHConfig setHost(String host) {
            this.host = host;
            return this;
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

    public ConnectInfo(String title, String host, Integer port, String username, String password, Integer database, Boolean ssl, Enum.Connect connectMode, SSLConfig sslConfig, SSHConfig sshConfig) {
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

    public static class SSLConfig implements Serializable {
        private String privateKeyFilePath;
        private String publicKeyFilePath;
        private String grantFilePath;
        private String password;

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
        return Fn.isEmpty(username) ? null : Fn.equal(username.toLowerCase(), "null") ? "" : username;
    }

    public ConnectInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return Fn.isEmpty(password) ? null : Fn.equal(password.toLowerCase(), "null") ? "" : password;
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

    public Enum.Connect connectMode() {
        return connectMode;
    }

    public ConnectInfo setConnectMode(Enum.Connect connectMode) {
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

    public Enum.RedisMode redisModeEnum() {
        return redisMode;
    }

    public ConnectInfo setRedisModeEnum(Enum.RedisMode redisMode) {
        this.redisMode = redisMode;
        return this;
    }


    @Override
    public String toString() {
        return "ConnectInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", database=" + database +
                ", ssl=" + ssl +
                ", connectMode=" + connectMode +
                ", redisModeEnum=" + redisMode +
                ", sslConfig=" + sslConfig +
                ", sshConfig=" + sshConfig +
                '}';
    }
}
