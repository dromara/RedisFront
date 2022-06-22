package com.redisfront.ui.component;

import cn.hutool.core.date.DateUtil;
import com.redisfront.constant.ConnectEnum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisService;
import com.redisfront.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

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
            Object s = RedisUtil.sendCommand(connectInfo(), false, input);
            String formatStr = format(s, "");
            print(formatStr);
        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    private String format(Object s, String space) {
        StringBuilder sb = new StringBuilder();
        if (s instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof List itemList) {
                    sb.append(space).append(i + 1).append(" ) ").append("\n").append(format(itemList, "  ")).append("\n");
                } else {
                    sb.append(space).append(i + 1).append(" ) ").append(space).append(item).append("\n");
                }
            }
        } else {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    protected ConnectInfo connectInfo() {
        return new ConnectInfo("a", "127.0.0.1", 6379, null, null, 11, false, ConnectEnum.NORMAL);
    }

    @Override
    protected String databaseName() {
        return String.valueOf(connectInfo.database());
    }

}

