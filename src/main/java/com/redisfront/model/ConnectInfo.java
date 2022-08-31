package com.redisfront.model;

import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;

import java.io.Serializable;
import java.util.Map;

/**
 * ConnectInfo
 *
 * @author Jin
 */
public class ConnectInfo implements Serializable, Cloneable {

    private int id;
    private String title;
    private String host;
    private Integer port;
    private String localHost;
    private Integer localPort;
    private Map<Integer, Integer> clusterLocalPort;
    private String username;
    private String password;
    private Integer database;
    private Boolean ssl;
    private Enum.Connect connectMode;
    private Enum.RedisMode redisMode;
    private SSLConfig sslConfig;
    private SSHConfig sshConfig;

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public Map<Integer, Integer> getClusterLocalPort() {
        return clusterLocalPort;
    }

    public void setClusterLocalPort(Map<Integer, Integer> clusterLocalPort) {
        this.clusterLocalPort = clusterLocalPort;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

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

        public String getPrivateKeyPath() {
            return privateKeyPath;
        }

        public void setPrivateKeyPath(String privateKeyPath) {
            this.privateKeyPath = privateKeyPath;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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

        public String getPrivateKeyFilePath() {
            return privateKeyFilePath;
        }

        public void setPrivateKeyFilePath(String privateKeyFilePath) {
            this.privateKeyFilePath = privateKeyFilePath;
        }

        public String getPublicKeyFilePath() {
            return publicKeyFilePath;
        }

        public void setPublicKeyFilePath(String publicKeyFilePath) {
            this.publicKeyFilePath = publicKeyFilePath;
        }

        public String getGrantFilePath() {
            return grantFilePath;
        }

        public void setGrantFilePath(String grantFilePath) {
            this.grantFilePath = grantFilePath;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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
        return username;
    }

    public ConnectInfo setUsername(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return password;
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

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getDatabase() {
        return database;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public Enum.Connect getConnectMode() {
        return connectMode;
    }

    public Enum.RedisMode getRedisMode() {
        return redisMode;
    }

    public SSLConfig getSslConfig() {
        return sslConfig;
    }

    public SSHConfig getSshConfig() {
        return sshConfig;
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
