package org.ethereum.db;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 24/04/14 20:11
 */
public class IpGeoDB {   // change

	private static Logger logger = LoggerFactory.getLogger(IpGeoDB.class);
	
	static {
		try {
			File file = null;
			try {

				String dir = System.getProperty("user.dir");
				String fileName = dir + "/config/GeoLiteCity.dat";
				file = new File(fileName);
				if (!file.exists()) {
					URL geiIpDBFile = ClassLoader
							.getSystemResource("GeoLiteCity.dat");
					file = new File(geiIpDBFile.toURI());
				}
			} catch (Throwable th) {
				logger.error(th.getMessage(), th);
				System.exit(-1);
			}
			cl = new LookupService(file);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

    private static LookupService cl;

    public static Location getLocationForIp(InetAddress ip) {
        try {
            return cl.getLocation(ip);
        } catch (Throwable e) {
            // TODO: think about this exception, maybe you can do something more reasonable
        	logger.error(e.getMessage(), e);
        }
        return null;
    }
}
