package org.dromara.redisfront.model;

import java.time.LocalDateTime;

public class LogInfo {

    private LocalDateTime date;
    private String ip;
    private String info;

    public String ip() {
        return ip;
    }

    public LogInfo setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public LocalDateTime date() {
        return date;
    }

    public LogInfo setDate(LocalDateTime date) {
        this.date = date;
        return this;
    }

    public String info() {
        return info;
    }

    public LogInfo setInfo(String info) {
        this.info = info;
        return this;
    }
}
