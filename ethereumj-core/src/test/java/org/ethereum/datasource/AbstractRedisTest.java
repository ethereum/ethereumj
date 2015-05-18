package org.ethereum.datasource;

import org.ethereum.config.SystemProperties;
import org.ethereum.datasource.redis.RedisConnection;
import org.ethereum.db.BlockStore;
import org.ethereum.db.InMemoryBlockStore;
import org.ethereum.manager.WorldManager;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import org.ethereum.TestContext;

import java.net.URI;

import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public abstract class AbstractRedisTest {

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext {
        static {
            SystemProperties.CONFIG.setDataBaseDir("test_db/" + "RedisAll");
            SystemProperties.CONFIG.setDatabaseReset(true);
        }

        @Bean
        @Transactional(propagation = Propagation.SUPPORTS)
        public BlockStore blockStore(SessionFactory sessionFactory){
            return new InMemoryBlockStore();
        }
    }

    @Autowired
    private RedisConnection redisConnection;

    @Autowired
    WorldManager worldManager;

    @After
    public void close(){
        worldManager.close();
    }


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
                connected = false;
                System.out.printf("Cannot connect to '%s' Redis cloud.\n", url);
            }

            assertFalse(connected ^ redisConnection.isAvailable());
        }

        return connected;
    }

}
