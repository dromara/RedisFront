package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.common.base.AbstractTerminalComponent;
import cn.devcms.redisfront.common.enums.ConnectEnum;
import cn.devcms.redisfront.common.util.RedisUtil;
import cn.devcms.redisfront.model.ConnectInfo;

import java.util.List;

public class TerminalComponent extends AbstractTerminalComponent {
    private final String database;

    public TerminalComponent(String host, String port, String database) {
        this.database = database;
        super.printConnectedSuccessMessage();
    }

    public TerminalComponent() {
        this.database = "0";
        super.printConnectedSuccessMessage();
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
        return database;
    }

}

