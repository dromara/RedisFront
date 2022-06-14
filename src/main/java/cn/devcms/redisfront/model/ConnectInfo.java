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
                          SSLConfig sslConfig,
                          SSHConfig sshConfig

) {

    public ConnectInfo(String title, String host, Integer port, String password, ConnectEnum connectMode) {
        this(title, host, port, password, connectMode, null, null);
    }

    public ConnectInfo(String title, String host, Integer port, String password, ConnectEnum connectMode, SSHConfig sshConfig) {
        this(title, host, port, password, connectMode,  null, sshConfig);
    }

    public ConnectInfo(String title, String host, Integer port, String password, ConnectEnum connectMode, SSLConfig sslConfig) {
        this(title, host, port, password, connectMode,sslConfig, null);
    }

    public record SSHConfig(String privateKeyPath, String user, Integer port, String password) {

    }


    public record SSLConfig(String privateKeyFilePath, String publicKeyFilePath, String grantFilePath,
                            Boolean enableStrictMode) {

    }

}
