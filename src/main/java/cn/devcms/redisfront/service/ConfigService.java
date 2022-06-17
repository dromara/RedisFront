package cn.devcms.redisfront.service;

import cn.devcms.redisfront.model.ConnectInfo;

import java.io.Serializable;
import java.util.List;

/**
 * ConnectService
 *
 * @author Jin
 */
public interface ConfigService {


    List<ConnectInfo> getConnectList(String name);

    ConnectInfo getConnectDetail(Serializable id);

    Boolean save(ConnectInfo connectInfo);

    Boolean update(ConnectInfo connectInfo);

}
