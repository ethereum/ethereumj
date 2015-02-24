package org.ethereum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class to retrieve property values from the system.properties files
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {
    private static Logger logger = LoggerFactory.getLogger("general");

    private final static String K_BLOCKCHAIN_ONLY = "blockchain.only";
    private final static String K_COINBASE_SECRET = "coinbase.secret";
    private final static String K_DUMP_BLOCK = "dump.block";
    private final static String K_DUMP_CLEAN_ON_RESTART = "dump.clean.on.restart";
    private final static String K_DUMP_DIR = "dump.dir";
    private final static String K_DUMP_FULL = "dump.full";
    private final static String K_DUMP_STYLE = "dump.style";
    private final static String K_DATABASE_DIR = "database.dir";
    private final static String K_DATABASE_RESET = "database.reset";
    private final static String K_HELLO_PHRASE = "hello.phrase";
    private final static String K_KEYVALUE_DATASOURCE = "keyvalue.datasource";
    private final static String K_MAX_BLOCKS_ASK = "max.blocks.ask";
    private final static String K_MAX_HASHES_ASK = "max.hashes.ask";
    private final static String K_MAX_BLOCKS_QUEUED = "max.blocks.queued";
    private final static String K_PEER_ACTIVE_IP = "peer.active.ip";
    private final static String K_PEER_ACTIVE_PORT = "peer.active.port";
    private final static String K_PEER_CAPABILITIES = "peer.capabilities";
    private final static String K_PEER_CHANNEL_READ_TIMEOUT = "peer.channel.read.timeout";
    private final static String K_PEER_CONNECTION_TIMEOUT = "peer.connection.timeout";
    private final static String K_PEER_DISCOVERY_ENABLED = "peer.discovery.enabled";
    private final static String K_PEER_DISCOVERY_WORKERS = "peer.discovery.workers";
    private final static String K_PEER_DISCOVERY_IP_LIST = "peer.discovery.ip.list";
    private final static String K_PEER_LISTEN_PORT = "peer.listen.port";
    private final static String K_PLAY_VM = "play.vm";
    private final static String K_PROJECT_VERSION = "project.version";
    private final static String K_RECORD_BLOCKS = "record.blocks";
    private final static String K_ROOT_HASH_START = "root.hash.start";
    private final static String K_SAMPLES_DIR = "samples.dir";
    private final static String K_TRANSACTION_APPROVE_TIMEOUT = "transaction.approve.timeout";
    private final static String K_TRACE_STARTBLOCK = "trace.startblock";
    private final static String K_VM_STRUCTURED_DIR = "vm.structured.dir";
    private final static String K_VM_STRUCTURED_TRACE = "vm.structured.trace";

    //testing, odd key
    private final static String K_VM_TEST_LOAD_LOCAL = "GitHubTests.VMTest.loadLocal";

    private final static Map<String,Object> DEFAULTS;

    static {
	String userDir = System.getProperty( "user.dir" );
	Map<String,Object> tmp = new HashMap<>();
	tmp.put( K_BLOCKCHAIN_ONLY, false );
	tmp.put( K_COINBASE_SECRET, "monkey" );
	tmp.put( K_DUMP_BLOCK, 0 );
	tmp.put( K_DUMP_CLEAN_ON_RESTART, true );
	tmp.put( K_DUMP_DIR, "dmp" );
	tmp.put( K_DUMP_FULL, false );
	tmp.put( K_DUMP_STYLE, "standard+" );
	tmp.put( K_DATABASE_DIR, userDir );
	tmp.put( K_DATABASE_RESET, false );
	tmp.put( K_HELLO_PHRASE, "Dev" );
	tmp.put( K_KEYVALUE_DATASOURCE, "leveldb" );
	tmp.put( K_MAX_BLOCKS_ASK, 10 );
	tmp.put( K_MAX_HASHES_ASK, -1 );
	tmp.put( K_MAX_BLOCKS_QUEUED, 300 );
	tmp.put( K_PEER_ACTIVE_IP, "poc-7.ethdev.com" );
	tmp.put( K_PEER_ACTIVE_PORT, 30303 );
	tmp.put( K_PEER_CAPABILITIES, "eth,shh" );
	tmp.put( K_PEER_CHANNEL_READ_TIMEOUT, 5 );
	tmp.put( K_PEER_CONNECTION_TIMEOUT, 10 );
	tmp.put( K_PEER_DISCOVERY_ENABLED, true );
	tmp.put( K_PEER_DISCOVERY_WORKERS, 2 );
	tmp.put( K_PEER_DISCOVERY_IP_LIST, "poc-7.ethdev.com:30303" );
	tmp.put( K_PEER_LISTEN_PORT, 30303 );
	tmp.put( K_PLAY_VM, true );
	tmp.put( K_PROJECT_VERSION, "" );
	tmp.put( K_RECORD_BLOCKS, false );
	tmp.put( K_ROOT_HASH_START, -1 );
	tmp.put( K_SAMPLES_DIR, "samples" );
	tmp.put( K_TRANSACTION_APPROVE_TIMEOUT, 10 );
	tmp.put( K_TRACE_STARTBLOCK, -1 );
	tmp.put( K_VM_STRUCTURED_DIR, "dmp" );
	tmp.put( K_VM_STRUCTURED_TRACE, false );
	tmp.put( K_VM_TEST_LOAD_LOCAL, false );
	DEFAULTS = Collections.unmodifiableMap( tmp );
    }

    public final static SystemProperties CONFIG = new SystemProperties();

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

    /*
     *
     *  Private utilitiies
     *
     */
    private String getNonDefaultAsString( String key ) {
	String value = System.getProperty( key );
	if ( value == null ) value = prop.getProperty( key );
    }
    private boolean getBoolean( String key ) {
	String value = getNondefaultAsString( key );
	return ( value != null ? Boolean.parseBoolean( value ) : ((Boolean) DEFAULTS.get( key )).booleanValue() );
    }
    private int getInt( String key ) {
	String value = getNondefaultAsString( key );
	return ( value != null ? Integer.parseInt( value ) : ((Integer) DEFAULTS.get( key )).intValue() );
    }
    private String getString( String key ) {
	String value = getNondefaultAsString( key );
	return ( value != null ? value : (String) DEFAULTS.get( key ) );
    }

    /*
     *
     *  Public accessors
     *
     */
    public String  activePeerIP()              { return getString( K_PEER_ACTIVE_IP ); }
    public int     activePeerPort()            { return getInt( K_PEER_ACTIVE_PORT ); }
    public boolean blockChainOnly()            { return getBoolean( K_BLOCKCHAIN_ONLY ); }
    public String  coinbaseSecret()            { return getString( K_COINBASE_SECRET ); }
    public String  databaseDir()               { return getString( K_DATABASE_DIR ); }
    public boolean databaseReset()             { return getBoolean( K_DATABASE_RESET ); }
    public int     dumpBlock()                 { return getInt( K_DUMP_BLOCK ); }
    public boolean dumpCleanOnRestart()        { return getBoolean( K_DUMP_CLEAN_ON_RESTART ); }
    public String  dumpDir()                   { return getString( K_DUMP_DIR ); }
    public boolean dumpFull()                  { return getBoolean( K_DUMP_FULL ); }
    public String  dumpStyle()                 { return getString( K_DUMP_STYLE ); }
    public String  helloPhrase()               { return getString( K_HELLO_PHRASE ); }
    public int     listenPort()                { return getInt( K_PEER_LISTEN_PORT ); }
    public int     maxBlocksAsk()              { return getInt( K_MAX_BLOCKS_ASK ); }
    public int     maxBlocksQueued()           { return getInt( K_MAX_BLOCKS_QUEUED ); }
    public int     maxHashesAsk()              { return getInt( K_MAX_HASHES_ASK ); }
    public int     peerChannelReadTimeout()    { return getInt( K_PEER_CHANNEL_READ_TIMEOUT ); }
    public boolean peerDiscoveryEnabled()      { return getBoolean( K_PEER_DISCOVERY_ENABLED ); }
    public String  peerDiscoveryIPList()       { return getString( K_PEER_DISCOVERY_IP_LIST ); }
    public int     peerDiscoveryWorkers()      { return getInt( K_PEER_DISCOVERY_WORKERS ); }
    public boolean playVM()                    { return getBoolean( K_PLAY_VM ); }
    public String  projectVersion()            { return getString( K_PROJECT_VERSION ); }
    public boolean recordBlocks()              { return getBoolean( K_RECORD_BLOCKS ); }
    public String  samplesDir()                { return getString( K_SAMPLES_DIR ); }
    public int     transactionApproveTimeout() { return getInt( K_TRANSACTION_APPROVE_TIMEOUT ); }
    public int     traceStartBlock()           { return getInt( K_TRACE_STARTBLOCK ); }
    public boolean vmTrace()                   { return getBoolean( K_VM_STRUCTURED_TRACE ); }
    public String  vmTraceDir()                { return getString( K_VM_STRUCTURED_DIR ); }
    public boolean vmTestLoadLocal()           { return getBoolean( K_VM_TEST_LOAD_LOCAL ); }

    public String  getKeyValueDataSource()     { return getString( K_KEYVALUE_DATASOURCE ); } 

    // special-case accessors
    public String rootHashStart() {
	int intVal = getInt( K_ROOT_HASH_START );
        return ( intVal == -1 ? null : String.valueOf( intVal ) );
    }

    public List<String> peerCapabilities() {
        String capabilitiesList = getString( K_PEER_CAPABILITIES );
        return Arrays.asList(capabilitiesList.split(","));
    }
    public int peerConnectionTimeout() { // configured in seconds, expected in millis
	return getInt( K_PEER_CONNECTION_TIMEOUT ) * 1000; 
    }

    // setters for org.ethereum.cli.CLIInterface
    public void setListenPort(Integer port)        { prop.setProperty(K_PEER_LISTEN_PORT, port.toString()); }
    public void setDatabaseReset(Boolean reset)    { prop.setProperty(K_DATABASE_RESET, reset.toString()); }
    public void setActivePeerIP(String host)       { prop.setProperty(K_PEER_ACTIVE_IP, host); }
    public void setActivePeerPort(Integer port)    { prop.setProperty(K_PEER_ACTIVE_PORT, port.toString()); }
    public void setDataBaseDir(String dataBaseDir) { prop.setProperty(K_DATABASE_DIR, dataBaseDir); }

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

    public static void main(String args[]) {
        SystemProperties systemProperties = new SystemProperties();
        systemProperties.print();
    }
}
