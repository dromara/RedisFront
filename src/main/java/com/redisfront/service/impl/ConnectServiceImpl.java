package com.redisfront.service.impl;

import com.redisfront.commons.constant.Const;
import com.redisfront.commons.util.DerbyUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.ConnectService;
import cn.hutool.core.io.resource.ResourceUtil;
import com.redisfront.commons.util.PrefUtils;

import java.util.List;
import java.util.Map;

/**
 * ConnectServiceImpl
 *
 * @author Jin
 */
public class ConnectServiceImpl implements ConnectService {

    public ConnectServiceImpl() {
        if (PrefUtils.getState().getBoolean(Const.KEY_APP_DATABASE_INIT, true)) {
            this.initDatabase();
            PrefUtils.getState().put(Const.KEY_APP_DATABASE_INIT, Boolean.FALSE.toString());
        }
    }

    @Override
    public List<ConnectInfo> getConnectListByName(String name) {
        String sql = "select * form rf_connect where name like '%".concat(name).concat("%'");
        return DerbyUtils.getInstance().querySql(sql).stream().map(this::mapToConnectInfo).toList();
    }

    @Override
    public List<ConnectInfo> getAllConnectList() {
        String sql = "select * from rf_connect";
        List<Map<String, Object>> result = DerbyUtils.getInstance().querySql(sql);
        return result.stream().map(this::mapToConnectInfo).toList();
    }

    @Override
    public ConnectInfo getConnect(Object id) {
        var sql = "select * from rf_connect where id =".concat(id.toString());
        return DerbyUtils.getInstance().querySql(sql).stream().map(this::mapToConnectInfo).findAny().orElse(null);
    }

    @Override
    public void update(ConnectInfo connectInfo) {
        var sql = this.buildUpdateSql(connectInfo);
        DerbyUtils.getInstance().exec(sql);
    }

    @Override
    public void delete(Object id) {
        var sql = "delete from rf_connect where id =".concat(id.toString());
        DerbyUtils.getInstance().exec(sql);
    }


    @Override
    public void save(ConnectInfo connectInfo) {
        var sql = this.buildInsertSql(connectInfo);
        DerbyUtils.getInstance().exec(sql);
    }

    @Override
    public void initDatabase() {
        var sqlData = ResourceUtil.readUtf8Str("sql/redisfront.sql");
        DerbyUtils.getInstance().exec(sqlData);
    }

}
