//package cn.devcms.redisfront.common.util;
//
//import cn.devcms.redisfront.model.ConnectInfo;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisCluster;
//import redis.clients.jedis.JedisPool;
//import redis.clients.jedis.JedisPoolConfig;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//public class JedisUtils {
//
//    protected static Logger log = LoggerFactory.getLogger(JedisUtils.class);
//
//    private void JedisUtil() {
//
//    }
//
//    private static Map<String, Jedis> maps = new HashMap<>();
//
//    private static Jedis getJedis(String ip, int port, String user, String password, boolean ssl) {
//        String key = ip + ":" + port;
//        JedisPool pool = null;
//        if (!maps.containsKey(key)) {
//
//            try {
//                JedisPoolConfig poolConfig = new JedisPoolConfig();
//                poolConfig.setMaxTotal(1024);
//                poolConfig.setMaxIdle(200);
//                poolConfig.setMaxWait(Duration.ofMillis(10000));
//                poolConfig.setTestOnBorrow(true);
//                JedisCluster
//                pool = new JedisPool(poolConfig, ip, port, 2000,user,password,ssl);
//                maps.put(key, pool);
//            } catch (Exception e) {
//                log.error(e.getMessage());
//            }
//        } else {
//            pool = maps.get(key);
//        }
//        return pool;
//    }
//
//    private static class RedisUtilHolder {
//        private static JedisUtils instance = new JedisUtils();
//    }
//
//    public static JedisUtils getInstance() {
//        return RedisUtilHolder.instance;
//    }
//
//
//    public Jedis getJedis(ConnectInfo connectInfo) {
//        Jedis jedis = null;
//        int count = 0;
//        do {
//            try {
//                jedis = getPool(connectInfo.host(), connectInfo.port(), connectInfo.user(), connectInfo.password(), connectInfo.ssl()).getResource();
//            } catch (Exception e) {
//                log.error("get redis master1 failed!", e);
//                getPool(connectInfo.host(), connectInfo.port(), connectInfo.user(), connectInfo.password(), connectInfo.ssl()).close();
//            }
//            count++;
//        } while (jedis == null && count < 5);
//        return jedis;
//    }
//}