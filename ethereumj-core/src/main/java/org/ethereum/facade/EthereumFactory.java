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

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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

    public static Ethereum createEthereum() {
        return createEthereum(SystemProperties.getDefault());
    }

    public static Ethereum createEthereum(SystemProperties config) {
        return createEthereum(config, null);
    }

    public static Ethereum createEthereum(Class userSpringConfig) {
        return createEthereum(SystemProperties.getDefault(), userSpringConfig);
    }

    public static Ethereum createEthereum(SystemProperties config, Class userSpringConfig) {
        logger.info("Running {},  core version: {}-{}", config.genesisInfo(), SystemProperties.projectVersion(), SystemProperties.projectVersionModifier());
        BuildInfo.printInfo();

        if (config.databaseReset()){
            FileUtil.recursiveDelete(config.databaseDir());
            logger.info("Database reset done");
        }

        Class[] springConfigs = userSpringConfig == null ?
                new Class[] { DefaultConfig.class } :
                new Class[] { DefaultConfig.class, userSpringConfig };

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

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("systemProperties", config);
        context.register(springConfigs);
        context.refresh();
        return context.getBean(Ethereum.class);
    }

}
