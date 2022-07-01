package com.redisfront.util;

import com.redisfront.model.ConnectInfo;
import redis.clients.jedis.*;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * RedisUtil
 *
 * @author Jin
 */
public class JedisUtil {

    public static Object sendCommand(ConnectInfo connect, String inputText) throws Exception {
        var connection = new Connection(new HostAndPort(connect.host(), connect.port()), createJedisClientConfig(connect));
        try (connection) {
            if (connection.ping()) {
                var commandList = new ArrayList<>(List.of(inputText.split(" ")));
                var command = Arrays.stream(Protocol.Command.values())
                        .filter(e -> Fn.equal(e.name(), commandList.get(0).toUpperCase()))
                        .findAny()
                        .orElseThrow(() -> new Throwable("ERR unknown command '" + inputText + "'"));
                commandList.remove(0);
                Object ret = encode(connection.executeCommand(new CommandArguments(command).addObjects(commandList)));
                return format(ret, "");
            } else {
                return "连接失败！";
            }
        } catch (Throwable e) {
            return e.getMessage();
        }
    }


    private static DefaultJedisClientConfig createJedisClientConfig(ConnectInfo connect) throws Exception {
        if (connect.ssl()) {
            if (Fn.isNotNull(connect.sslConfig())) {
                var sslSocketFactory = SslUtil.getSocketFactory(connect.sslConfig().publicKeyFilePath(), connect.sslConfig().grantFilePath(), connect.sslConfig().privateKeyFilePath(), connect.sslConfig().password());
                return DefaultJedisClientConfig
                        .builder()
                        .password(connect.password())
                        .database(connect.database())
                        .ssl(true)
                        .sslSocketFactory(sslSocketFactory)
                        .build();
            }
            return DefaultJedisClientConfig
                    .builder()
                    .password(connect.password())
                    .database(connect.database())
                    .ssl(true)
                    .build();
        } else {
            return DefaultJedisClientConfig.builder().user(connect.user()).password(connect.password()).database(connect.database()).build();
        }
    }

    private static String format(Object s, String space) {
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

    private static Object encode(Object object) {
        if (object instanceof byte[] bytes) {
            return SafeEncoder.encode(bytes);
        } else if (object instanceof List<?> list) {
            return list.stream().parallel().map(JedisUtil::encode).toList();
        } else if (object instanceof Number number) {
            return number;
        } else if (object instanceof String str) {
            return str;
        } else if (object == null) {
            return ("null");
        }
        return object;
    }
}
