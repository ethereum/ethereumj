package org.ethereum.util;

import org.apache.log4j.PropertyConfigurator;
import org.ethereum.config.SystemProperties;

import java.net.URL;

/**
 * @author Roman Mandeleil
 * @since 25.07.2014
 */
public class AdvancedDeviceUtils {

    public static void adjustDetailedTracing(long blockNum) {
        // here we can turn on the detail tracing in the middle of the chain
        if (blockNum >= SystemProperties.getDefault().traceStartBlock() && SystemProperties.getDefault().traceStartBlock() != -1) {
            URL configFile = ClassLoader.getSystemResource("log4j-detailed.properties");
            PropertyConfigurator.configure(configFile);
        }
    }
}
