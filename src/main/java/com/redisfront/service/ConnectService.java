package com.redisfront.service;

import cn.hutool.json.JSONUtil;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.func.Fn;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.ConnectServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * ConnectService
 *
 * @author Jin
 */
public interface ConnectService {

    Logger log = LoggerFactory.getLogger(ConnectService.class);


    ConnectService service = new ConnectServiceImpl();

    List<ConnectInfo> getConnectListByName(String name);

    List<ConnectInfo> getAllConnectList();

    ConnectInfo getConnect(Object id);

    void save(ConnectInfo connectInfo);

    void update(ConnectInfo connectInfo);

    void delete(Object id);

    void initDatabase();

    default String buildUpdateSql(ConnectInfo connectInfo) {
        return "update rf_connect" +
                " set " +
                "title ='" +
                connectInfo.title() +
                "'," +
                "host ='" +
                connectInfo.host() +
                "'," +
                "port =" +
                connectInfo.port() +
                "," +
                (Fn.isNull(connectInfo.user()) ? "" : "username ='" + connectInfo.user() + "',") +
                (Fn.isNull(connectInfo.password()) ? "" : "password ='" + connectInfo.password() + "',") +
                "ssl ='" +
                connectInfo.ssl() +
                "'," +
                "connect_mode ='" +
                connectInfo.connectMode().name() +
                "'," +
                "ssl_config ='" +
                (Fn.isNull(connectInfo.sslConfig()) ? "" : Fn.toJson(connectInfo.sslConfig())) +
                "'," +
                "ssh_config ='" +
                (Fn.isNull(connectInfo.sshConfig()) ? "" : Fn.toJson(connectInfo.sshConfig())) +
                "' where id =" +
                connectInfo.id();

    }

    default String buildInsertSql(ConnectInfo connectInfo) {
        return "insert into rf_connect" +
                "(" +
                "title, " +
                "host, " +
                "port, " +
                (Fn.isNotEmpty(connectInfo.user()) ? "username, " : "") +
                (Fn.isNotEmpty(connectInfo.password()) ? "password, " : "") +
                "ssl, " +
                "connect_mode, " +
                "ssl_config, " +
                "ssh_config" +
                ") " +
                "values('" +
                connectInfo.title() +
                "','" +
                connectInfo.host() +
                "'," +
                connectInfo.port() +
                ",'" +
                (Fn.isNotEmpty(connectInfo.user()) ? connectInfo.user() + "','" : "") +
                (Fn.isNotEmpty(connectInfo.password()) ? connectInfo.password() + "','" : "") +
                connectInfo.ssl() +
                "','" +
                connectInfo.connectMode().name() +
                "','" +
                (Fn.isNull(connectInfo.sslConfig()) ? "" : Fn.toJson(connectInfo.sslConfig())) +
                "','" +
                (Fn.isNull(connectInfo.sshConfig()) ? "" : Fn.toJson(connectInfo.sshConfig())) +
                "')";
    }

    default ConnectInfo mapToConnectInfo(Map<String, Object> map) {
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
                .setConnectMode(Enum.Connect.valueOf((String) map.get("connect_mode")))
                .setSshConfig(sshConfig)
                .setSslConfig(sslConfig)
                ;
    }

}
