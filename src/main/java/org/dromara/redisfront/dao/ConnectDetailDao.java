package org.dromara.redisfront.dao;

import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.model.ConnectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * ConnectService
 *
 * @author Jin
 */
public class ConnectDetailDao {

    public static final String SQL_TABLE_CONNECT_DETAIL = "connect_detail";


    public static ConnectDetailDao DAO = new ConnectDetailDao();

    public List<ConnectInfo> getAllConnectList() {
        return null;
    }

    public ConnectInfo getConnect(Object id) {
        return null;
    }

    public void save(ConnectInfo connectInfo) {

    }

    public void update(ConnectInfo connectInfo) {

    }

    public void delete(Object id) {

    }

    private ConnectInfo mapToConnectInfo(Map<String, Object> map) {
        var sslConfigStr = (String) map.get("ssl_config");
        var sshConfigStr = (String) map.get("ssh_config");
        var sslConfig = Fn.isEmpty(sslConfigStr) ? null : Fn.fromJson(sslConfigStr, ConnectInfo.SSLConfig.class);
        var sshConfig = Fn.isEmpty(sshConfigStr) ? null : Fn.fromJson(sshConfigStr, ConnectInfo.SSHConfig.class);
        return new ConnectInfo()
                .setId((Integer) map.get("id"))
                .setTitle((String) map.get("title"))
                .setHost((String) map.get("host"))
                .setPort((Integer) map.get("port"))
                .setUsername((String) map.get("username"))
                .setPassword((String) map.get("password"))
                .setDatabase((Integer) map.get("database"))
                .setSsl(Boolean.valueOf((String) map.get("ssl")))
                .setConnectMode(Enums.Connect.valueOf((String) map.get("connect_mode")))
                .setSshConfig(sshConfig)
                .setSslConfig(sslConfig)
                ;
    }

}
