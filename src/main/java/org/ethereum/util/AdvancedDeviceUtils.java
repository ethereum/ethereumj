package org.ethereum.util;

import com.maxmind.geoip.Location;
import org.apache.log4j.PropertyConfigurator;
import org.ethereum.db.IpGeoDB;

import java.net.InetAddress;
import java.net.URL;

import static org.ethereum.config.SystemProperties.CONFIG;

/**
 * www.ethereumJ.com
 *
 * @author: Roman Mandeleil
 * Created on: 25/07/2014 13:01
 */

public class AdvancedDeviceUtils {

    public static void adjustDetailedTracing(long blockNum){
        // here we can turn on the detail tracing in the middle of the chain
        if (blockNum >= CONFIG.traceStartBlock() && CONFIG.traceStartBlock() != -1) {
            URL configFile = ClassLoader
                    .getSystemResource("log4j-detailed.properties");

            PropertyConfigurator.configure(configFile);
        }
    }
}
