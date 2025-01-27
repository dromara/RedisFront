package org.dromara.redisfront.service.impl;

import org.dromara.redisfront.commons.enums.Enums;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.dialog.LogsDialog;
import org.dromara.redisfront.Fn;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.service.RedisStringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisStringServiceImpl implements RedisStringService {

    private static final Logger log = LoggerFactory.getLogger(RedisStringServiceImpl.class);


    @Override
    public String set(ConnectContext connectContext, String key, String value) {


        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("SET ".concat(key).concat(" ").concat(value));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.set(key, value));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.set(key, value));
        }
    }

    @Override
    public String get(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("GET ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.get(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.get(key));
        }
    }

    @Override
    public Long strlen(ConnectContext connectContext, String key) {

        var logInfo = RedisBasicService.buildLogInfo(connectContext).setInfo("STRLEN ".concat(key));
        LogsDialog.appendLog(logInfo);

        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.strlen(key));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.strlen(key));
        }
    }

    @Override
    public String setex(ConnectContext connectContext, String key, long seconds, String value) {
        if (Fn.equal(connectContext.getRedisMode(), Enums.RedisMode.CLUSTER)) {
            return LettuceUtils.clusterExec(connectContext, commands -> commands.setex(key, seconds, value));
        } else {
            return LettuceUtils.exec(connectContext, commands -> commands.setex(key, seconds, value));
        }
    }


}
