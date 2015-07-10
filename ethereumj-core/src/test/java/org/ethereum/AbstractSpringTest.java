package org.ethereum;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * @author Mikhail Kalinin
 * @since 09.07.2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public abstract class AbstractSpringTest {

    @Autowired
    protected ApplicationContext context;

    @Configuration
    @ComponentScan(basePackages = "org.ethereum")
    static class ContextConfiguration extends TestContext {
    }

}
