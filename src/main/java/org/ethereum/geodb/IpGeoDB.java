package org.ethereum.geodb;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 24/04/14 20:11
 */
public class IpGeoDB {

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
