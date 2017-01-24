package org.ethereum.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import org.ethereum.config.SystemProperties;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * @author Roman Mandeleil
 * @since 25.07.2014
 */
public class AdvancedDeviceUtils {

    public static void adjustDetailedTracing(SystemProperties config, long blockNum) {
        // here we can turn on the detail tracing in the middle of the chain
        if (blockNum >= config.traceStartBlock() && config.traceStartBlock() != -1) {
            final URL configFile = ClassLoader.getSystemResource("logback-detailed.xml");
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final ContextInitializer ci = new ContextInitializer(loggerContext);

            loggerContext.reset();
            try {
                ci.configureByResource(configFile);
            } catch (Exception e) {
                System.out.println("Error applying new config " + e.getMessage());
            }
        }
    }
}
