package cn.devcms.redisfront.ui.component;

import cn.devcms.redisfront.common.base.AbstractTerminalComponent;

public class TerminalComponent extends AbstractTerminalComponent {
    private final String host;
    private final String port;

    private final String database;

    public TerminalComponent(String host, String port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
        super.printConnectedSuccessMessage();
    }

    public TerminalComponent() {
        this.host = "127.0.0.1";
        this.port = "6379";
        this.database = "0";
        super.printConnectedSuccessMessage();
    }

    @Override
    protected void inputProcessHandler(String input) {
        print(input);
    }

    @Override
    protected String getHost() {
        return host;
    }

    @Override
    protected String getPort() {
        return port;
    }

    @Override
    protected String getDatabaseName() {
        return database;
    }

}

