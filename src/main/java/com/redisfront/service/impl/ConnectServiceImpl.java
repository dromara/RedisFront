package com.redisfront.service.impl;

import com.redisfront.util.DerbyUtil;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.ConnectService;
import cn.hutool.core.io.resource.ResourceUtil;

import java.util.List;
import java.util.Map;

/**
 * ConnectServiceImpl
 *
 * @author Jin
 */
public class ConnectServiceImpl implements ConnectService {

    @Override
    public List<ConnectInfo> getConnectListByName(String name) {
        String sql = "select * form rf_connect where name like '%".concat(name).concat("%'");
        return DerbyUtil.getInstance().querySql(sql).stream().map(this::mapToConnectInfo).toList();
    }

    @Override
    public List<ConnectInfo> getAllConnectList() {
        String sql = "select * from rf_connect";
        List<Map<String, Object>> result = DerbyUtil.getInstance().querySql(sql);
        return result.stream().map(this::mapToConnectInfo).toList();
    }

    @Override
    public ConnectInfo getConnect(Object id) {
        String sql = "select * from rf_connect where id =".concat(id.toString());
        return DerbyUtil.getInstance().querySql(sql).stream().map(this::mapToConnectInfo).findAny().orElse(null);
    }

    @Override
    public Boolean update(ConnectInfo connectInfo) {
        String sql = this.buildUpdateSql(connectInfo);
        return DerbyUtil.getInstance().exec(sql);
    }

    @Override
    public Boolean delete(Object id) {
        String sql = "delete from rf_connect where id =".concat(id.toString());
        return DerbyUtil.getInstance().exec(sql);
    }


    @Override
    public Boolean save(ConnectInfo connectInfo) {
        String sql = this.buildInsertSql(connectInfo);
        return DerbyUtil.getInstance().exec(sql);
    }

    @Override
    public Boolean initDatabase() {
        String sqlData = ResourceUtil.readUtf8Str("sql/redisfront.sql");
        return DerbyUtil.getInstance().exec(sqlData);
    }

}
