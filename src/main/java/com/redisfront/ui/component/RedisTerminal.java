package com.redisfront.ui.component;

import cn.hutool.core.date.DateUtil;
import com.redisfront.commons.constant.Enum;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import com.redisfront.commons.ui.AbstractTerminal;
import com.redisfront.commons.util.LettuceUtils;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.RedisBasicService;
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

public class RedisTerminal extends AbstractTerminal {
    private static final Logger log = LoggerFactory.getLogger(RedisTerminal.class);
    private final ConnectInfo connectInfo;


    public static RedisTerminal newInstance(ConnectInfo connectInfo) {
        return new RedisTerminal(connectInfo);
    }

    public RedisTerminal(final ConnectInfo connectInfo) {
        super();
        this.connectInfo = connectInfo.clone();
        terminal.setEnabled(false);
    }

    public void ping() {
        try {
            if (RedisBasicService.service.ping(connectInfo)) {
                if (!terminal.isEnabled()) {
                    connectInfo.setDatabase(0);
                    terminal.setEnabled(true);
                    super.printConnectedSuccessMessage();
                }
            } else {
                println(DateUtil.formatDateTime(new Date()) + " - ".concat("redis PING failed!"));
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
                    .filter(e -> Fn.equal(e.name(), commandList.get(0).toUpperCase()))
                    .findAny()
                    .orElseThrow(() -> new RedisFrontException("ERR unknown command '" + inputText + "'", false));
            commandList.remove(0);

            if (Fn.equal(connectInfo().redisModeEnum(), Enum.RedisMode.CLUSTER)) {
                LettuceUtils.clusterRun(connectInfo(), redisCommands -> {
                    var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                    println(format(res, ""));
                });
            } else if (Fn.equal(connectInfo().redisModeEnum(), Enum.RedisMode.SENTINEL)) {
                LettuceUtils.sentinelRun(connectInfo(), redisCommands -> {
                    var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                    println(format(res, ""));
                });
            } else {
                LettuceUtils.run(connectInfo(), redisCommands -> {
                    if (CommandType.SELECT.equals(commandType)) {
                        connectInfo.setDatabase(Integer.valueOf(commandList.get(0)));
                    }
                    if (CommandType.PUBLISH.equals(commandType)) {
                        //监听后期完善
                        var newCommandList = new ArrayList<String>();
                        newCommandList.add(commandList.get(0));
                        commandList.remove(0);
                        var message = commandList.toArray(new String[]{});
                        newCommandList.add(String.join(" ", message));
                        var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(newCommandList));
                        println(format(res, ""));
                    } else {
                        var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                        println(format(res, ""));
                    }

                });
            }
        } catch (Exception e) {
            print(e.getMessage());
        }
    }

    private static String format(Object s, String space) {
        var sb = new StringBuilder();
        if (s instanceof List<?> list) {
            if (list.size() == 1) {
                return String.valueOf(list.get(0));
            }
            for (int i = 0; i < list.size(); i++) {
                var item = list.get(i);
                if (item instanceof List<?> itemList) {
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


