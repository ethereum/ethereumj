package org.ethereum.facade;

import org.ethereum.config.DefaultConfig;
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

import static org.ethereum.config.SystemProperties.CONFIG;

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

        logger.info("Running {},  core version: {}-{}", CONFIG.genesisInfo(), CONFIG.projectVersion(), CONFIG.projectVersionModifier());
        BuildInfo.printInfo();

        if (CONFIG.databaseReset()){
            FileUtil.recursiveDelete(CONFIG.databaseDir());
            logger.info("Database reset done");
        }

        return userSpringConfig == null ? createEthereum(new Class[] {DefaultConfig.class}) :
                createEthereum(userSpringConfig, DefaultConfig.class);
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

}
