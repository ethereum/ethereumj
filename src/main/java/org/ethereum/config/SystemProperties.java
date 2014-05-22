package org.ethereum.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 22/05/2014 19:22
 */

public class SystemProperties {

    Properties prop = new Properties();
    InputStream input = null;

    public static SystemProperties config = new SystemProperties();

    public SystemProperties() {


        try {

            String filename = "system.properties";
            input = SystemProperties.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                System.out.println("Sorry, unable to find " + filename);
                return;
            }

            //load a properties file from class path, inside static method
            prop.load(input);


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public boolean peerDiscovery(){

        if(prop.isEmpty()) return true;

        boolean result =
                Boolean.parseBoolean( prop.getProperty("peer.discovery") );

        return result;
    }

    public int peerDiscoveryWorkers(){
        if(prop.isEmpty()) return 2;

        int result =
                Integer.parseInt( prop.getProperty("peer.discovery") );

        return result;
    }


    public String toString(){

        Enumeration<?> e = prop.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = prop.getProperty(key);

            if (!key.equals("null"))
                System.out.println("Key : " + key + ", Value : " + value);
        }

        return "";
    }


    public static void main(String args[]){

        SystemProperties systemProperties = new SystemProperties();
        System.out.println(systemProperties.toString());



    }

}
