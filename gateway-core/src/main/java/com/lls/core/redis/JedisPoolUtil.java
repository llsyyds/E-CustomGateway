package com.lls.core.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.*;

import java.util.Properties;

/**
 * @PROJECT_NAME: api-gateway
 * @DESCRIPTION: 在高并发场景下，提供一个单例的（只会创建一个）、线程安全的 Jedis 连接池，用于管理 Redis 连接
 * @AUTHOR: lls
 * @DATE: 2023/8/4 10:52
 */

@Slf4j
public class JedisPoolUtil {
    public static JedisPool jedisPool = null;
    private String host;
    private int port;
    //在连接池内部创建一个连接允许的最大时间
    private int timeout;
    private String password;
    private int database;
    //连接池中最大的活动连接数
    private int maxTotal;
    //连接池中最大的空闲连接数
    private int maxIdle;
    //连接池中最小的空闲连接数
    private int minIdle;
    //当连接池的连接用完时，新的请求是否需要等待（其最大等待时间是由maxWaitMillis决定的）。true表示等待，false表示抛出异常
    private boolean blockWhenExhausted;
    //当连接池的连接用完时，新的请求等待新连接的最大时间（毫秒）
    private int maxWaitMillis;
    //在从连接池中借出连接时，是否进行有效性检查（ping操作）。如果设为true，则所有借出的连接都是可用的
    private boolean testOnBorrow;
    //在连接归还到连接池时，是否进行有效性检查（ping操作）。如果设为true，那么所有归还的连接都是可用的在连接归还到连接池时，是否进行有效性检查（ping操作）。如果设为true，那么所有归还的连接都是可用的
    private boolean testOnReturn;
    //加载并且解析配置文件
    private void initialConfig() {
        try {
            Properties prop = new Properties();
            //加载文件获取数据 文件带后缀
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("gateway.properties"));

            host = prop.getProperty("redis.host");
            port = Integer.parseInt(prop.getProperty("redis.port"));
            timeout = Integer.parseInt(prop.getProperty("redis.timeout"));
            password = prop.getProperty("redis.password");
            database = Integer.parseInt(prop.getProperty("redis.database"));
            maxTotal = Integer.parseInt(prop.getProperty("redis.maxTotal"));
            maxIdle = Integer.parseInt(prop.getProperty("redis.maxIdle"));
            minIdle = Integer.parseInt(prop.getProperty("redis.minIdle"));
            blockWhenExhausted = Boolean.parseBoolean(prop.getProperty("redis.blockWhenExhausted"));
            maxWaitMillis = Integer.parseInt(prop.getProperty("redis.maxWaitMillis"));
            testOnBorrow = Boolean.parseBoolean(prop.getProperty("redis.testOnBorrow"));
            testOnReturn = Boolean.parseBoolean(prop.getProperty("redis.testOnReturn"));
        } catch (Exception e) {
            log.debug("parse configure file error.");
        }
    }

    /**
     * 初始化连接池即创建jedisPool对象
     */
    private void initialPool() {
            initialConfig();
            try {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(maxTotal);
                config.setMaxIdle(maxIdle);
                config.setMinIdle(minIdle);
                config.setBlockWhenExhausted(blockWhenExhausted);
                config.setMaxWaitMillis(maxWaitMillis);
                config.setTestOnBorrow(testOnBorrow);
                config.setTestOnReturn(testOnReturn);
                jedisPool = new JedisPool(config, host, port,timeout,password,database);
            } catch (Exception e) {
                log.debug("init redis pool failed : {}", e.getMessage());
            }
        }

    //获取jedisPool对象（以单例形式）
    public Jedis getJedis() {
        if (jedisPool == null) {
            synchronized (JedisPoolUtil.class){
                if (jedisPool == null){
                    initialPool();
                }
            }
        }
        try {
            return jedisPool.getResource();
        } catch (Exception e) {
            log.debug("getJedis() throws : {}" + e.getMessage());
        }
        return null;
    }

    public Pipeline getPipeline() {
        BinaryJedis binaryJedis = new BinaryJedis(host, port);
        return binaryJedis.pipelined();
    }
}

