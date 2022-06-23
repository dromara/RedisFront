package com.redisfront.util;

import javax.net.ssl.SSLSocketFactory;


public class SslUtilTest {

    public static void main(String[] args) {
        try {
            SSLSocketFactory socketFactory = SslUtil.getSocketFactory("ca.crt", "redis.crt", "redis.key", "D8769D08908529D6");
            System.out.println(socketFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
