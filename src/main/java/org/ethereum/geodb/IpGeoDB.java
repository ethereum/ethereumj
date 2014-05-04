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

            URL geiIpDBFile = ClassLoader.getSystemResource("GeoLiteCity.dat");
            File file = new File(geiIpDBFile.toURI());
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
