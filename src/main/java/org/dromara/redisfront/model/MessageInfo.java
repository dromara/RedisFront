package org.dromara.redisfront.model;


import java.io.Serializable;

/**
 * ConnectInfo
 *
 * @author Jin
 */
public class MessageInfo implements Serializable {

    private String date;
    private String channel;
    private String message;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
