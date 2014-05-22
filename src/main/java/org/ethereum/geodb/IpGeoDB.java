package org.ethereum.geodb;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 24/04/14 20:11
 */
public class IpGeoDB {   // change

    static{
        try {
            File file = null;
            try {

                URL geiIpDBFile = ClassLoader.getSystemResource("geolitecity.dat");
                file = new File(geiIpDBFile.toURI());
            } catch (Throwable th) {

                String dir = System.getProperty("user.dir");
                String fileName = dir + "/db/GeoLiteCity.dat";
                file = new File(fileName);
            }
            cl = new LookupService(file);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static LookupService cl;

    public static Location getLocationForIp(InetAddress ip){
        try {
            return cl.getLocation(ip);
        } catch (Throwable e) {
            // todo: think about this exception, maybe you can do something more reasonable
            System.out.println(e.getMessage());
//            e.printStackTrace();
        }
        return null;
    }
}
