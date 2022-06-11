package cn.devcms.redisfront.model;

import cn.devcms.redisfront.common.enums.ConnectEnum;
import lombok.Data;

/**
 * ConnectInfo
 *
 * @author Jin
 */
@Data
public class ConnectInfo {
    private String title;
    private String host;
    private Integer port;
    private String password;
    private ConnectEnum connectMode;
    private Boolean active;

    @Data
    public static class SSLConfig {
        private String privateKeyFilePath;
        private String publicKeyFilePath;
        private String grantFilePath;
        private Boolean enableStrictMode;
    }

    @Data
    public static class SSHConfig {
        private String privateKeyPath;
        private String user;
        private Integer port;
        private String password;
    }

}
