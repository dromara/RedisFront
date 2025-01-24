package org.dromara.redisfront.dao;

import org.dromara.redisfront.commons.constant.Enums;
import org.dromara.redisfront.commons.func.Fn;
import org.dromara.redisfront.model.ConnectInfo;

import java.util.List;
import java.util.Map;

/**
 * ConnectService
 *
 * @author Jin
 */
public class ConnectDetailDao {

    public static final String TABLE_NAME = "connect_detail";


    public static ConnectDetailDao DAO = new ConnectDetailDao();



    public List<ConnectInfo> loadAll() {
        return null;
    }

    public List<ConnectInfo> loadByGroupId(Object id) {
        return null;
    }

    public ConnectInfo getById(Object id) {
        return null;
    }

    public void save(ConnectInfo connectInfo) {

    }

    public void update(ConnectInfo connectInfo) {

    }

    public void delete(Object id) {

    }

}
