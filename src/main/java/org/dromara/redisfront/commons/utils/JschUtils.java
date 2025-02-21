package org.dromara.redisfront.commons.utils;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.Session;
import org.dromara.redisfront.model.context.RedisConnectContext;

/**
 * JSchUtil
 *
 * @author Jin
 */
public class JschUtils {


    public static Session createSession(RedisConnectContext redisConnectContext) {
        RedisConnectContext.SshInfo sshInfo = redisConnectContext.getSshInfo();
        if (RedisFrontUtils.isNotEmpty(sshInfo.getPrivateKeyPath()) && RedisFrontUtils.isNotEmpty(sshInfo.getPassword())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), sshInfo.getPassword().getBytes());
        } else if (RedisFrontUtils.isNotEmpty(sshInfo.getPrivateKeyPath())) {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPrivateKeyPath(), null);
        } else {
            return JschUtil.createSession(sshInfo.getHost(), sshInfo.getPort(), sshInfo.getUser(), sshInfo.getPassword());
        }
    }

}
