package org.ethereum.datasource;

import org.ethereum.config.SystemProperties;
import org.ethereum.config.NoAutoscan;
import org.ethereum.datasource.redis.RedisConnection;
import org.ethereum.datasource.redis.RedisConnectionImpl;
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
@NoAutoscan
public abstract class AbstractRedisTest {

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    @NoAutoscan
    static class ContextConfiguration extends TestContext {
        @Bean
        @Transactional(propagation = Propagation.SUPPORTS)
        public BlockStore blockStore(SessionFactory sessionFactory){
            return new InMemoryBlockStore();
        }
    }

    private RedisConnection redisConnection;

    @Autowired
    WorldManager worldManager;

    @After
    public void close(){
        worldManager.close();
    }


    private Boolean connected;

    protected RedisConnection getRedisConnection() {
        if (redisConnection == null) {
            SystemProperties config = SystemProperties.getDefault();
            config.setDataBaseDir("test_db/" + "RedisAll");
            config.setDatabaseReset(true);

            redisConnection = new RedisConnectionImpl(config);
        }

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

            assertFalse(connected ^ getRedisConnection().isAvailable());
        }

        return connected;
    }

}
