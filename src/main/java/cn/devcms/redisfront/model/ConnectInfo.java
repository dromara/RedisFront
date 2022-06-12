package cn.devcms.redisfront.model;

import cn.devcms.redisfront.common.enums.ConnectEnum;

/**
 * ConnectInfo
 *
 * @author Jin
 */
public record ConnectInfo(String title,
                          String host,
                          Integer port,
                          String password,
                          ConnectEnum connectMode,
                          Boolean active,
                          SSLConfig sslConfig,
                          SSHConfig sshConfig

) {

    public ConnectInfo(String title, String host, Integer port, String password, ConnectEnum connectMode, Boolean active) {
        this(title, host, port, password, connectMode, active, null, null);
    }

    public ConnectInfo(String title, String host, Integer port, String password, ConnectEnum connectMode, Boolean active, SSHConfig sshConfig) {
        this(title, host, port, password, connectMode, active, null, sshConfig);
    }

    public ConnectInfo(String title, String host, Integer port, String password, ConnectEnum connectMode, Boolean active, SSLConfig sslConfig) {
        this(title, host, port, password, connectMode, active, sslConfig, null);
    }

    public record SSHConfig(String privateKeyPath, String user, Integer port, String password) {

    }


    public record SSLConfig(String privateKeyFilePath, String publicKeyFilePath, String grantFilePath,
                            Boolean enableStrictMode) {

    }

}
