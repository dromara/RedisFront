package com.redisfront.common.util;

import com.redisfront.common.func.Fn;
import com.redisfront.model.ConnectInfo;
import cn.hutool.core.io.IoUtil;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class TelnetUtil {

    public static String sendCommand(ConnectInfo connect, String command) {
        try {
            var telnetClient = new TelnetClient("vt200");
            telnetClient.setConnectTimeout(5000);
            telnetClient.setDefaultTimeout(5000);
            telnetClient.connect(connect.host(), connect.port());
            if (telnetClient.isConnected() && telnetClient.isAvailable()) {
                var printStream = new PrintStream(telnetClient.getOutputStream());
                if (!Fn.isEmpty(connect.password())) {
                    printStream.println("auth " + connect.password());
                    printStream.flush();
                }
                printStream.println(command);
                printStream.flush();
                printStream.println("quit");
                printStream.flush();
                var result = IoUtil.read(telnetClient.getInputStream(), StandardCharsets.UTF_8);
                telnetClient.disconnect();
                return result;
            } else {
                return "连接已断开...";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

}
