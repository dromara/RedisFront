package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.common.base.AbstractTerminalComponent;
import cn.devcms.redisfront.common.enums.ConnectEnum;
import cn.devcms.redisfront.common.util.TelnetUtil;
import cn.devcms.redisfront.model.ConnectInfo;

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
        String s = TelnetUtil.exec(connectInfo(), input);
        print(s);
    }

    @Override
    protected ConnectInfo connectInfo() {
        return new ConnectInfo("a", "127.0.0.1", 63378, "", ConnectEnum.NORMAL, true);
    }

    @Override
    protected String databaseName() {
        return database;
    }

}

