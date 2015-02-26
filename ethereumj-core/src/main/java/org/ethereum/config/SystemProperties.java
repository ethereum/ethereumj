package org.ethereum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.ethereum.config.KeysDefaults.*;

/**
 * Utility class to retrieve property values from the system.properties files
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public abstract class SystemProperties {
    final static Logger logger = LoggerFactory.getLogger("config");

    public final static SystemProperties CONFIG;

    private final static String DEFAULT_IMPLEMENTATION_CLASS_FQCN = "org.ethereum.config.PropertiesSystemProperties";
    private final static String SYSPROP_CONFIG_IMPLEMENTATION_CLASS = "config.implementation.class";

    static {
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
	for ( String key : ORDERED_KEYS ) {
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
