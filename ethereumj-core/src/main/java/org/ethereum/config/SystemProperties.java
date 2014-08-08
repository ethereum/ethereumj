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
 * @author: Roman Mandeleil
 * Created on: 22/05/2014 19:22
 */
public class SystemProperties {

    private static Logger logger = LoggerFactory.getLogger(SystemProperties.class);
	
	private static int     DEFAULT_TX_APPROVE_TIMEOUT = 10;
	private static String  DEFAULT_DISCOVERY_PEER_LIST = "54.201.28.117:30303";
	private static String  DEFAULT_ACTIVE_PEER_IP = "54.201.28.117";
	private static int     DEFAULT_ACTIVE_PORT = 30303;
    private static String  DEFAULT_SAMPLES_DIR = "samples";
    private static String  DEFAULT_COINBASE_SECRET = "monkey";
    private static int     DEFAULT_ACTIVE_PEER_CHANNEL_TIMEOUT = 5;
    private static Boolean DEFAULT_DB_RESET = false;
    private static Boolean DEFAULT_DUMP_FULL = false;
    private static String  DEFAULT_DUMP_DIR = "dmp";
    private static String  DEFAULT_DATABASE_DIR = System.getProperty("user.dir");
    private static Boolean DEFAULT_DUMP_CLEAN_ON_RESTART = true;
    private static Boolean DEFAULT_PLAY_VM = true;
    private static Boolean DEFAULT_BLOCKCHAIN_ONLY = false;
    private static int     DEFAULT_TRACE_STARTBLOCK = -1;
    private static byte    DEFAULT_MAX_BLOCKS_ASK = 10;
    private static int     DEFAULT_MAX_BLOCKS_QUEUED = 300;
    private static String  DEFAULT_PROJECT_VERSION = "";
    private static String  DEFAULT_HELLO_PHRASE = "RJ";


	public static SystemProperties CONFIG = new SystemProperties();
    private Properties prop = new Properties();
    private InputStream input = null;
    
    public SystemProperties() {
        
    	try {
            String userDir = System.getProperty("user.dir");
            String fileName = userDir + "/config/system.properties";
            File file = new File(fileName);

            if (file.exists()) {
                input = new FileInputStream(file);
			} else {
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

    public int peerDiscoveryWorkers() {
        if(prop.isEmpty()) return 2;
		return Integer.parseInt(prop.getProperty("peer.discovery.workers"));
    }

    public int peerDiscoveryTimeout() {
		if (prop.isEmpty())
			return 10000;
		return Integer.parseInt(prop.getProperty("peer.discovery.timeout")) * 1000;
    }

    public int transactionApproveTimeout() {
        if (prop.isEmpty())
            return DEFAULT_TX_APPROVE_TIMEOUT;
        return Integer.parseInt(prop.getProperty("transaction.approve.timeout"));
    }

    public String peerDiscoveryIPList() {
        if(prop.isEmpty()) return DEFAULT_DISCOVERY_PEER_LIST;
        return prop.getProperty("peer.discovery.ip.list");
    }

    public boolean databaseReset() {
        if(prop.isEmpty()) return DEFAULT_DB_RESET;
        return Boolean.parseBoolean(prop.getProperty("database.reset"));
    }

    public String activePeerIP() {
        if(prop.isEmpty()) return DEFAULT_ACTIVE_PEER_IP;
        return prop.getProperty("peer.active.ip");
    }

    public int activePeerPort() {
        if(prop.isEmpty()) return DEFAULT_ACTIVE_PORT;
        return Integer.parseInt(prop.getProperty("peer.active.port"));
    }

    public String samplesDir() {
        if(prop.isEmpty()) return DEFAULT_SAMPLES_DIR;
        return prop.getProperty("samples.dir");
    }

    public String coinbaseSecret() {
        if(prop.isEmpty()) return DEFAULT_COINBASE_SECRET;
        return prop.getProperty("coinbase.secret");
    }

    public Integer activePeerChannelTimeout() {
        if(prop.isEmpty()) return DEFAULT_ACTIVE_PEER_CHANNEL_TIMEOUT;
        return Integer.parseInt(prop.getProperty("active.peer.channel.timeout"));
    }

    public Integer traceStartBlock() {
        if(prop.isEmpty()) return DEFAULT_TRACE_STARTBLOCK;
        return Integer.parseInt(prop.getProperty("trace.startblock"));
    }


    public Boolean dumpFull() {
        if(prop.isEmpty()) return DEFAULT_DUMP_FULL;
        return Boolean.parseBoolean(prop.getProperty("dump.full"));
    }

    public String dumpDir() {
        if(prop.isEmpty()) return DEFAULT_DUMP_DIR;
        return prop.getProperty("dump.dir");
    }

    public String databaseDir() {
        if(prop.isEmpty()) return DEFAULT_DATABASE_DIR;
        return prop.getProperty("database.dir");
    }

    public Boolean dumpCleanOnRestart() {
        if(prop.isEmpty()) return DEFAULT_DUMP_CLEAN_ON_RESTART;
		return Boolean.parseBoolean(prop.getProperty("dump.clean.on.restart"));
    }

	public void print() {
		Enumeration<?> e = prop.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = prop.getProperty(key);
			if (!key.equals("null"))
				logger.info("Key: " + key + ", Value: " + value);
		}
	}

    public Boolean playVM() {
        if(prop.isEmpty()) return DEFAULT_PLAY_VM;
        return Boolean.parseBoolean(prop.getProperty("play.vm"));
    }

    public Boolean blockChainOnly() {
        if(prop.isEmpty()) return DEFAULT_BLOCKCHAIN_ONLY;
        return Boolean.parseBoolean(prop.getProperty("blockchain.only"));
    }


    public Byte maxBlocksAsk() {
        if(prop.isEmpty()) return DEFAULT_MAX_BLOCKS_ASK;
        return Byte.parseByte(prop.getProperty("max.blocks.ask"));
    }

    public Integer maxBlocksQueued() {
        if(prop.isEmpty()) return DEFAULT_MAX_BLOCKS_QUEUED;
        return Integer.parseInt(prop.getProperty("max.blocks.queued"));
    }

    public String projectVersion() {
        if(prop.isEmpty()) return DEFAULT_PROJECT_VERSION;
        return prop.getProperty("project.version");
    }

    public String helloPhrase() {
        if(prop.isEmpty()) return DEFAULT_HELLO_PHRASE;
        return prop.getProperty("hello.phrase");
    }


    public static void main(String args[]) {
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.print();
    }
}
