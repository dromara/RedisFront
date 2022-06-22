package com.redisfront.ui.component;

import com.redisfront.constant.ConnectEnum;
import com.redisfront.model.ConnectInfo;
import com.redisfront.ui.base.AbstractTerminalComponent;
import com.redisfront.util.RedisUtil;

import java.util.List;

public class TerminalComponent extends AbstractTerminalComponent {
    private ConnectInfo connectInfo;
    private static TerminalComponent terminalComponent;

    public static TerminalComponent getInstance() {
        if (terminalComponent == null) {
            terminalComponent = new TerminalComponent();
        }
        return terminalComponent;
    }

    public TerminalComponent init(ConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
        super.printConnectedSuccessMessage();
        return this;
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

