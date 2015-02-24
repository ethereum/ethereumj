package org.ethereum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to retrieve property values from the system.properties files
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public abstract class SystemProperties {
    final static Logger logger = LoggerFactory.getLogger("config");

    final static String K_BLOCKCHAIN_ONLY = "blockchain.only";
    final static String K_COINBASE_SECRET = "coinbase.secret";
    final static String K_DUMP_BLOCK = "dump.block";
    final static String K_DUMP_CLEAN_ON_RESTART = "dump.clean.on.restart";
    final static String K_DUMP_DIR = "dump.dir";
    final static String K_DUMP_FULL = "dump.full";
    final static String K_DUMP_STYLE = "dump.style";
    final static String K_DATABASE_DIR = "database.dir";
    final static String K_DATABASE_RESET = "database.reset";
    final static String K_HELLO_PHRASE = "hello.phrase";
    final static String K_KEYVALUE_DATASOURCE = "keyvalue.datasource";
    final static String K_MAX_BLOCKS_ASK = "max.blocks.ask";
    final static String K_MAX_HASHES_ASK = "max.hashes.ask";
    final static String K_MAX_BLOCKS_QUEUED = "max.blocks.queued";
    final static String K_PEER_ACTIVE_IP = "peer.active.ip";
    final static String K_PEER_ACTIVE_PORT = "peer.active.port";
    final static String K_PEER_CAPABILITIES = "peer.capabilities";
    final static String K_PEER_CHANNEL_READ_TIMEOUT = "peer.channel.read.timeout";
    final static String K_PEER_CONNECTION_TIMEOUT = "peer.connection.timeout";
    final static String K_PEER_DISCOVERY_ENABLED = "peer.discovery.enabled";
    final static String K_PEER_DISCOVERY_WORKERS = "peer.discovery.workers";
    final static String K_PEER_DISCOVERY_IP_LIST = "peer.discovery.ip.list";
    final static String K_PEER_LISTEN_PORT = "peer.listen.port";
    final static String K_PLAY_VM = "play.vm";
    final static String K_PROJECT_VERSION = "project.version";
    final static String K_RECORD_BLOCKS = "record.blocks";
    final static String K_ROOT_HASH_START = "root.hash.start";
    final static String K_SAMPLES_DIR = "samples.dir";
    final static String K_TRANSACTION_APPROVE_TIMEOUT = "transaction.approve.timeout";
    final static String K_TRACE_STARTBLOCK = "trace.startblock";
    final static String K_VM_STRUCTURED_DIR = "vm.structured.dir";
    final static String K_VM_STRUCTURED_TRACE = "vm.structured.trace";

    //testing, this is an odd key
    final static String K_VM_TEST_LOAD_LOCAL = "GitHubTests.VMTest.loadLocal";

    private final static String DEFAULT_IMPLEMENTATION_CLASS_FQCN = "org.ethereum.config.PropertiesSystemProperties";
    private final static String SYSPROP_CONFIG_IMPLEMENTATION_CLASS = "config.implementation.class";

    public final static SystemProperties CONFIG;

    final static Map<String,Object> DEFAULTS;

    // utilities for implementations in this package
    final static String TRADITIONAL_PROPS_FILENAME;
    final static String TRADITIONAL_PROPS_RESOURCE;

    static {
	String userDir = System.getProperty( "user.dir" );

	TRADITIONAL_PROPS_FILENAME = userDir + "/config/system.properties";
	TRADITIONAL_PROPS_RESOURCE = "system.properties";

	Map<String,Object> tmpDefaults = new HashMap<>();
	tmpDefaults.put( K_BLOCKCHAIN_ONLY,             false                    );
	tmpDefaults.put( K_COINBASE_SECRET,             "monkey"                 );
	tmpDefaults.put( K_DUMP_BLOCK,                  0                        );
	tmpDefaults.put( K_DUMP_CLEAN_ON_RESTART,       true                     );
	tmpDefaults.put( K_DUMP_DIR,                    "dmp"                    );
	tmpDefaults.put( K_DUMP_FULL,                   false                    );
	tmpDefaults.put( K_DUMP_STYLE,                  "standard+"              );
	tmpDefaults.put( K_DATABASE_DIR,                userDir                  );
	tmpDefaults.put( K_DATABASE_RESET,              false                    );
	tmpDefaults.put( K_HELLO_PHRASE,                "Dev"                    );
	tmpDefaults.put( K_KEYVALUE_DATASOURCE,         "leveldb"                );
	tmpDefaults.put( K_MAX_BLOCKS_ASK,              10                       );
	tmpDefaults.put( K_MAX_HASHES_ASK,              -1                       );
	tmpDefaults.put( K_MAX_BLOCKS_QUEUED,           300                      );
	tmpDefaults.put( K_PEER_ACTIVE_IP,              "poc-7.ethdev.com"       );
	tmpDefaults.put( K_PEER_ACTIVE_PORT,            30303                    );
	tmpDefaults.put( K_PEER_CAPABILITIES, "         eth,shh"                 );
	tmpDefaults.put( K_PEER_CHANNEL_READ_TIMEOUT,   5                        );
	tmpDefaults.put( K_PEER_CONNECTION_TIMEOUT,     10                       );
	tmpDefaults.put( K_PEER_DISCOVERY_ENABLED,      true                     );
	tmpDefaults.put( K_PEER_DISCOVERY_WORKERS,      2                        );
	tmpDefaults.put( K_PEER_DISCOVERY_IP_LIST,      "poc-7.ethdev.com:30303" );
	tmpDefaults.put( K_PEER_LISTEN_PORT,            30303                    );
	tmpDefaults.put( K_PLAY_VM,                     true                     );
	tmpDefaults.put( K_PROJECT_VERSION,             ""                       );
	tmpDefaults.put( K_RECORD_BLOCKS,               false                    );
	tmpDefaults.put( K_ROOT_HASH_START,             -1                       );
	tmpDefaults.put( K_SAMPLES_DIR,                 "samples"                );
	tmpDefaults.put( K_TRANSACTION_APPROVE_TIMEOUT, 10                       );
	tmpDefaults.put( K_TRACE_STARTBLOCK,            -1                       );
	tmpDefaults.put( K_VM_STRUCTURED_DIR,           "dmp"                    );
	tmpDefaults.put( K_VM_STRUCTURED_TRACE,         false                    );
	tmpDefaults.put( K_VM_TEST_LOAD_LOCAL,          false                    );

	DEFAULTS = Collections.unmodifiableMap( tmpDefaults );

	String implementationClassFqcn = null;
	try {
	    implementationClassFqcn = System.getProperty( SYSPROP_CONFIG_IMPLEMENTATION_CLASS );
	    if ( implementationClassFqcn == null ) implementationClassFqcn = DEFAULT_IMPLEMENTATION_CLASS_FQCN;
	    CONFIG = (SystemProperties) Class.forName( implementationClassFqcn ).newInstance();
	} catch ( Exception e ) {
	    throw new RuntimeException( "Could not instantiate concrete implementation '" + implementationClassFqcn + "'." );
	}

	if ( logger.isDebugEnabled() ) {
	    logger.debug( "ethereumj configuration:" );
	    CONFIG.debugPrint();
	}
    }

    /*
     *
     *  Abstract, package-scoped methods
     *
     */
    abstract boolean getBoolean( String key );
    abstract int getInt( String key );
    abstract String getString( String key );
    abstract String getCoerceToString( String key );

    /*
     *
     *  Public, abstract setters for org.ethereum.cli.CLIInterface
     *
     */
    public abstract void setListenPort(Integer port);
    public abstract void setDatabaseReset(Boolean reset);
    public abstract void setActivePeerIP(String host);
    public abstract void setActivePeerPort(Integer port);
    public abstract void setDataBaseDir(String dataBaseDir);

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

    interface LinePrinter {
	public void print( String key, Object value );
    }
    private static String kvline( String key, Object value ) { 
	return "    " + key + " -> " + value; 
    }

    private LinePrinter infoPrinter = new LinePrinter() {
	    public void print( String key, Object value ) {
		if ( logger.isInfoEnabled() )
		    logger.info( kvline( key, value ) );
	    }
    };
    private LinePrinter debugPrinter = new LinePrinter() {
	    public void print( String key, Object value ) {
		if ( logger.isDebugEnabled() )
		    logger.debug( kvline( key, value ) );
	    }
    };

    private void print(LinePrinter lp) {
	for ( String key : DEFAULTS.keySet() ) {
            String value = getCoerceToString( key );
	    value = ( value == null ? "null" : value );
	    if ( K_COINBASE_SECRET.equals( key ) ) value = "[hidden]";
	    lp.print( key, value );
        }
    }

    public void debugPrint() { print( debugPrinter ); }
    public void infoPrint()  { print( infoPrinter ); }
    public void print()      { infoPrint(); }

    /*
     *
     * Testing
     *
     */
    public static void main(String args[]) {
        CONFIG.print();
    }
}
