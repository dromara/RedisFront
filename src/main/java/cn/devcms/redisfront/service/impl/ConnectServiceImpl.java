package cn.devcms.redisfront.service.impl;

import cn.devcms.redisfront.common.enums.ConnectEnum;
import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.common.util.DerbyUtil;
import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.service.ConnectService;
import cn.hutool.core.io.resource.ResourceUtil;

import java.io.Serializable;
import java.util.List;

/**
 * ConnectServiceImpl
 *
 * @author Jin
 */
public class ConnectServiceImpl implements ConnectService {

    @Override
    public List<ConnectInfo> getConnectListByName(String name) {
        String sql = "select * form rf_connect where name like '%".concat(name).concat("%'");
        return DerbyUtil.newInstance().querySql(sql, ConnectInfo.class);
    }

    @Override
    public List<ConnectInfo> getAllConnectList() {
        String sql = "select * from rf_connect";
        return DerbyUtil.newInstance().querySql(sql, ConnectInfo.class);
    }

    @Override
    public ConnectInfo getConnectDetail(Serializable id) {
        String sql = "select * from rf_connect where id =".concat(id.toString());
        return DerbyUtil.newInstance().querySql(sql, ConnectInfo.class).stream().findAny().orElse(null);
    }

    @Override
    public Boolean update(ConnectInfo connectInfo) {
        String sql = "update rf_connect" +
                "set " +
                "title ='" +
                connectInfo.title() +
                "'," +
                "host ='" +
                connectInfo.host() +
                "'," +
                "port =" +
                connectInfo.port() +
                "," +
                "username ='" +
                connectInfo.user() +
                "'," +
                "password ='" +
                connectInfo.password() +
                "'," +
                "ssl ='" +
                connectInfo.ssl() +
                "'," +
                "connectMode ='" +
                connectInfo.connectMode().name() +
                "'," +
                "sslConfig ='" +
                (Fn.isNull(connectInfo.sshConfig()) ? "" : connectInfo.sshConfig()) +
                "'," +
                "sshConfig ='" +
                (Fn.isNull(connectInfo.sshConfig()) ? "" : connectInfo.sshConfig()) +
                "' where id =" +
                connectInfo.id();

        return DerbyUtil.newInstance().exec(sql);
    }


    @Override
    public Boolean save(ConnectInfo connectInfo) {
        String sql = "insert into rf_connect" +
                "(" +
                "title, " +
                "host, " +
                "port, " +
                "username, " +
                "password, " +
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
                connectInfo.user() +
                "','" +
                connectInfo.password() +
                "','" +
                connectInfo.ssl() +
                "','" +
                connectInfo.connectMode().name() +
                "','" +
                (Fn.isNull(connectInfo.sshConfig()) ? "" : connectInfo.sshConfig()) +
                "','" +
                (Fn.isNull(connectInfo.sshConfig()) ? "" : connectInfo.sshConfig()) +
                "')";
        return DerbyUtil.newInstance().exec(sql);
    }

    @Override
    public Boolean initDatabase() {
        String sqlData = ResourceUtil.readUtf8Str("sql/redisfront.sql");
        return DerbyUtil.newInstance().exec(sqlData);
    }

    public static void main(String[] args) {
        service.save(new ConnectInfo("1111111", "DSADSADSA", 222, "DSADSAD", "DSADSADSAD", 0, true, ConnectEnum.NORMAL));
        var v = service.getAllConnectList();
        System.out.println(v);

    }

}
