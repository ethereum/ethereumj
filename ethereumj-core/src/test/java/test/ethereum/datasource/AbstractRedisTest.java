package test.ethereum.datasource;

import org.ethereum.datasource.redis.RedisConnection;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import test.ethereum.TestContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class AbstractRedisTest {

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext { }

    @Autowired
    protected RedisConnection redisConnection;

}
