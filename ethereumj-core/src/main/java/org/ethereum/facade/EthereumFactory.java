package org.ethereum.facade;

import org.ethereum.config.DefaultConfig;
import org.ethereum.config.SystemProperties;
import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.shh.ShhHandler;

import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.util.BuildInfo;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;


/**
 * @author Roman Mandeleil
 * @since 13.11.2014
 */
@Component
public class EthereumFactory {

    private static final Logger logger = LoggerFactory.getLogger("general");
    public static ApplicationContext context = null;

    public static Ethereum createEthereum() {
        return createEthereum((Class) null);
    }

    public static Ethereum createEthereum(Class userSpringConfig) {
        return userSpringConfig == null ? createEthereum(new Class[] {DefaultConfig.class}) :
                createEthereum(DefaultConfig.class, userSpringConfig);
    }

    /**
     * @deprecated The config parameter is not used anymore. The configuration is passed
     * via 'systemProperties' bean either from the DefaultConfig or from supplied userSpringConfig
     * @param config  Not used
     * @param userSpringConfig   User Spring configuration class
     * @return  Fully initialized Ethereum instance
     */
    public static Ethereum createEthereum(SystemProperties config, Class userSpringConfig) {

        return userSpringConfig == null ? createEthereum(new Class[] {DefaultConfig.class}) :
                createEthereum(DefaultConfig.class, userSpringConfig);
    }

    public static Ethereum createEthereum(Class ... springConfigs) {
        logger.info("Starting EthereumJ...");
        context = new AnnotationConfigApplicationContext(springConfigs);
        return context.getBean(Ethereum.class);
    }
}
