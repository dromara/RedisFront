package com.redisfront.ui.component;

import cn.hutool.core.date.DateUtil;
import com.redisfront.constant.Enum;
import com.redisfront.exception.RedisFrontException;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
import com.redisfront.util.FunUtil;
import com.redisfront.util.LettuceUtil;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
            if (RedisBasicService.service.ping(connectInfo)) {
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
    protected void inputProcessHandler(String inputText) {
        try {

            var commandList = new ArrayList<>(List.of(inputText.split(" ")));
            var commandType = Arrays.stream(CommandType.values())
                    .filter(e -> FunUtil.equal(e.name(), commandList.get(0).toUpperCase()))
                    .findAny()
                    .orElseThrow(() -> new RedisFrontException("ERR unknown command '" + inputText + "'", false));
            commandList.remove(0);

            if (FunUtil.equal(connectInfo().redisModeEnum(), Enum.RedisMode.CLUSTER)) {
                LettuceUtil.clusterRun(connectInfo(), redisCommands -> {
                    var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                    println(format(res, ""));
                });
            } else if (FunUtil.equal(connectInfo().redisModeEnum(), Enum.RedisMode.SENTINEL)) {

            } else {
                LettuceUtil.run(connectInfo(), redisCommands -> {
                        var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                        println(format(res, ""));
                });
            }

        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    private static String format(Object s, String space) {
        StringBuilder sb = new StringBuilder();
        if (s instanceof List<?> list) {
            if (list.size() == 1) {
                return (String) list.get(0);
            }
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof List itemList) {
                    sb.append(space).append(i + 1).append(" ) ").append("\n").append(format(itemList, "  " + space));
                } else {
                    sb.append(space).append(i + 1).append(" ) ").append(item).append("\n");
                }
            }
        } else {
            sb.append(s);
        }
        return sb.toString();
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

