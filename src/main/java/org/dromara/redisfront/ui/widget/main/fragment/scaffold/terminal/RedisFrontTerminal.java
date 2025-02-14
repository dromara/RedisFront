package org.dromara.redisfront.ui.widget.main.fragment.scaffold.terminal;

import cn.hutool.core.date.DateUtil;
import org.dromara.redisfront.commons.enums.RedisMode;
import org.dromara.redisfront.model.context.RedisConnectContext;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.ArrayOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import org.dromara.redisfront.commons.exception.RedisFrontException;
import org.dromara.redisfront.commons.utils.RedisFrontUtils;
import org.dromara.redisfront.commons.utils.LettuceUtils;
import org.dromara.redisfront.service.RedisBasicService;
import org.dromara.redisfront.ui.components.terminal.AbstractTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RedisFrontTerminal extends AbstractTerminal {
    private static final Logger log = LoggerFactory.getLogger(RedisFrontTerminal.class);
    private final RedisConnectContext redisConnectContext;

    public RedisFrontTerminal(final RedisConnectContext redisConnectContext) {
        super();
        this.redisConnectContext = redisConnectContext.clone();
        terminal.setEnabled(true);
        printConnectedSuccessMessage();
    }

    public void ping() {
        try {
            if (RedisBasicService.service.ping(redisConnectContext)) {
                if (!terminal.isEnabled()) {
                    redisConnectContext.setDatabase(0);
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
                    .filter(e -> RedisFrontUtils.equal(e.name(), commandList.getFirst().toUpperCase()))
                    .findAny()
                    .orElseThrow(() -> new RedisFrontException("ERR unknown command '" + inputText + "'", false));
            commandList.removeFirst();

            if (RedisFrontUtils.equal(connectInfo().getRedisMode(), RedisMode.CLUSTER)) {
                LettuceUtils.clusterRun(connectInfo(), redisCommands -> {
                    var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                    println(format(res, ""));
                });
            } else if (RedisFrontUtils.equal(connectInfo().getRedisMode(), RedisMode.SENTINEL)) {
                LettuceUtils.sentinelRun(connectInfo(), redisCommands -> {
                    var res = redisCommands.dispatch(commandType, new ArrayOutput<>(new StringCodec()), new CommandArgs<>(new StringCodec()).addKeys(commandList));
                    println(format(res, ""));
                });
            } else {
                LettuceUtils.run(connectInfo(), redisCommands -> {
                    if (CommandType.SELECT.equals(commandType)) {
                        redisConnectContext.setDatabase(Integer.valueOf(commandList.getFirst()));
                    }
                    if (CommandType.PUBLISH.equals(commandType)) {
                        //监听后期完善
                        var newCommandList = new ArrayList<String>();
                        newCommandList.add(commandList.getFirst());
                        commandList.removeFirst();
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
                return String.valueOf(list.getFirst());
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
    protected RedisConnectContext connectInfo() {
        return redisConnectContext;
    }

    @Override
    protected String databaseName() {
        return String.valueOf(redisConnectContext.getDatabase());
    }

}


