package org.ethereum.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 22/05/2014 19:22
 */
public class SystemProperties {

	private static Logger logger = LoggerFactory.getLogger(SystemProperties.class);
	
	public static SystemProperties CONFIG = new SystemProperties();
    private Properties prop = new Properties();
    private InputStream input = null;
    
    public SystemProperties() {
        try {

            File file = null;
            String dir = System.getProperty("user.dir");
            String fileName = dir + "/config/system.properties";
            file = new File(fileName);

            if (file.exists()){
                input = new FileInputStream(file);
            }  else{
                fileName = "system.properties";
                input = SystemProperties.class.getClassLoader().getResourceAsStream(fileName);
                if (input == null) {
                    logger.error("Sorry, unable to find " + fileName);
                    return;
                }
            }

            //load a properties file from class path, inside static method
            prop.load(input);

        } catch (IOException ex) {
        	logger.error(ex.getMessage(), ex);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	public boolean peerDiscovery() {
		if (prop.isEmpty())
			return true;
		return Boolean.parseBoolean(prop.getProperty("peer.discovery"));
	}

    public int peerDiscoveryWorkers(){
        if(prop.isEmpty()) return 2;
        return Integer.parseInt( prop.getProperty("peer.discovery.workers") );
    }

    public int peerDiscoveryTimeout(){
		if (prop.isEmpty())
			return 10000;
		return Integer.parseInt(prop.getProperty("peer.discovery.timeout")) * 1000;
    }

    public int transactionApproveTimeout(){
        if (prop.isEmpty())
            return 10;
        return Integer.parseInt(prop.getProperty("transaction.approve.timeout"));
    }


    public String peerDiscoveryIP(){
        if(prop.isEmpty()) return "54.201.28.117";
        return prop.getProperty("peer.discovery.ip");
    }

    public int peerDiscoveryPort(){
        if(prop.isEmpty()) return 30303;
        return Integer.parseInt(prop.getProperty("peer.discovery.port"));
    }
    
    public boolean databaseReset(){
        if(prop.isEmpty()) return false;
        return Boolean.parseBoolean(prop.getProperty("database.reset"));
    }

    public String activePeerIP(){
        if(prop.isEmpty()) return "54.201.28.117";
        return prop.getProperty("peer.active.ip");
    }

    public int activePeerPort(){
        if(prop.isEmpty()) return 30303;
        return Integer.parseInt(prop.getProperty("peer.active.port"));
    }

    public String samplesDir(){
        if(prop.isEmpty()) return "samples";
        return prop.getProperty("samples.dir");
    }


	public String toString() {
		Enumeration<?> e = prop.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = prop.getProperty(key);
			if (!key.equals("null"))
				logger.info("Key: " + key + ", Value: " + value);
		}
		return "";
	}

    public static void main(String args[]){
        SystemProperties systemProperties = new SystemProperties();
        logger.info(systemProperties.toString());
    }
}
