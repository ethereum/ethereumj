package org.ethereum.config;

import org.ethereum.net.eth.EthVersion;
import org.ethereum.net.shh.ShhHandler;
import org.ethereum.net.swarm.bzz.BzzHandler;
import org.ethereum.util.BuildInfo;
import org.ethereum.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Created by Anton Nashatyrev on 13.05.2016.
 */
class Initializer implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger("general");

    /**
     * Method to be called right after the config is instantiated.
     * Effectively is called before any other bean is initialized
     */
    private void initConfig(SystemProperties config) {
        logger.info("Running {},  core version: {}-{}", config.genesisInfo(), config.projectVersion(), config.projectVersionModifier());
        BuildInfo.printInfo();

        if (config.databaseReset()){
            FileUtil.recursiveDelete(config.databaseDir());
            logger.info("Database reset done");
        }

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

    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SystemProperties) {
            initConfig((SystemProperties) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
