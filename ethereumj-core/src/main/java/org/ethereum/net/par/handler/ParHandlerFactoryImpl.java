package org.ethereum.net.par.handler;

import org.ethereum.net.par.ParVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Default factory {@link ParHandlerFactory} implementation
 */
@Component
public class ParHandlerFactoryImpl implements ParHandlerFactory {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public ParHandler create(ParVersion version) {
        switch (version) {
            case PAR1:   return (ParHandler) ctx.getBean("Par1");
            default:    throw new IllegalArgumentException("Par " + version + " is not supported");
        }
    }
}
