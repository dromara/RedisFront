package cn.devcms.redisfront.common;

import redis.clients.jedis.Connection;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * TerminalConnection
 *
 * @author Jin
 */
public class TerminalConnection extends Connection {

    public void sendCommand(String command) throws JedisConnectionException {
        super.connect();

    }
}
