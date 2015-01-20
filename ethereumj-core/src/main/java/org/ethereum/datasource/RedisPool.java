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
    private static JedisPool state;
    private static JedisPool details;

    static {

        try {
            
            if (System.getenv("REDIS_STATE") != null) {
                URI redisUri = new URI(System.getenv("REDIS_STATE"));
                logger.info("Init redis pool: "  + redisUri.toString());
                state = new JedisPool(new JedisPoolConfig(),
                        redisUri.getHost(),
                        redisUri.getPort(),
                        Protocol.DEFAULT_TIMEOUT,
                        redisUri.getUserInfo().split(":",2)[1]);
            }

            if (System.getenv("REDIS_DETAILS") != null) {
                URI redisUri = new URI(System.getenv("REDIS_DETAILS"));
                logger.info("Init redis pool: "  + redisUri.toString());
                details = new JedisPool(new JedisPoolConfig(),
                        redisUri.getHost(),
                        redisUri.getPort(),
                        Protocol.DEFAULT_TIMEOUT,
                        redisUri.getUserInfo().split(":",2)[1]);
            }

        } catch (URISyntaxException e) {
            logger.info("Pool is not available");
        }
    }
    
    public static Jedis getResource(String name){
        if (state == null) return null;
        
        if (name.equals("state")) return state.getResource();
        if (name.equals("details")) return details.getResource();
        return null;
    }
}
