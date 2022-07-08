package com.redisfront.service.impl;

import com.redisfront.constant.Const;
import com.redisfront.util.DerbyUtil;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.ConnectService;
import cn.hutool.core.io.resource.ResourceUtil;
import com.redisfront.util.PrefUtil;

import java.util.List;
import java.util.Map;

/**
 * ConnectServiceImpl
 *
 * @author Jin
 */
public class ConnectServiceImpl implements ConnectService {

    public ConnectServiceImpl() {
        if (PrefUtil.getState().getBoolean(Const.KEY_APP_DATABASE_INIT, true)) {
            this.initDatabase();
            PrefUtil.getState().put(Const.KEY_APP_DATABASE_INIT, Boolean.FALSE.toString());
        }
    }

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
    public void update(ConnectInfo connectInfo) {
        String sql = this.buildUpdateSql(connectInfo);
        DerbyUtil.getInstance().exec(sql);
    }

    @Override
    public void delete(Object id) {
        String sql = "delete from rf_connect where id =".concat(id.toString());
        DerbyUtil.getInstance().exec(sql);
    }


    @Override
    public void save(ConnectInfo connectInfo) {
        String sql = this.buildInsertSql(connectInfo);
        DerbyUtil.getInstance().exec(sql);
    }

    @Override
    public void initDatabase() {
        String sqlData = ResourceUtil.readUtf8Str("sql/redisfront.sql");
        DerbyUtil.getInstance().exec(sqlData);
    }

}
