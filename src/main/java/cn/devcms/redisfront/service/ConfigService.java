package cn.devcms.redisfront.service;

import cn.devcms.redisfront.model.ConfigInfo;

import java.io.File;

/**
 * ConnectService
 *
 * @author Jin
 */
public interface ConfigService {

    ConfigInfo loadConfig();

    void persistenceConfig(ConfigInfo configInfo);

    ConfigInfo importConfig(String filePath);

    void exportConfig(ConfigInfo configInfo, File file);

}
