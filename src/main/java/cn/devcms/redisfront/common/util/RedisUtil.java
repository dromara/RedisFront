package cn.devcms.redisfront.common.util;

import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.model.ConnectInfo;
import redis.clients.jedis.CommandArguments;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * RedisUtil
 *
 * @author Jin
 */
public class RedisUtil {

    public static Object sendCommand(ConnectInfo connect, Integer index, String inputCommand) {
        Connection connection = new Connection(connect.host(), connect.port());
        if (Fn.isEmpty(inputCommand)) {
            return "";
        }
        if (!connection.ping()) {
            return "连接失败！";
        }
        if (Fn.isNotNull(index)) {
            connection.select(index);
        }
        try {
            ArrayList<String> commands = new ArrayList<>(List.of(inputCommand.split(" ")));
            Protocol.Command command = Protocol.Command.valueOf(commands.get(0).toUpperCase());
            commands.remove(0);
            CommandArguments commandArguments = new CommandArguments(command).addObjects(commands);
            return connection.executeCommand(commandArguments);
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            connection.close();
        }

    }

}
