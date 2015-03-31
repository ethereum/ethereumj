package test.ethereum.datasource;

import org.ethereum.datasource.redis.RedisConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import test.ethereum.TestContext;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public abstract class AbstractRedisTest {

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext { }

    @Autowired
    private RedisConnection redisConnection;
    
    private Boolean connected;

    protected RedisConnection getRedisConnection() {
        return redisConnection;
    }

    protected Boolean isConnected() {
        if (connected == null) {
            String url = System.getenv(RedisConnection.REDISCLOUD_URL);
            try {
                Jedis jedis = new Jedis(new URI(url));
                connected = jedis.ping().equals("PONG");
                jedis.close();
            } catch (Exception e) {
                System.out.printf("Cannot connect to '%s' Redis cloud.\n", url);
            }

            assertFalse(connected ^ redisConnection.isAvailable());
        }
        
        return connected;
    }

}
