package org.ethereum.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author: Roman Mandeleil
 * Created on: 20/01/2015 13:27
 */

public class RedisPool {

    private static final Logger logger = LoggerFactory.getLogger("db");
    private static JedisPool pool;
    
    static {

        try {
            if (System.getProperty("REDISCLOUD_URL") != null){
                URI redisUri = new URI(System.getenv("REDISCLOUD_URL"));
                logger.info("Init redis pool: "  + redisUri.toString());
                pool = new JedisPool(new JedisPoolConfig(),
                        redisUri.getHost(),
                        redisUri.getPort(),
                        Protocol.DEFAULT_TIMEOUT,
                        redisUri.getUserInfo().split(":",2)[1]);
            }
        } catch (URISyntaxException e) {
            logger.info("Pool is not available");
        }
    }
    
    public static Jedis getResource(){
        if (pool == null) return null;
        return pool.getResource();
    }
}
