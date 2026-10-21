package org.dromara.redisfront.model.context;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dromara.redisfront.commons.enums.ConnectType;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.commons.utils.MD5Util;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

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
    private RedisMode redisMode = RedisMode.STANDALONE;
    private SettingInfo setting;
    private SslInfo sslInfo;
    private SshInfo sshInfo;
    private Map<String, Integer> clusterLocalPort;
    // Original Sentinel endpoint and credentials, retained after redirecting to its master.
    private String sentinelHost;
    private Integer sentinelPort;
    private String sentinelMasterName;
    private String sentinelUsername;
    private String sentinelPassword;

    /**
     * 每次“打开连接”的唯一标识。clone() 是浅拷贝，会原样带上该值，
     * 因此同一连接下的主 Tab 与命令终端 Tab 归属同一组连接池，关闭时能一起回收。
     */
    private String uid = UUID.randomUUID().toString();

    /**
     * 连接池与 RedisClient 缓存 key 的前缀，同一连接下保持不变。
     * <p>
     * key() 的后半段是连接参数的快照哈希，而 host/port/database/password 在连接生命周期内
     * 都可能被改写（哨兵定位 master、切换数据库、终端 SELECT、重新输入密码），
     * 每一次改写都会产生一个新 key 并把旧池遗留在缓存里，所以回收必须按前缀整组清理。
     */
    public String keyPrefix() {
        if (uid == null) {
            // 反序列化（Gson / Hutool toBean）不会执行字段初始化器，这里兜底
            uid = UUID.randomUUID().toString();
        }
        return uid + ":";
    }

    public String key() {
        return keyPrefix() + MD5Util.encrypt(id + ":" + host + ":" + localHost + ":" + port + ":" + localPort + ":" + username + ":"
                + password + ":" + sslInfo + ":" + sshInfo + ":" + database);
    }

    /**
     * 集群本地端口映射的 key 格式：host:port。
     * <p>
     * JschManager 生成映射、AddressMappingResolver 查映射、RedisInfoView 展示映射，
     * 三处必须走同一个构造方法，否则会出现查不到映射的空指针，或转发到错误节点。
     */
    public static String clusterAddress(String host, int port) {
        return host + ":" + port;
    }

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
        private Integer defaultDatabase;

        public SettingInfo(Integer loadKeyNum, String keySeparator, Integer redisTimeout, Integer sshTimeout) {
            this(loadKeyNum, keySeparator, redisTimeout, sshTimeout, 0);
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
