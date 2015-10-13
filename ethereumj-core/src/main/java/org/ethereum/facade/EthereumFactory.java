package org.ethereum.facade;

import org.ethereum.config.DefaultConfig;
import org.ethereum.config.NoAutoscan;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


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
        return createEthereum(SystemProperties.CONFIG, userSpringConfig);
    }

    public static Ethereum createEthereum(SystemProperties config, Class userSpringConfig) {

        logger.info("Running {},  core version: {}-{}", config.genesisInfo(), config.projectVersion(), config.projectVersionModifier());
        BuildInfo.printInfo();

        if (config.databaseReset()){
            FileUtil.recursiveDelete(config.databaseDir());
            logger.info("Database reset done");
        }

//        List<Class<?>> springConfigs = new ArrayList<>();
//        springConfigs.add(DefaultConfig.class);
//        if (config != SystemProperties.CONFIG) {
//            springConfigs.add(new SysPropConfig(config));
//        }

        return userSpringConfig == null ? createEthereum(new Class[] {DefaultConfig.class}) :
                createEthereum(DefaultConfig.class, userSpringConfig);
    }

    public static Ethereum createEthereum(Class ... springConfigs) {

        if (logger.isInfoEnabled()) {
            StringBuilder versions = new StringBuilder();
            for (EthVersion v : EthVersion.supported()) {
                versions.append(v.getCode()).append(", ");
            }
            versions.delete(versions.length() - 2, versions.length());
            logger.info("capability eth version: [{}]", versions);
        }
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);
        logger.info("capability bzz version: [{}]", BzzHandler.VERSION);

        context = new AnnotationConfigApplicationContext(springConfigs);
        return context.getBean(Ethereum.class);
    }


//    @Configuration
//    @NoAutoscan
//    static class SysPropConfig {
//
//        SystemProperties systemProperties;
//
//        public SysPropConfig(SystemProperties systemProperties) {
//            this.systemProperties = systemProperties;
//        }
//
//        @Bean
//        public SystemProperties systemProperties() {
//            return systemProperties;
//        }
//    }
}
