package cn.devcms.redisfront.common.util;

import cn.devcms.redisfront.common.enums.ConnectEnum;
import cn.devcms.redisfront.common.func.Fn;
import cn.devcms.redisfront.model.ConnectInfo;
import redis.clients.jedis.*;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToDoubleFunction;

/**
 * RedisUtil
 *
 * @author Jin
 */
public class RedisUtil {

    public static Object sendCommand(ConnectInfo connect, Integer database, String inputText) {
        Connection connection = new Connection(
                new HostAndPort(connect.host(), connect.port()),
                DefaultJedisClientConfig
                        .builder()
                        .password(connect.password())
                        .database(database).build()
        );
        try (connection) {
            if (Fn.isEmpty(inputText)) {
                return "";
            }
            if (!connection.ping()) {
                return "连接失败！";
            }
            ArrayList<String> commands = new ArrayList<>(List.of(inputText.split(" ")));
            Protocol.Command command = Protocol.Command.valueOf(commands.get(0).toUpperCase());
            commands.remove(0);
//            CommandArguments commandArguments = connection.executeCommand()
            return connection.executeCommand(new CommandArguments(command).addObjects(commands));
//            return connection.executeCommand(new ClusterCommandArguments(command).addObjects(commands));
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static void main(String[] args) {

        Object object = RedisUtil.sendCommand(new ConnectInfo("A",
                        "127.0.0.1",
                        63378,
                        null,
                        ConnectEnum.NORMAL),
                11,
                "SSCAN hnct_oauth:client_id_to_access:test 0 COUNT 100");

        if (object instanceof byte[] bytes) {
            String s = SafeEncoder.encode(bytes);
            System.out.println(s);
        }
        if (object instanceof List<?> list) {
            List<Object> str = list.stream().map(obj -> {
                if (obj instanceof byte[] bytes) {
                    return SafeEncoder.encode(bytes);
                } else if (obj instanceof List<?> objects) {
                    return objects.stream().map(bytes -> SafeEncoder.encode((byte[]) bytes)).toList();
                } else {
                    return (String) obj;
                }
            }).toList();
            System.out.println(str);
        }
        if (object instanceof Number number) {
            System.out.println(number);
        }
        if (object instanceof String str) {
            System.out.println(str);
        }
        if (object == null) {
            System.out.println("null");
        }
        System.out.println();
    }
}
