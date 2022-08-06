package com.redisfront.model;

public class ClusterNode {
    private String id;
    private String ipAndPort;
    private String flags;
    private String master;
    private String ping;
    private String pong;
    private String epoch;
    private String state;
    private String slot;
    private String host;
    private Integer port;

    public String id() {
        return id;
    }

    public ClusterNode setId(String id) {
        this.id = id;
        return this;
    }

    public String ipAndPort() {
        return ipAndPort;
    }

    public ClusterNode setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
        return this;
    }

    public String flags() {
        return flags;
    }

    public ClusterNode setFlags(String flags) {
        this.flags = flags;
        return this;
    }

    public String master() {
        return master;
    }

    public ClusterNode setMaster(String master) {
        this.master = master;
        return this;
    }

    public String ping() {
        return ping;
    }

    public ClusterNode setPing(String ping) {
        this.ping = ping;
        return this;
    }

    public String pong() {
        return pong;
    }

    public ClusterNode setPong(String pong) {
        this.pong = pong;
        return this;
    }

    public String epoch() {
        return epoch;
    }

    public ClusterNode setEpoch(String epoch) {
        this.epoch = epoch;
        return this;
    }

    public String state() {
        return state;
    }

    public ClusterNode setState(String state) {
        this.state = state;
        return this;
    }

    public String slot() {
        return slot;
    }

    public ClusterNode setSlot(String slot) {
        this.slot = slot;
        return this;
    }

    public String host() {
        return host;
    }

    public ClusterNode setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer port() {
        return port;
    }

    public ClusterNode setPort(Integer port) {
        this.port = port;
        return this;
    }
}
