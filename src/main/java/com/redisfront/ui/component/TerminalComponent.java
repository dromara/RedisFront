package com.redisfront.ui.component;

import cn.hutool.core.date.DateUtil;
import com.redisfront.constant.Enum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
import com.redisfront.util.TelnetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class TerminalComponent extends AbstractTerminal {
    private static final Logger log = LoggerFactory.getLogger(TerminalComponent.class);
    private final ConnectInfo connectInfo;


    public static TerminalComponent newInstance(ConnectInfo connectInfo) {
        return new TerminalComponent(connectInfo);
    }

    public TerminalComponent(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        terminal.setEnabled(false);
    }

    public void ping() {
        try {
            if (RedisService.service.ping(connectInfo)) {
                if (!terminal.isEnabled()) {
                    terminal.setEnabled(true);
                    super.printConnectedSuccessMessage();
                }
            } else {
                println(DateUtil.formatDateTime(new Date()) + " - ".concat("redis PING faild!"));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            println(DateUtil.formatDateTime(new Date()) + " - ".concat(e.getMessage()));
        }
    }

    @Override
    protected void inputProcessHandler(String input) {
        try {
            String str = TelnetUtil.sendCommand(connectInfo(),  input);
            println(str);
        } catch (Exception e) {
            print(e.getMessage());
        }
    }



    @Override
    protected ConnectInfo connectInfo() {
        return connectInfo;
    }

    @Override
    protected String databaseName() {
        return String.valueOf(connectInfo.database());
    }

}

