package org.ethereum.util;

import org.apache.log4j.PropertyConfigurator;

import java.net.URL;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * @author Roman Mandeleil
 * @since 25.07.2014
 */
public class AdvancedDeviceUtils {

    public static void adjustDetailedTracing(long blockNum) {
        // here we can turn on the detail tracing in the middle of the chain
        if (blockNum >= CONFIG.traceStartBlock() && CONFIG.traceStartBlock() != -1) {
            URL configFile = ClassLoader.getSystemResource("log4j-detailed.properties");
            PropertyConfigurator.configure(configFile);
        }
    }
}
