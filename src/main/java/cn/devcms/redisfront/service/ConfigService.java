package cn.devcms.redisfront.service;

import cn.devcms.redisfront.model.ConfigInfo;

/**
 * ConnectService
 *
 * @author Jin
 */
public interface ConfigService {

    ConfigInfo loadConfig();

    void saveConfig(ConfigInfo configInfo);

}
