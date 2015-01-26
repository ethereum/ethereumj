package org.ethereum.facade;

import org.ethereum.net.eth.EthHandler;
import org.ethereum.net.shh.ShhHandler;

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

    public static Ethereum createEthereum() {
        return createEthereum(DefaultConfig.class);
    }

    public static Ethereum createEthereum(Class clazz) {

        logger.info("capability eth version: [{}]", EthHandler.VERSION);
        logger.info("capability shh version: [{}]", ShhHandler.VERSION);

        ApplicationContext context = new AnnotationConfigApplicationContext(clazz);
        return context.getBean(Ethereum.class);
    }

}
