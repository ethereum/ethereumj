package org.ethereum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Utility class to retrieve property values from the system.properties files
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {

    private static Logger logger = LoggerFactory.getLogger("general");

    private final static int DEFAULT_TX_APPROVE_TIMEOUT = 10;
    private final static String DEFAULT_DISCOVERY_PEER_LIST = "poc-7.ethdev.com:30303";
    private final static String DEFAULT_ACTIVE_PEER_IP = "poc-7.ethdev.com";
    private final static int DEFAULT_ACTIVE_PORT = 30303;
    private final static String DEFAULT_SAMPLES_DIR = "samples";
    private final static String DEFAULT_COINBASE_SECRET = "monkey";
    private final static int DEFAULT_ACTIVE_PEER_CHANNEL_TIMEOUT = 5;
    private final static Boolean DEFAULT_DB_RESET = false;
    private final static Boolean DEFAULT_DUMP_FULL = false;
    private final static Boolean DEFAULT_RECORD_BLOCKS = false;
    private final static String DEFAULT_DUMP_DIR = "dmp";
    private final static String DEFAULT_DUMP_STYLE = "standard+";
    private final static Integer DEFAULT_VMTRACE_BLOCK = 0;
    private final static String DEFAULT_DATABASE_DIR = System.getProperty("user.dir");
    private final static Boolean DEFAULT_DUMP_CLEAN_ON_RESTART = true;
    private final static Boolean DEFAULT_PLAY_VM = true;
    private final static Boolean DEFAULT_BLOCKCHAIN_ONLY = false;
    private final static int DEFAULT_TRACE_STARTBLOCK = -1;
    private final static int DEFAULT_MAX_HASHES_ASK = -1; // unlimited
    private final static int DEFAULT_MAX_BLOCKS_ASK = 10;
    private final static int DEFAULT_MAX_BLOCKS_QUEUED = 300;
    private final static String DEFAULT_PROJECT_VERSION = "";
    private final static String DEFAULT_HELLO_PHRASE = "Dev";
    private final static Boolean DEFAULT_VM_TRACE = false;
    private final static String DEFAULT_VM_TRACE_DIR = "dmp";
    private final static int DEFAULT_PEER_LISTEN_PORT = 30303;
    private final static String DEFAULT_KEY_VALUE_DATA_SOURCE = "leveldb";
    private final static boolean DEFAULT_REDIS_ENABLED = true;


    /* Testing */
    private final static Boolean DEFAULT_VMTEST_LOAD_LOCAL = false;

    private final static String DEFAULT_PROTOCOL_LIST = "eth,shh";

    public static SystemProperties CONFIG = new SystemProperties();
    private final Properties prop = new Properties();

    public SystemProperties() {

        InputStream input = null;
        try {
            String userDir = System.getProperty("user.dir");
            String fileName = userDir + "/config/system.properties";
            File file = new File(fileName);

            if (file.exists()) {
                logger.info("config loaded from {}", fileName);
                input = new FileInputStream(file);
            } else {
                fileName = "system.properties";
                input = SystemProperties.class.getClassLoader()
                        .getResourceAsStream(fileName);
                if (input == null) {
                    logger.error("Sorry, unable to find {}", fileName);
                    return;
                }
            }
            // load a properties file from class path, inside static method
            prop.load(input);

            overrideCLIParams();

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

    private void overrideCLIParams() {
        String value = System.getProperty("keyvalue.datasource");
        if (value != null) {
            prop.setProperty("keyvalue.datasource", value);
        }
    }

    public boolean peerDiscovery() {
        return Boolean.parseBoolean(prop.getProperty("peer.discovery", "true"));
    }

    public int peerDiscoveryWorkers() {
        return Integer.parseInt(prop.getProperty("peer.discovery.workers", "2"));
    }

    public int peerConnectionTimeout() {
        return Integer.parseInt(prop.getProperty("peer.connection.timeout", "10")) * 1000;
    }

    public int transactionApproveTimeout() {
        return Integer.parseInt(prop.getProperty("transaction.approve.timeout", String.valueOf("DEFAULT_TX_APPROVE_TIMEOUT")));
    }

    public String peerDiscoveryIPList() {
        return prop.getProperty("peer.discovery.ip.list", DEFAULT_DISCOVERY_PEER_LIST);
    }

    public boolean databaseReset() {
        return Boolean.parseBoolean(prop.getProperty("database.reset", String.valueOf(DEFAULT_DB_RESET)));
    }

    public void setDatabaseReset(Boolean reset) {
        prop.setProperty("database.reset", reset.toString());
    }

    public String activePeerIP() {
        return prop.getProperty("peer.active.ip", DEFAULT_ACTIVE_PEER_IP);
    }

    public void setActivePeerIP(String host) {
        prop.setProperty("peer.active.ip", host);
    }

    public int activePeerPort() {
        return Integer.parseInt(prop.getProperty("peer.active.port", String.valueOf(DEFAULT_ACTIVE_PORT)));
    }

    public void setActivePeerPort(Integer port) {
        prop.setProperty("peer.active.port", port.toString());
    }

    public String samplesDir() {
        return prop.getProperty("samples.dir", DEFAULT_SAMPLES_DIR);
    }

    public String coinbaseSecret() {
        return prop.getProperty("coinbase.secret", DEFAULT_COINBASE_SECRET);
    }

    public Integer peerChannelReadTimeout() {
        return Integer.parseInt(prop.getProperty("peer.channel.read.timeout", String.valueOf(DEFAULT_ACTIVE_PEER_CHANNEL_TIMEOUT)));
    }

    public Integer traceStartBlock() {
        return Integer.parseInt(prop.getProperty("trace.startblock", String.valueOf(DEFAULT_TRACE_STARTBLOCK)));
    }

    public Boolean recordBlocks() {
        return Boolean.parseBoolean(prop.getProperty("record.blocks", String.valueOf(DEFAULT_RECORD_BLOCKS)));
    }

    public Boolean dumpFull() {
        return Boolean.parseBoolean(prop.getProperty("dump.full", String.valueOf(DEFAULT_DUMP_FULL)));
    }

    public String dumpDir() {
        return prop.getProperty("dump.dir", DEFAULT_DUMP_DIR);
    }

    public String dumpStyle() {
        return prop.getProperty("dump.style", DEFAULT_DUMP_STYLE);
    }

    public Integer dumpBlock() {
        return Integer.parseInt(prop.getProperty("dump.block", String.valueOf(DEFAULT_VMTRACE_BLOCK)));
    }

    public String databaseDir() {
        return prop.getProperty("database.dir", DEFAULT_DATABASE_DIR);
    }

    public void setDataBaseDir(String dataBaseDir) {
        prop.setProperty("database.dir", dataBaseDir);
    }

    public Boolean dumpCleanOnRestart() {
        return Boolean.parseBoolean(prop.getProperty("dump.clean.on.restart", String.valueOf(DEFAULT_DUMP_CLEAN_ON_RESTART)));
    }

    public Boolean playVM() {
        return Boolean.parseBoolean(prop.getProperty("play.vm", String.valueOf(DEFAULT_PLAY_VM)));
    }

    public Boolean blockChainOnly() {
        return Boolean.parseBoolean(prop.getProperty("blockchain.only", String.valueOf(DEFAULT_BLOCKCHAIN_ONLY)));
    }

    public Integer maxHashesAsk() {
        return Integer.parseInt(prop.getProperty("max.hashes.ask", String.valueOf(DEFAULT_MAX_HASHES_ASK)));
    }

    public Integer maxBlocksAsk() {
        return Integer.parseInt(prop.getProperty("max.blocks.ask", String.valueOf(DEFAULT_MAX_BLOCKS_ASK)));
    }

    public Integer maxBlocksQueued() {
        return Integer.parseInt(prop.getProperty("max.blocks.queued", String.valueOf(DEFAULT_MAX_BLOCKS_QUEUED)));
    }

    public String projectVersion() {
        return prop.getProperty("project.version", DEFAULT_PROJECT_VERSION);
    }

    public String helloPhrase() {
        return prop.getProperty("hello.phrase", DEFAULT_HELLO_PHRASE);
    }

    public String rootHashStart() {
        if (prop.isEmpty()) return null;
        String hash = prop.getProperty("root.hash.start");
        if (hash == null || hash.equals("-1"))
            return null;
        return hash;
    }

    public List<String> peerCapabilities() {
        String capabilitiesList = prop.getProperty("peer.capabilities", DEFAULT_PROTOCOL_LIST);
        return Arrays.asList(capabilitiesList.split(","));
    }

    public boolean vmTrace() {
        return boolProperty("vm.structured.trace", DEFAULT_VM_TRACE);
    }

    private boolean boolProperty(String key, Boolean defaultValue) {
        return Boolean.parseBoolean(prop.getProperty(key, String.valueOf(defaultValue)));
    }

    public String vmTraceDir() {
        return prop.getProperty("vm.structured.dir", DEFAULT_VM_TRACE_DIR);
    }

    public int listenPort() {
        return Integer.parseInt(prop.getProperty("peer.listen.port", String.valueOf(DEFAULT_PEER_LISTEN_PORT)));
    }

    public String getKeyValueDataSource() {
        return prop.getProperty("keyvalue.datasource", DEFAULT_KEY_VALUE_DATA_SOURCE);
    }
    
    public boolean isRedisEnabled() {
        return boolProperty("redis.enabled", DEFAULT_REDIS_ENABLED);    
    }

    public void setListenPort(Integer port) {
        prop.setProperty("peer.listen.port", port.toString());
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

    /*
     *
     * Testing
     *
     */
    public boolean vmTestLoadLocal() {
        return Boolean.parseBoolean(prop.getProperty("GitHubTests.VMTest.loadLocal", String.valueOf(DEFAULT_VMTEST_LOAD_LOCAL)));
    }

    public static void main(String args[]) {
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.print();
    }
}