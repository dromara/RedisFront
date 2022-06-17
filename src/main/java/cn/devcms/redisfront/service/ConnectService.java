package cn.devcms.redisfront.service;

import cn.devcms.redisfront.model.ConnectInfo;
import cn.devcms.redisfront.service.impl.ConnectServiceImpl;

import java.io.Serializable;
import java.util.List;

/**
 * ConnectService
 *
 * @author Jin
 */
public interface ConnectService {

    ConnectService service = new ConnectServiceImpl();

    List<ConnectInfo> getConnectListByName(String name);

    List<ConnectInfo> getAllConnectList();

    ConnectInfo getConnectDetail(Serializable id);

    Boolean save(ConnectInfo connectInfo);

    Boolean update(ConnectInfo connectInfo);

    Boolean initDatabase();

}
